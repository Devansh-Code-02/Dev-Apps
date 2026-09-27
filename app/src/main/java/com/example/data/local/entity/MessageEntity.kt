package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String, // peer MAC or group roomId
    val isGroup: Boolean = false,
    val senderMac: String,
    val recipientMac: String,
    val senderName: String,
    val encryptedPayloadBase64: String = "",
    val ivBase64: String = "",
    val decryptedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean,
    val messageType: String = "TEXT", // TEXT, IMAGE, FILE, VOICE
    val fileUri: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val transferProgress: Float = 1.0f,
    val isEncrypted: Boolean = true
)
