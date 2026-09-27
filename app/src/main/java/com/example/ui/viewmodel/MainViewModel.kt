package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.GroupRoomEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity
import com.example.data.p2p.P2PManager
import com.example.data.preferences.ThemePreferences
import com.example.data.preferences.UIStyleTheme
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DISCOVERY,
    CONTACTS,
    GROUPS,
    BACKUPS,
    SETTINGS
}

data class UiState(
    val selectedTab: ScreenTab = ScreenTab.DISCOVERY,
    val activeChatId: String? = null,
    val activeChatPeer: PeerEntity? = null,
    val activeChatGroup: GroupRoomEntity? = null,
    val isEditingLocalName: Boolean = false,
    val isCreatingGroupRoom: Boolean = false,
    val isExportingBackup: Boolean = false,
    val showFingerprintDialog: Boolean = false,
    val showQrGenerator: Boolean = false,
    val showQrScanner: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val themePreferences = ThemePreferences(application)
    private val p2pManager = P2PManager(application, db.peerDao(), db.messageDao(), db.groupRoomDao())
    private val backupManager = BackupManager(application, db.peerDao(), db.messageDao(), db.groupRoomDao(), db.backupDao())

    val repository = ChatRepository(
        peerDao = db.peerDao(),
        messageDao = db.messageDao(),
        groupRoomDao = db.groupRoomDao(),
        backupDao = db.backupDao(),
        p2pManager = p2pManager,
        backupManager = backupManager,
        themePreferences = themePreferences
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val peersState: StateFlow<List<PeerEntity>> = repository.allPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactsState: StateFlow<List<PeerEntity>> = repository.savedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupRoomsState: StateFlow<List<GroupRoomEntity>> = repository.groupRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val p2pState = repository.p2pState

    val backupHistoryState: StateFlow<List<BackupHistoryEntity>> = repository.backupHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val themeStyle: StateFlow<UIStyleTheme> = repository.themeStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UIStyleTheme.GLASSMORPHISM)

    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val localDeviceName: StateFlow<String> = repository.localDeviceName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Speaker-Node-01")

    val isDiscoverable: StateFlow<Boolean> = repository.isDiscoverable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun selectTab(tab: ScreenTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, activeChatId = null)
    }

    fun openDirectChat(peer: PeerEntity) {
        p2pManager.connectToPeer(peer.macAddress)
        _uiState.value = _uiState.value.copy(
            activeChatId = peer.macAddress,
            activeChatPeer = peer,
            activeChatGroup = null
        )
    }

    fun openGroupChat(group: GroupRoomEntity) {
        _uiState.value = _uiState.value.copy(
            activeChatId = group.id,
            activeChatPeer = null,
            activeChatGroup = group
        )
    }

    fun closeChat() {
        _uiState.value = _uiState.value.copy(
            activeChatId = null,
            activeChatPeer = null,
            activeChatGroup = null
        )
    }

    fun sendTextMessage(text: String) {
        val chatId = _uiState.value.activeChatId ?: return
        val isGroup = _uiState.value.activeChatGroup != null
        p2pManager.sendTextMessage(
            recipientId = chatId,
            isGroup = isGroup,
            text = text,
            messageType = "TEXT"
        )
    }

    fun sendFileTransfer(fileName: String, fileSize: Long, fileType: String) {
        val chatId = _uiState.value.activeChatId ?: return
        val isGroup = _uiState.value.activeChatGroup != null
        p2pManager.sendTextMessage(
            recipientId = chatId,
            isGroup = isGroup,
            text = "Shared File: $fileName",
            messageType = fileType,
            fileName = fileName,
            fileSize = fileSize
        )
    }

    fun updatePeerAlias(macAddress: String, alias: String?) {
        viewModelScope.launch {
            repository.updateCustomAlias(macAddress, alias)
        }
    }

    fun toggleSavedPeer(macAddress: String, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSavedStatus(macAddress, isSaved)
        }
    }

    fun updateLocalDeviceName(newName: String) {
        viewModelScope.launch {
            repository.updateLocalDeviceName(newName)
            _uiState.value = _uiState.value.copy(isEditingLocalName = false)
        }
    }

    fun setThemeStyle(style: UIStyleTheme) {
        viewModelScope.launch {
            repository.setThemeStyle(style)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(isDark)
        }
    }

    fun startScan() {
        p2pManager.startScan()
    }

    fun toggleDiscoverable(discoverable: Boolean) {
        viewModelScope.launch {
            themePreferences.setDiscoverable(discoverable)
            p2pManager.setDiscoverable(discoverable)
        }
    }

    fun createGroupRoom(name: String, description: String) {
        viewModelScope.launch {
            repository.createGroupRoom(name, description)
            _uiState.value = _uiState.value.copy(isCreatingGroupRoom = false)
        }
    }

    fun exportBackup(password: String? = null) {
        viewModelScope.launch {
            val backup = repository.exportBackup(password)
            _uiState.value = _uiState.value.copy(
                isExportingBackup = false,
                statusMessage = "Backup exported: ${backup.fileName}"
            )
        }
    }

    fun importBackup(filePath: String, password: String? = null) {
        viewModelScope.launch {
            val count = repository.importBackup(filePath, password)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Restored $count messages from backup"
            )
        }
    }

    fun setShowEditNameDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isEditingLocalName = show)
    }

    fun setShowCreateGroupDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isCreatingGroupRoom = show)
    }

    fun setShowExportBackupDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(isExportingBackup = show)
    }

    fun setShowFingerprintDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFingerprintDialog = show)
    }

    fun setShowQrGenerator(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQrGenerator = show)
    }

    fun setShowQrScanner(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQrScanner = show)
    }

    fun handleScannedPeer(peer: PeerEntity) {
        viewModelScope.launch {
            db.peerDao().upsertPeer(peer)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Exchanged keys & connected to ${peer.getDisplayName()}"
            )
            openDirectChat(peer)
        }
    }

    fun getMessagesForChat(chatId: String) = repository.getMessagesForChat(chatId)
}
