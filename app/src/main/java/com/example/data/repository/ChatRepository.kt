package com.example.data.repository

import com.example.data.bluetooth.BluetoothChatManager
import com.example.data.bluetooth.BluetoothPacket
import com.example.data.local.AppDatabase
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StatusUpdateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val database: AppDatabase,
    val bluetoothManager: BluetoothChatManager
) {
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()
    private val statusDao = database.statusDao()
    private val callDao = database.callLogDao()

    private val repoScope = CoroutineScope(Dispatchers.IO)

    init {
        listenToBluetoothPackets()
        listenToBluetoothState()
        clearLegacyDummyData()
    }

    private fun listenToBluetoothPackets() {
        repoScope.launch {
            bluetoothManager.incomingPackets.collect { packet ->
                handleInboundPacket(packet)
            }
        }
    }

    private fun listenToBluetoothState() {
        repoScope.launch {
            bluetoothManager.uiState.collect { btState ->
                val connectedAddrs = btState.connectedPeers.map { it.address }
                val connectedNames = btState.connectedPeers.map { it.name.lowercase() }

                // Auto-create conversations for newly connected peers
                btState.connectedPeers.forEach { peer ->
                    val peerId = peer.name.replace(" ", "_").lowercase()
                    val existing = conversationDao.getConversationById(peerId)
                    if (existing == null) {
                        conversationDao.insertConversation(
                            ChatConversationEntity(
                                id = peerId,
                                peerName = peer.name,
                                peerAddress = peer.address,
                                avatarColorIndex = (peer.name.hashCode() and 0x7FFFFFFF) % 6,
                                lastMessage = "🟢 Connected via Bluetooth (Offline)",
                                lastMessageTimestamp = System.currentTimeMillis(),
                                unreadCount = 0,
                                isOnline = true,
                                customStatus = "Direct RFCOMM Peer • Zero Internet"
                            )
                        )
                    }
                }

                val all = conversationDao.getAllConversations().firstOrNull() ?: emptyList()
                all.forEach { conv ->
                    val isConnected = connectedAddrs.contains(conv.peerAddress) ||
                            connectedNames.contains(conv.peerName.lowercase())
                    if (conv.isOnline != isConnected) {
                        conversationDao.updateConversation(conv.copy(isOnline = isConnected))
                    }
                }
            }
        }
    }

    private suspend fun handleInboundPacket(packet: BluetoothPacket) {
        val chatId = packet.senderName.replace(" ", "_").lowercase()
        
        // Ensure conversation exists
        val existing = conversationDao.getConversationById(chatId)
        if (existing == null) {
            conversationDao.insertConversation(
                ChatConversationEntity(
                    id = chatId,
                    peerName = packet.senderName,
                    peerAddress = "BT-PEER",
                    avatarColorIndex = (chatId.hashCode() and 0x7FFFFFFF) % 6,
                    lastMessage = packet.text,
                    lastMessageTimestamp = packet.timestamp,
                    unreadCount = 1,
                    isOnline = true
                )
            )
        } else {
            conversationDao.updateConversation(
                existing.copy(
                    lastMessage = packet.text,
                    lastMessageTimestamp = packet.timestamp,
                    unreadCount = existing.unreadCount + 1,
                    isOnline = true
                )
            )
        }

        val type = try {
            MessageType.valueOf(packet.type)
        } catch (e: Exception) {
            MessageType.TEXT
        }

        val message = MessageEntity(
            id = packet.id,
            chatId = chatId,
            senderId = chatId,
            senderName = packet.senderName,
            text = packet.text,
            timestamp = packet.timestamp,
            isFromMe = false,
            status = MessageStatus.READ,
            messageType = type,
            extraData = packet.extra
        )
        messageDao.insertMessage(message)
    }

    fun getAllConversations(): Flow<List<ChatConversationEntity>> =
        conversationDao.getAllConversations()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForChat(chatId)

    fun getAllStatuses(): Flow<List<StatusUpdateEntity>> =
        statusDao.getAllStatuses()

    fun getAllCallLogs(): Flow<List<CallLogEntity>> =
        callDao.getAllCallLogs()

    suspend fun clearUnread(chatId: String) {
        conversationDao.clearUnread(chatId)
    }

    suspend fun sendMessage(
        chatId: String,
        peerName: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        extraData: String? = null
    ) {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "me",
            senderName = "You",
            text = text,
            timestamp = now,
            isFromMe = true,
            status = MessageStatus.SENT,
            messageType = type,
            extraData = extraData
        )

        messageDao.insertMessage(message)

        val snippet = when (type) {
            MessageType.VOICE -> "🎤 Voice message"
            MessageType.IMAGE -> "📷 Photo"
            MessageType.LOCATION -> "📍 Live Location"
            MessageType.DOCUMENT -> "📄 Document"
            else -> text
        }

        val existing = conversationDao.getConversationById(chatId)
        if (existing != null) {
            conversationDao.updateConversation(
                existing.copy(
                    lastMessage = snippet,
                    lastMessageTimestamp = now
                )
            )
        }

        // Send over Bluetooth RFCOMM stream using the actual Bluetooth device name
        val myName = bluetoothManager.getMyDeviceName()
        val packet = BluetoothPacket(
            id = messageId,
            type = type.name,
            senderName = myName,
            text = text,
            timestamp = now,
            extra = extraData
        )
        val sent = bluetoothManager.sendPacket(packet, targetAddress = existing?.peerAddress)

        if (sent) {
            messageDao.updateMessage(message.copy(status = MessageStatus.DELIVERED))
        }
    }


    suspend fun postStatus(text: String, caption: String = "") {
        val status = StatusUpdateEntity(
            id = UUID.randomUUID().toString(),
            userName = "My Status",
            text = text,
            timestamp = System.currentTimeMillis(),
            isMe = true,
            isViewed = false,
            caption = caption
        )
        statusDao.insertStatus(status)
    }

    suspend fun addCallLog(peerName: String, callType: String, direction: String, duration: String) {
        val log = CallLogEntity(
            id = UUID.randomUUID().toString(),
            peerName = peerName,
            timestamp = System.currentTimeMillis(),
            callType = callType,
            direction = direction,
            duration = duration
        )
        callDao.insertCallLog(log)
    }

    private fun clearLegacyDummyData() {
        repoScope.launch {
            // Delete legacy pre-seeded dummy records so the app is completely empty by default
            conversationDao.deleteConversation("ghaznain_ahmad")
            conversationDao.deleteConversation("xer0byte_mesh_group")
            conversationDao.deleteConversation("nearby_user")
            messageDao.deleteMessagesForChat("ghaznain_ahmad")
            messageDao.deleteMessagesForChat("xer0byte_mesh_group")
            messageDao.deleteMessagesForChat("nearby_user")
            statusDao.deleteStatus("status_ghaznain")
            statusDao.deleteStatus("status_node")
            callDao.deleteCallLog("call_1")
            callDao.deleteCallLog("call_2")
            callDao.deleteCallLog("call_3")
        }
    }
}
