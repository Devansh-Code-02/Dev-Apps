package com.example.data.security

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoEngine {
    private const val RSA_ALGORITHM = "RSA"
    private const val AES_ALGORITHM = "AES"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH_BYTES = 12

    data class EncryptedPayload(
        val ciphertextBase64: String,
        val ivBase64: String,
        val isEncrypted: Boolean = true
    )

    fun generateRsaKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    fun generateAesKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGen.init(256)
        return keyGen.generateKey()
    }

    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    fun stringToPublicKey(base64PublicKey: String): PublicKey {
        val bytes = Base64.decode(base64PublicKey, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(bytes)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }

    fun encryptAesGcm(plainText: String, secretKey: SecretKey): EncryptedPayload {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return EncryptedPayload(
            ciphertextBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP),
            isEncrypted = true
        )
    }

    fun decryptAesGcm(payload: EncryptedPayload, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val cipherBytes = Base64.decode(payload.ciphertextBase64, Base64.NO_WRAP)
        val iv = Base64.decode(payload.ivBase64, Base64.NO_WRAP)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val plainBytes = cipher.doFinal(cipherBytes)
        return String(plainBytes, Charsets.UTF_8)
    }

    fun encryptAesKeyWithRsa(secretKey: SecretKey, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedKey = cipher.doFinal(secretKey.encoded)
        return Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
    }

    fun decryptAesKeyWithRsa(encryptedKeyBase64: String, privateKey: PrivateKey): SecretKey {
        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.NO_WRAP)
        val secretKeyBytes = cipher.doFinal(encryptedKey)
        return SecretKeySpec(secretKeyBytes, AES_ALGORITHM)
    }

    fun generateSecurityDigest(keyString: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyString.toByteArray())
        val hex = hash.joinToString("") { "%02X".format(it) }
        return hex.chunked(4).take(4).joinToString("-")
    }
}
