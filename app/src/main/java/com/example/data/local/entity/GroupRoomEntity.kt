package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_rooms")
data class GroupRoomEntity(
    @PrimaryKey val id: String,
    val roomName: String,
    val description: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val memberMacsJson: String = "[]",
    val sharedRoomKeyBase64: String = ""
)
