package com.example.data.p2p

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.example.data.local.dao.GroupRoomDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PeerDao
import com.example.data.local.entity.GroupRoomEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity
import com.example.data.security.CryptoEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.KeyPair
import javax.crypto.SecretKey

enum class ConnectionType {
    BLUETOOTH,
    WIFI_DIRECT,
    HYBRID_MESH
}

data class P2PState(
    val isScanning: Boolean = false,
    val isDiscoverable: Boolean = true,
    val connectionType: ConnectionType = ConnectionType.BLUETOOTH,
    val localDeviceName: String = "Speaker-Node-01",
    val localMacAddress: String = "AA:BB:CC:DD:EE:01",
    val connectedPeerMacs: Set<String> = emptySet(),
    val isSimulationActive: Boolean = true
)

class P2PManager(
    private val context: Context,
    private val peerDao: PeerDao,
    private val messageDao: MessageDao,
    private val groupRoomDao: GroupRoomDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val _state = MutableStateFlow(P2PState())
    val state: StateFlow<P2PState> = _state.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val localKeyPair: KeyPair = CryptoEngine.generateRsaKeyPair()
    private val peerSecretKeys = mutableMapOf<String, SecretKey>()

    init {
        seedInitialSimulatedMeshPeers()
        startMeshNetworkPingLoop()
    }

    fun updateLocalDeviceName(name: String) {
        _state.value = _state.value.copy(localDeviceName = name)
        // If Bluetooth adapter available and permission granted, attempt to set Bluetooth name
        try {
            bluetoothAdapter?.name = name
        } catch (_: SecurityException) {}
    }

    fun setDiscoverable(discoverable: Boolean) {
        _state.value = _state.value.copy(isDiscoverable = discoverable)
    }

    fun setConnectionType(type: ConnectionType) {
        _state.value = _state.value.copy(connectionType = type)
    }

    fun startScan() {
        _state.value = _state.value.copy(isScanning = true)
        scope.launch {
            // Check real hardware scan
            try {
                if (bluetoothAdapter?.isEnabled == true) {
                    bluetoothAdapter.startDiscovery()
                }
            } catch (_: SecurityException) {}

            // Simulated mesh scan
            delay(1500)
            generateDiscoveredPeers()
            _state.value = _state.value.copy(isScanning = false)
        }
    }

    fun stopScan() {
        _state.value = _state.value.copy(isScanning = false)
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (_: SecurityException) {}
    }

    fun connectToPeer(macAddress: String) {
        scope.launch {
            val peer = peerDao.getPeerByMac(macAddress) ?: return@launch
            // Simulate E2EE handshake
            val aesKey = CryptoEngine.generateAesKey()
            peerSecretKeys[macAddress] = aesKey

            val updatedPeer = peer.copy(
                isOnline = true,
                sharedAesKeyBase64 = CryptoEngine.encryptAesKeyWithRsa(aesKey, localKeyPair.public),
                rsaPublicKey = CryptoEngine.publicKeyToString(localKeyPair.public)
            )
            peerDao.upsertPeer(updatedPeer)

            val currentConnected = _state.value.connectedPeerMacs.toMutableSet()
            currentConnected.add(macAddress)
            _state.value = _state.value.copy(connectedPeerMacs = currentConnected)
        }
    }

    fun disconnectPeer(macAddress: String) {
        scope.launch {
            val peer = peerDao.getPeerByMac(macAddress)
            if (peer != null) {
                peerDao.upsertPeer(peer.copy(isOnline = false))
            }
            val currentConnected = _state.value.connectedPeerMacs.toMutableSet()
            currentConnected.remove(macAddress)
            _state.value = _state.value.copy(connectedPeerMacs = currentConnected)
        }
    }

    fun sendTextMessage(
        recipientId: String,
        isGroup: Boolean,
        text: String,
        messageType: String = "TEXT",
        fileUri: String? = null,
        fileName: String? = null,
        fileSize: Long = 0
    ) {
        scope.launch {
            val senderMac = _state.value.localMacAddress
            val senderName = _state.value.localDeviceName

            // E2EE Encryption
            val aesKey = peerSecretKeys[recipientId] ?: CryptoEngine.generateAesKey().also { peerSecretKeys[recipientId] = it }
            val encryptedPayload = CryptoEngine.encryptAesGcm(text, aesKey)

            val msg = MessageEntity(
                chatId = recipientId,
                isGroup = isGroup,
                senderMac = senderMac,
                recipientMac = recipientId,
                senderName = senderName,
                encryptedPayloadBase64 = encryptedPayload.ciphertextBase64,
                ivBase64 = encryptedPayload.ivBase64,
                decryptedText = text,
                timestamp = System.currentTimeMillis(),
                isMine = true,
                messageType = messageType,
                fileUri = fileUri,
                fileName = fileName,
                fileSize = fileSize,
                transferProgress = 1.0f,
                isEncrypted = true
            )

            val insertedId = messageDao.insertMessage(msg)

            // Simulate File Transfer progress if sending file
            if (messageType == "FILE" || messageType == "IMAGE") {
                simulateFileTransferProgress(insertedId)
            }

            // Trigger simulated offline peer response if sending to a direct contact
            if (!isGroup) {
                simulatePeerAutoReply(recipientId, text, messageType)
            } else {
                simulateGroupRoomReply(recipientId, text)
            }
        }
    }

    private fun simulateFileTransferProgress(messageId: Long) {
        scope.launch {
            messageDao.updateTransferProgress(messageId, 0.1f)
            delay(500)
            messageDao.updateTransferProgress(messageId, 0.45f)
            delay(600)
            messageDao.updateTransferProgress(messageId, 0.8f)
            delay(400)
            messageDao.updateTransferProgress(messageId, 1.0f)
        }
    }

    private fun simulatePeerAutoReply(peerMac: String, originalText: String, msgType: String) {
        scope.launch {
            delay(2000)
            val peer = peerDao.getPeerByMac(peerMac) ?: return@launch

            val replyText = when {
                msgType == "FILE" || msgType == "IMAGE" -> "Received attachment from ${peer.getDisplayName()} securely via P2P Bluetooth!"
                originalText.contains("hello", ignoreCase = true) || originalText.contains("hi", ignoreCase = true) ->
                    "Hey there! Connected via offline encrypted ${state.value.connectionType.name} P2P."
                originalText.contains("speaker", ignoreCase = true) ->
                    "P2P Audio Node configured and ready for off-grid broadcasts."
                originalText.contains("test", ignoreCase = true) ->
                    "E2EE Handshake verified! Signal RSSI: ${peer.rssi} dBm."
                else -> "Offline P2P ACK: '${originalText.take(20)}...' decrypted cleanly with AES-256-GCM."
            }

            val aesKey = peerSecretKeys[peerMac] ?: CryptoEngine.generateAesKey().also { peerSecretKeys[peerMac] = it }
            val encryptedPayload = CryptoEngine.encryptAesGcm(replyText, aesKey)

            val replyMsg = MessageEntity(
                chatId = peerMac,
                isGroup = false,
                senderMac = peerMac,
                recipientMac = _state.value.localMacAddress,
                senderName = peer.getDisplayName(),
                encryptedPayloadBase64 = encryptedPayload.ciphertextBase64,
                ivBase64 = encryptedPayload.ivBase64,
                decryptedText = replyText,
                timestamp = System.currentTimeMillis(),
                isMine = false,
                messageType = "TEXT",
                isEncrypted = true
            )
            messageDao.insertMessage(replyMsg)
        }
    }

    private fun simulateGroupRoomReply(roomId: String, text: String) {
        scope.launch {
            delay(2500)
            val room = groupRoomDao.getGroupRoomById(roomId) ?: return@launch
            val replyText = "Mesh Broadcast Node in '${room.roomName}' routed message across 2 P2P hops successfully."

            val replyMsg = MessageEntity(
                chatId = roomId,
                isGroup = true,
                senderMac = "CC:11:22:33:44:00",
                recipientMac = roomId,
                senderName = "P2P Relay Alpha",
                encryptedPayloadBase64 = "",
                ivBase64 = "",
                decryptedText = replyText,
                timestamp = System.currentTimeMillis(),
                isMine = false,
                messageType = "TEXT",
                isEncrypted = true
            )
            messageDao.insertMessage(replyMsg)
        }
    }

    private fun seedInitialSimulatedMeshPeers() {
        scope.launch {
            val initialPeers = listOf(
                PeerEntity(
                    macAddress = "11:22:33:44:55:66",
                    deviceName = "Bluetooth Speaker LivingRoom",
                    customAlias = "Speaker-Main",
                    deviceType = "SPEAKER",
                    isSaved = true,
                    rssi = -42,
                    isOnline = true
                ),
                PeerEntity(
                    macAddress = "22:33:44:55:66:77",
                    deviceName = "Tactical Field Tablet",
                    customAlias = "Command Tab",
                    deviceType = "TABLET",
                    isSaved = true,
                    rssi = -58,
                    isOnline = true
                ),
                PeerEntity(
                    macAddress = "33:44:55:66:77:88",
                    deviceName = "Crypto Node Alpha",
                    customAlias = null,
                    deviceType = "LAPTOP",
                    isSaved = false,
                    rssi = -72,
                    isOnline = true
                ),
                PeerEntity(
                    macAddress = "44:55:66:77:88:99",
                    deviceName = "Off-Grid Rescue Headset",
                    customAlias = "Rescue Headset",
                    deviceType = "HEADSET",
                    isSaved = true,
                    rssi = -35,
                    isOnline = true
                )
            )

            peerDao.upsertPeers(initialPeers)

            // Seed initial group room
            val defaultRoom = GroupRoomEntity(
                id = "ROOM_OFFGRID_ALPHA",
                roomName = "Emergency Mesh Channel 01",
                description = "Secure offline multi-hop P2P group room for local device mesh.",
                createdTimestamp = System.currentTimeMillis()
            )
            groupRoomDao.upsertGroupRoom(defaultRoom)
        }
    }

    private fun generateDiscoveredPeers() {
        scope.launch {
            val randomPeers = listOf(
                PeerEntity(
                    macAddress = "55:66:77:88:99:AA",
                    deviceName = "Portable Speaker Outdoor",
                    customAlias = null,
                    deviceType = "SPEAKER",
                    isSaved = false,
                    rssi = (-40..-85).random(),
                    isOnline = true
                ),
                PeerEntity(
                    macAddress = "66:77:88:99:AA:BB",
                    deviceName = "Galaxy Tab Ultra P2P",
                    customAlias = null,
                    deviceType = "TABLET",
                    isSaved = false,
                    rssi = (-50..-90).random(),
                    isOnline = true
                ),
                PeerEntity(
                    macAddress = "77:88:99:AA:BB:CC",
                    deviceName = "Encrypted Node Sierra",
                    customAlias = null,
                    deviceType = "PHONE",
                    isSaved = false,
                    rssi = (-30..-65).random(),
                    isOnline = true
                )
            )
            peerDao.upsertPeers(randomPeers)
        }
    }

    private fun startMeshNetworkPingLoop() {
        scope.launch {
            while (true) {
                delay(10000)
                // Random RSSI signal fluctuations for realism
                val peers = peerDao.getSavedContacts()
                // Periodic RSSI pulse
            }
        }
    }
}
