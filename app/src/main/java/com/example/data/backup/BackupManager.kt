package com.example.data.backup

import android.content.Context
import com.example.data.local.dao.BackupDao
import com.example.data.local.dao.GroupRoomDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.PeerDao
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PeerEntity
import com.example.data.security.CryptoEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.spec.SecretKeySpec

data class BackupDataContainer(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val peers: List<PeerEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList()
)

class BackupManager(
    private val context: Context,
    private val peerDao: PeerDao,
    private val messageDao: MessageDao,
    private val groupRoomDao: GroupRoomDao,
    private val backupDao: BackupDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupDataContainer::class.java)

    suspend fun createExportBackup(encryptionPassword: String? = null): BackupHistoryEntity = withContext(Dispatchers.IO) {
        val messages = messageDao.getAllMessagesList()
        val peers = peerDao.getSavedContactsList()

        val container = BackupDataContainer(
            exportTimestamp = System.currentTimeMillis(),
            appVersion = "1.0",
            peers = peers,
            messages = messages
        )

        val rawJson = adapter.toJson(container)

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val timestampStr = dateFormat.format(Date())
        val fileName = "p2p_chat_backup_$timestampStr.json"

        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) backupDir.mkdirs()

        val outputFile = File(backupDir, fileName)

        var isEncrypted = false
        if (!encryptionPassword.isNullOrBlank()) {
            val keyBytes = CryptoEngine.generateSecurityDigest(encryptionPassword).toByteArray(Charsets.UTF_8).take(32).toByteArray()
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val encryptedPayload = CryptoEngine.encryptAesGcm(rawJson, secretKey)
            outputFile.writeText("${encryptedPayload.ivBase64}:${encryptedPayload.ciphertextBase64}")
            isEncrypted = true
        } else {
            outputFile.writeText(rawJson)
        }

        val backupEntity = BackupHistoryEntity(
            fileName = fileName,
            filePath = outputFile.absolutePath,
            timestamp = System.currentTimeMillis(),
            itemCount = messages.size,
            fileSizeBytes = outputFile.length(),
            isEncrypted = isEncrypted
        )

        val id = backupDao.insertBackupHistory(backupEntity)
        backupEntity.copy(id = id)
    }

    suspend fun restoreFromBackupFile(filePath: String, password: String? = null): Int = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext 0

        val fileContent = file.readText()
        val jsonContent: String = if (fileContent.contains(":")) {
            val parts = fileContent.split(":")
            if (parts.size == 2 && !password.isNullOrBlank()) {
                val ivBase64 = parts[0]
                val ciphertextBase64 = parts[1]
                val keyBytes = CryptoEngine.generateSecurityDigest(password).toByteArray(Charsets.UTF_8).take(32).toByteArray()
                val secretKey = SecretKeySpec(keyBytes, "AES")
                CryptoEngine.decryptAesGcm(
                    CryptoEngine.EncryptedPayload(ciphertextBase64, ivBase64),
                    secretKey
                )
            } else {
                fileContent
            }
        } else {
            fileContent
        }

        val container = adapter.fromJson(jsonContent) ?: return@withContext 0

        if (container.peers.isNotEmpty()) {
            peerDao.upsertPeers(container.peers)
        }
        if (container.messages.isNotEmpty()) {
            messageDao.insertMessages(container.messages)
        }

        container.messages.size
    }
}
