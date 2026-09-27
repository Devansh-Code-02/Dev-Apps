package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val macAddress: String,
    val deviceName: String,
    val customAlias: String? = null,
    val deviceType: String = "PHONE", // SPEAKER, PHONE, TABLET, LAPTOP, HEADSET, OTHER
    val isSaved: Boolean = false,
    val isBlocked: Boolean = false,
    val rsaPublicKey: String? = null,
    val sharedAesKeyBase64: String? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val rssi: Int = -60,
    val isOnline: Boolean = true
) {
    fun getDisplayName(): String {
        return if (!customAlias.isNullOrBlank()) customAlias else deviceName
    }
}
