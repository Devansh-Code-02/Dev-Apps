package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_history")
data class BackupHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemCount: Int,
    val fileSizeBytes: Long,
    val isEncrypted: Boolean = true
)
