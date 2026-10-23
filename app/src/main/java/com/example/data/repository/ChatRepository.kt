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
        seedInitialDataIfEmpty()
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
                            connectedNames.contains(conv.peerName.lowercase()) ||
                            (conv.id == "ghaznain_ahmad" && btState.isAnyConnected)
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

        // Send over Bluetooth RFCOMM stream
        val packet = BluetoothPacket(
            id = messageId,
            type = type.name,
            senderName = "You",
            text = text,
            timestamp = now,
            extra = extraData
        )
        val sent = bluetoothManager.sendPacket(packet, targetAddress = existing?.peerAddress)

        if (sent) {
            messageDao.updateMessage(message.copy(status = MessageStatus.DELIVERED))
        }

        // If chatting with offline simulated contact (Ghaznain Ahmad / Xer0byte Hub), trigger smart response
        if (chatId.contains("ghaznain", ignoreCase = true) || chatId.contains("xer0byte", ignoreCase = true)) {
            triggerOfflinePeerResponse(chatId, peerName, text, type)
        }
    }

    private fun triggerOfflinePeerResponse(
        chatId: String,
        peerName: String,
        userText: String,
        userType: MessageType
    ) {
        repoScope.launch {
            delay(1000)
            messageDao.updateChatMessagesStatus(chatId, MessageStatus.READ)
            delay(1500)

            val replyText = when {
                userType == MessageType.VOICE -> "Awaaz bilkul clear h! Xer0byte Bluetooth audio compression zabardast kaam kr rha h bina internet k."
                userType == MessageType.LOCATION -> "Location mil gai h. Bluetooth local radius (~100m) k andar link stable h."
                userText.contains("kya haal", ignoreCase = true) || userText.contains("kese", ignoreCase = true) ->
                    "Alhamdulillah sub theek! Xer0byte app completely offline Bluetooth pe active h."
                userText.contains("bluetooth", ignoreCase = true) || userText.contains("range", ignoreCase = true) ->
                    "Ji bilkul, yeh system Bluetooth RFCOMM protocol use krta h, internet connection ki bilkul zaroorat nahi."
                userText.contains("owner", ignoreCase = true) || userText.contains("ghaznain", ignoreCase = true) ->
                    "Ghaznain Ahmad is the Founder & Owner of Xer0byte! Welcome to our offline private messenger."
                else -> "Message received over Xer0byte Bluetooth RFCOMM! Status: 100% Offline & Secure."
            }

            val replyId = UUID.randomUUID().toString()
            val replyNow = System.currentTimeMillis()
            val replyMsg = MessageEntity(
                id = replyId,
                chatId = chatId,
                senderId = chatId,
                senderName = peerName,
                text = replyText,
                timestamp = replyNow,
                isFromMe = false,
                status = MessageStatus.READ,
                messageType = MessageType.TEXT
            )
            messageDao.insertMessage(replyMsg)

            val conv = conversationDao.getConversationById(chatId)
            if (conv != null) {
                conversationDao.updateConversation(
                    conv.copy(
                        lastMessage = replyText,
                        lastMessageTimestamp = replyNow
                    )
                )
            }
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

    private fun seedInitialDataIfEmpty() {
        repoScope.launch {
            val existing = conversationDao.getAllConversations().firstOrNull()
            if (existing.isNullOrEmpty()) {
                val now = System.currentTimeMillis()

                val defaultConversations = listOf(
                    ChatConversationEntity(
                        id = "ghaznain_ahmad",
                        peerName = "Ghaznain Ahmad",
                        peerAddress = "BT-00:1A:7D:DA:71:01",
                        avatarColorIndex = 0,
                        lastMessage = "Welcome to Xer0byte! Bluetooth chat offline active h.",
                        lastMessageTimestamp = now - 60000,
                        unreadCount = 1,
                        isOnline = true,
                        customStatus = "Owner & Founder @ Xer0byte",
                        isPinned = true
                    ),
                    ChatConversationEntity(
                        id = "xer0byte_mesh_group",
                        peerName = "Xer0byte Bluetooth Node",
                        peerAddress = "BT-00:1A:7D:DA:71:02",
                        avatarColorIndex = 1,
                        lastMessage = "🎤 Voice message (0:14)",
                        lastMessageTimestamp = now - 360000,
                        unreadCount = 0,
                        isOnline = true,
                        customStatus = "Local RFCOMM Hub • Range: 100m",
                        isPinned = false
                    ),
                    ChatConversationEntity(
                        id = "nearby_user",
                        peerName = "Nearby Peer (Device 8F2)",
                        peerAddress = "BT-00:1A:7D:DA:71:03",
                        avatarColorIndex = 2,
                        lastMessage = "Bluetooth paired successfully!",
                        lastMessageTimestamp = now - 1800000,
                        unreadCount = 0,
                        isOnline = true,
                        customStatus = "Using Xer0byte Offline Messenger",
                        isPinned = false
                    )
                )
                conversationDao.insertConversations(defaultConversations)

                // Seed messages for Ghaznain Ahmad
                val msgs = listOf(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "ghaznain_ahmad",
                        senderId = "ghaznain_ahmad",
                        senderName = "Ghaznain Ahmad",
                        text = "Assalam-o-Alaikum! Main Ghaznain Ahmad hoon, Xer0byte ka owner.",
                        timestamp = now - 120000,
                        isFromMe = false,
                        status = MessageStatus.READ,
                        messageType = MessageType.TEXT
                    ),
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "ghaznain_ahmad",
                        senderId = "ghaznain_ahmad",
                        senderName = "Ghaznain Ahmad",
                        text = "Yeh application baghair internet k Bluetooth RFCOMM protocol se direct phone-to-phone chalti h.",
                        timestamp = now - 90000,
                        isFromMe = false,
                        status = MessageStatus.READ,
                        messageType = MessageType.TEXT
                    ),
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "ghaznain_ahmad",
                        senderId = "me",
                        senderName = "You",
                        text = "Zabardast! Interface bilkul WhatsApp jesa lag rha h.",
                        timestamp = now - 70000,
                        isFromMe = true,
                        status = MessageStatus.READ,
                        messageType = MessageType.TEXT
                    ),
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "ghaznain_ahmad",
                        senderId = "ghaznain_ahmad",
                        senderName = "Ghaznain Ahmad",
                        text = "Welcome to Xer0byte! Bluetooth chat offline active h.",
                        timestamp = now - 60000,
                        isFromMe = false,
                        status = MessageStatus.READ,
                        messageType = MessageType.TEXT
                    )
                )
                messageDao.insertMessages(msgs)

                // Seed messages for Xer0byte Mesh Node
                val meshMsgs = listOf(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "xer0byte_mesh_group",
                        senderId = "xer0byte_mesh_group",
                        senderName = "Xer0byte Node",
                        text = "Audio transmission packet test",
                        timestamp = now - 400000,
                        isFromMe = false,
                        status = MessageStatus.READ,
                        messageType = MessageType.VOICE,
                        extraData = "0:14"
                    ),
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        chatId = "xer0byte_mesh_group",
                        senderId = "xer0byte_mesh_group",
                        senderName = "Xer0byte Node",
                        text = "Xer0byte P2P protocol: Packets encrypted locally, no SIM or WiFi needed.",
                        timestamp = now - 360000,
                        isFromMe = false,
                        status = MessageStatus.READ,
                        messageType = MessageType.TEXT
                    )
                )
                messageDao.insertMessages(meshMsgs)

                // Seed status updates
                val statuses = listOf(
                    StatusUpdateEntity(
                        id = "status_ghaznain",
                        userName = "Ghaznain Ahmad (Xer0byte)",
                        text = "Building decentralized offline communication technologies with Xer0byte 🚀",
                        timestamp = now - 1800000,
                        isMe = false,
                        isViewed = false,
                        caption = "Offline connectivity for everyone!"
                    ),
                    StatusUpdateEntity(
                        id = "status_node",
                        userName = "Xer0byte Bluetooth Node",
                        text = "Status: 100% P2P Bluetooth channel operational",
                        timestamp = now - 7200000,
                        isMe = false,
                        isViewed = true,
                        caption = "RFCOMM Socket Active"
                    )
                )
                statusDao.insertStatuses(statuses)

                // Seed call logs
                val calls = listOf(
                    CallLogEntity(
                        id = "call_1",
                        peerName = "Ghaznain Ahmad",
                        timestamp = now - 14400000,
                        callType = "AUDIO",
                        direction = "INCOMING",
                        duration = "03:45"
                    ),
                    CallLogEntity(
                        id = "call_2",
                        peerName = "Xer0byte Bluetooth Node",
                        timestamp = now - 28800000,
                        callType = "VIDEO",
                        direction = "MISSED",
                        duration = "00:00"
                    ),
                    CallLogEntity(
                        id = "call_3",
                        peerName = "Ghaznain Ahmad",
                        timestamp = now - 86400000,
                        callType = "AUDIO",
                        direction = "OUTGOING",
                        duration = "01:12"
                    )
                )
                callDao.insertCallLogs(calls)
            }
        }
    }
}
