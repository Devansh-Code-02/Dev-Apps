package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.UIStyleTheme
import com.example.ui.components.QrCodeGeneratorDialog
import com.example.ui.components.QrCodeScannerDialog
import com.example.ui.screens.BackupsScreen
import com.example.ui.screens.ContactListScreen
import com.example.ui.screens.DirectChatScreen
import com.example.ui.screens.DiscoveryScreen
import com.example.ui.screens.GroupRoomsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBgStart
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.startScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val peers by viewModel.peersState.collectAsStateWithLifecycle()
            val contacts by viewModel.contactsState.collectAsStateWithLifecycle()
            val groups by viewModel.groupRoomsState.collectAsStateWithLifecycle()
            val p2pState by viewModel.p2pState.collectAsStateWithLifecycle()
            val backups by viewModel.backupHistoryState.collectAsStateWithLifecycle()
            val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()
            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val localName by viewModel.localDeviceName.collectAsStateWithLifecycle()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.statusMessage) {
                uiState.statusMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                }
            }

            AppTheme(
                styleTheme = themeStyle,
                darkTheme = isDark
            ) {
                if (uiState.activeChatId != null) {
                    val messages by viewModel.getMessagesForChat(uiState.activeChatId!!)
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    val title = uiState.activeChatPeer?.getDisplayName()
                        ?: uiState.activeChatGroup?.roomName
                        ?: "P2P Encrypted Session"

                    DirectChatScreen(
                        chatTitle = title,
                        chatId = uiState.activeChatId!!,
                        messages = messages,
                        themeStyle = themeStyle,
                        isDark = isDark,
                        onBack = { viewModel.closeChat() },
                        onSendMessage = { text -> viewModel.sendTextMessage(text) },
                        onSendFile = { name, size, type -> viewModel.sendFileTransfer(name, size, type) }
                    )
                } else {
                    MainNavigationContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        peers = peers,
                        contacts = contacts,
                        groups = groups,
                        p2pState = p2pState,
                        backups = backups,
                        themeStyle = themeStyle,
                        isDark = isDark,
                        localName = localName,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContent(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.UiState,
    peers: List<com.example.data.local.entity.PeerEntity>,
    contacts: List<com.example.data.local.entity.PeerEntity>,
    groups: List<com.example.data.local.entity.GroupRoomEntity>,
    p2pState: com.example.data.p2p.P2PState,
    backups: List<com.example.data.local.entity.BackupHistoryEntity>,
    themeStyle: UIStyleTheme,
    isDark: Boolean,
    localName: String,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.selectedTab) {
                            ScreenTab.DISCOVERY -> "Discovery Radar"
                            ScreenTab.CONTACTS -> "P2P Contacts"
                            ScreenTab.GROUPS -> "Group Rooms"
                            ScreenTab.BACKUPS -> "History Backups"
                            ScreenTab.SETTINGS -> "P2P Settings"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBgStart)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkBgStart,
                contentColor = Color.White,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == ScreenTab.DISCOVERY,
                    onClick = { viewModel.selectTab(ScreenTab.DISCOVERY) },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Discovery") },
                    label = { Text("Radar") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == ScreenTab.CONTACTS,
                    onClick = { viewModel.selectTab(ScreenTab.CONTACTS) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Contacts") },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == ScreenTab.GROUPS,
                    onClick = { viewModel.selectTab(ScreenTab.GROUPS) },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == ScreenTab.BACKUPS,
                    onClick = { viewModel.selectTab(ScreenTab.BACKUPS) },
                    icon = { Icon(Icons.Default.Backup, contentDescription = "Backups") },
                    label = { Text("Backups") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = uiState.selectedTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.selectTab(ScreenTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBgStart
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                ScreenTab.DISCOVERY -> DiscoveryScreen(
                    peers = peers,
                    p2pState = p2pState,
                    themeStyle = themeStyle,
                    isDark = isDark,
                    localDeviceName = localName,
                    onStartScan = { viewModel.startScan() },
                    onConnectPeer = { peer -> viewModel.openDirectChat(peer) },
                    onToggleSaveContact = { mac, isSaved -> viewModel.toggleSavedPeer(mac, isSaved) },
                    onEditDeviceName = { viewModel.setShowEditNameDialog(true) },
                    onToggleDiscoverable = { viewModel.toggleDiscoverable(it) },
                    onShowQrGenerator = { viewModel.setShowQrGenerator(true) },
                    onShowQrScanner = { viewModel.setShowQrScanner(true) }
                )

                ScreenTab.CONTACTS -> ContactListScreen(
                    contacts = contacts,
                    themeStyle = themeStyle,
                    isDark = isDark,
                    onOpenChat = { contact -> viewModel.openDirectChat(contact) },
                    onUpdateAlias = { mac, alias -> viewModel.updatePeerAlias(mac, alias) }
                )

                ScreenTab.GROUPS -> GroupRoomsScreen(
                    groups = groups,
                    themeStyle = themeStyle,
                    isDark = isDark,
                    isCreatingGroup = uiState.isCreatingGroupRoom,
                    onOpenGroupChat = { group -> viewModel.openGroupChat(group) },
                    onStartCreateGroup = { viewModel.setShowCreateGroupDialog(true) },
                    onConfirmCreateGroup = { name, desc -> viewModel.createGroupRoom(name, desc) },
                    onDismissCreateGroup = { viewModel.setShowCreateGroupDialog(false) }
                )

                ScreenTab.BACKUPS -> BackupsScreen(
                    backups = backups,
                    themeStyle = themeStyle,
                    isDark = isDark,
                    isExportingBackup = uiState.isExportingBackup,
                    onStartExport = { viewModel.setShowExportBackupDialog(true) },
                    onConfirmExport = { pwd -> viewModel.exportBackup(pwd) },
                    onDismissExport = { viewModel.setShowExportBackupDialog(false) },
                    onRestoreBackup = { path -> viewModel.importBackup(path) }
                )

                ScreenTab.SETTINGS -> SettingsScreen(
                    themeStyle = themeStyle,
                    isDark = isDark,
                    localDeviceName = localName,
                    isDiscoverable = p2pState.isDiscoverable,
                    onSelectThemeStyle = { viewModel.setThemeStyle(it) },
                    onToggleDarkMode = { viewModel.setDarkMode(it) },
                    onUpdateDeviceName = { viewModel.updateLocalDeviceName(it) },
                    onToggleDiscoverable = { viewModel.toggleDiscoverable(it) },
                    isEditingName = uiState.isEditingLocalName,
                    onSetEditingName = { viewModel.setShowEditNameDialog(it) }
                )
            }
        }

        if (uiState.showQrGenerator) {
            QrCodeGeneratorDialog(
                localDeviceName = localName,
                onDismiss = { viewModel.setShowQrGenerator(false) }
            )
        }

        if (uiState.showQrScanner) {
            QrCodeScannerDialog(
                onDismiss = { viewModel.setShowQrScanner(false) },
                onScannedPeer = { peer -> viewModel.handleScannedPeer(peer) }
            )
        }
    }
}
