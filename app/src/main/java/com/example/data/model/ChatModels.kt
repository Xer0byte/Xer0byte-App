package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    LOCATION,
    DOCUMENT,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
    val messageType: MessageType = MessageType.TEXT,
    val extraData: String? = null // for audio duration, file size, or coordinates
)

@Entity(tableName = "conversations")
data class ChatConversationEntity(
    @PrimaryKey
    val id: String,
    val peerName: String,
    val peerAddress: String, // Bluetooth MAC or unique ID
    val avatarColorIndex: Int = 0,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isOnline: Boolean = true,
    val customStatus: String = "Available on Xer0byte Bluetooth",
    val isPinned: Boolean = false
)

@Entity(tableName = "statuses")
data class StatusUpdateEntity(
    @PrimaryKey
    val id: String,
    val userName: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false,
    val isViewed: Boolean = false,
    val caption: String = ""
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey
    val id: String,
    val peerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val callType: String = "AUDIO", // AUDIO, VIDEO
    val direction: String = "INCOMING", // INCOMING, OUTGOING, MISSED
    val duration: String = "02:14"
)
