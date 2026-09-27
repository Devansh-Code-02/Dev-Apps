package com.example.data.repository

import com.example.data.backup.BackupManager
import com.example.data.local.dao.BackupDao
import com.example.data.local.dao.GroupRoomDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PeerDao
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.GroupRoomEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity
import com.example.data.p2p.P2PManager
import com.example.data.p2p.P2PState
import com.example.data.preferences.ThemePreferences
import com.example.data.preferences.UIStyleTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class ChatRepository(
    val peerDao: PeerDao,
    val messageDao: MessageDao,
    val groupRoomDao: GroupRoomDao,
    val backupDao: BackupDao,
    val p2pManager: P2PManager,
    val backupManager: BackupManager,
    val themePreferences: ThemePreferences
) {
    val allPeers: Flow<List<PeerEntity>> = peerDao.getAllPeers()
    val savedContacts: Flow<List<PeerEntity>> = peerDao.getSavedContacts()
    val groupRooms: Flow<List<GroupRoomEntity>> = groupRoomDao.getAllGroupRooms()
    val p2pState: StateFlow<P2PState> = p2pManager.state
    val backupHistory: Flow<List<BackupHistoryEntity>> = backupDao.getAllBackupHistory()

    val themeStyle: Flow<UIStyleTheme> = themePreferences.themeStyleFlow
    val isDarkMode: Flow<Boolean> = themePreferences.isDarkModeFlow
    val localDeviceName: Flow<String> = themePreferences.deviceNameFlow
    val isDiscoverable: Flow<Boolean> = themePreferences.isDiscoverableFlow

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)

    suspend fun updateCustomAlias(macAddress: String, alias: String?) {
        peerDao.updateCustomAlias(macAddress, alias)
    }

    suspend fun toggleSavedStatus(macAddress: String, isSaved: Boolean) {
        peerDao.updateSavedStatus(macAddress, isSaved)
    }

    suspend fun updateLocalDeviceName(newName: String) {
        themePreferences.setDeviceName(newName)
        p2pManager.updateLocalDeviceName(newName)
    }

    suspend fun setThemeStyle(style: UIStyleTheme) {
        themePreferences.setThemeStyle(style)
    }

    suspend fun setDarkMode(isDark: Boolean) {
        themePreferences.setDarkMode(isDark)
    }

    suspend fun createGroupRoom(name: String, description: String) {
        val roomId = "ROOM_${System.currentTimeMillis()}"
        val room = GroupRoomEntity(
            id = roomId,
            roomName = name,
            description = description,
            createdTimestamp = System.currentTimeMillis()
        )
        groupRoomDao.upsertGroupRoom(room)
    }

    suspend fun exportBackup(password: String? = null): BackupHistoryEntity {
        return backupManager.createExportBackup(password)
    }

    suspend fun importBackup(filePath: String, password: String? = null): Int {
        return backupManager.restoreFromBackupFile(filePath, password)
    }
}
