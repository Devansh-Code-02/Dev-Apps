package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BackupDao
import com.example.data.local.dao.GroupRoomDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PeerDao
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.GroupRoomEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity

@Database(
    entities = [
        PeerEntity::class,
        MessageEntity::class,
        GroupRoomEntity::class,
        BackupHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peerDao(): PeerDao
    abstract fun messageDao(): MessageDao
    abstract fun groupRoomDao(): GroupRoomDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bluetooth_mesh_chat.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
