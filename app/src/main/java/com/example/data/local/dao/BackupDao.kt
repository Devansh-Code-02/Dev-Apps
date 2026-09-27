package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BackupHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupDao {
    @Query("SELECT * FROM backup_history ORDER BY timestamp DESC")
    fun getAllBackupHistory(): Flow<List<BackupHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupHistory(backup: BackupHistoryEntity): Long

    @Query("DELETE FROM backup_history WHERE id = :id")
    suspend fun deleteBackupHistory(id: Long)
}
