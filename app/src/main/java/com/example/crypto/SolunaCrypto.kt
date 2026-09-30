package com.example.crypto

import android.util.Base64
import com.example.model.SolunaConfig
import org.json.JSONObject
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SolunaCrypto {
    private const val PREFIX = "SOLUNA-SYNC-v1:"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_DERIVATION_ALGO = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    fun configToJson(config: SolunaConfig): String {
        val json = JSONObject()
        json.put("sunSizeDp", config.sunSizeDp.toDouble())
        json.put("moonSizeDp", config.moonSizeDp.toDouble())
        json.put("sunGlowIntensity", config.sunGlowIntensity.toDouble())
        json.put("moonGlowIntensity", config.moonGlowIntensity.toDouble())
        json.put("celestialArcHeight", config.celestialArcHeight.toDouble())
        json.put("islandWidthDp", config.islandWidthDp.toDouble())
        json.put("islandHeightDp", config.islandHeightDp.toDouble())
        json.put("islandYOffsetDp", config.islandYOffsetDp.toDouble())
        json.put("enableDynamicIsland", config.enableDynamicIsland)
        json.put("enableBatteryWidget", config.enableBatteryWidget)
        json.put("enableMusicWidget", config.enableMusicWidget)
        json.put("enableWaveform", config.enableWaveform)
        json.put("smartPowerSaveEnabled", config.smartPowerSaveEnabled)
        json.put("powerSaveThreshold", config.powerSaveThreshold)
        json.put("targetFpsNormal", config.targetFpsNormal)
        json.put("targetFpsPowerSave", config.targetFpsPowerSave)
        json.put("autoDayNightSchedule", config.autoDayNightSchedule)
        json.put("sunriseHour", config.sunriseHour)
        json.put("sunriseMinute", config.sunriseMinute)
        json.put("sunsetHour", config.sunsetHour)
        json.put("sunsetMinute", config.sunsetMinute)
        json.put("moonPhase", config.moonPhase.toDouble())
        json.put("colorPreset", config.colorPreset)
        return json.toString()
    }

    fun jsonToConfig(jsonStr: String): SolunaConfig {
        val json = JSONObject(jsonStr)
        return SolunaConfig(
            sunSizeDp = json.optDouble("sunSizeDp", 110.0).toFloat(),
            moonSizeDp = json.optDouble("moonSizeDp", 95.0).toFloat(),
            sunGlowIntensity = json.optDouble("sunGlowIntensity", 0.75).toFloat(),
            moonGlowIntensity = json.optDouble("moonGlowIntensity", 0.6).toFloat(),
            celestialArcHeight = json.optDouble("celestialArcHeight", 0.35).toFloat(),
            islandWidthDp = json.optDouble("islandWidthDp", 190.0).toFloat(),
            islandHeightDp = json.optDouble("islandHeightDp", 36.0).toFloat(),
            islandYOffsetDp = json.optDouble("islandYOffsetDp", 16.0).toFloat(),
            enableDynamicIsland = json.optBoolean("enableDynamicIsland", true),
            enableBatteryWidget = json.optBoolean("enableBatteryWidget", true),
            enableMusicWidget = json.optBoolean("enableMusicWidget", true),
            enableWaveform = json.optBoolean("enableWaveform", true),
            smartPowerSaveEnabled = json.optBoolean("smartPowerSaveEnabled", true),
            powerSaveThreshold = json.optInt("powerSaveThreshold", 20),
            targetFpsNormal = json.optInt("targetFpsNormal", 60),
            targetFpsPowerSave = json.optInt("targetFpsPowerSave", 24),
            autoDayNightSchedule = json.optBoolean("autoDayNightSchedule", true),
            sunriseHour = json.optInt("sunriseHour", 6),
            sunriseMinute = json.optInt("sunriseMinute", 0),
            sunsetHour = json.optInt("sunsetHour", 18),
            sunsetMinute = json.optInt("sunsetMinute", 0),
            moonPhase = json.optDouble("moonPhase", 0.55).toFloat(),
            colorPreset = json.optString("colorPreset", "AUREATE")
        )
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun decodeBase64(str: String): ByteArray {
        return try {
            android.util.Base64.decode(str, android.util.Base64.DEFAULT)
        } catch (_: Throwable) {
            java.util.Base64.getDecoder().decode(str)
        }
    }

    /**
     * Encrypts a SolunaConfig using AES-256-GCM and PBKDF2WithHmacSHA256 key derivation.
     * Returns a tamper-proof Base64 bundle with integrity authentication tag.
     */
    fun encrypt(config: SolunaConfig, passphrase: String): String {
        require(passphrase.isNotBlank()) { "Passphrase tidak boleh kosong" }

        val plainText = configToJson(config).toByteArray(Charsets.UTF_8)

        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { secureRandom.nextBytes(it) }

        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO)
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val cipherTextWithTag = cipher.doFinal(plainText)

        val buffer = ByteBuffer.allocate(salt.size + iv.size + cipherTextWithTag.size)
        buffer.put(salt)
        buffer.put(iv)
        buffer.put(cipherTextWithTag)

        val base64Payload = encodeBase64(buffer.array())
        return "$PREFIX$base64Payload"
    }

    /**
     * Decrypts and authenticates an end-to-end encrypted bundle.
     * Fails if passphrase is wrong or if ciphertext has been tampered with.
     */
    fun decrypt(encryptedPayload: String, passphrase: String): Result<SolunaConfig> {
        return runCatching {
            require(passphrase.isNotBlank()) { "Passphrase tidak boleh kosong" }
            val trimmed = encryptedPayload.trim()
            require(trimmed.startsWith(PREFIX)) { "Format sync bundle tidak valid (harus diawali $PREFIX)" }

            val rawBase64 = trimmed.substring(PREFIX.length)
            val allBytes = decodeBase64(rawBase64)

            require(allBytes.size > (SALT_LENGTH + IV_LENGTH + 16)) { "Ukuran data payload tidak valid" }

            val buffer = ByteBuffer.wrap(allBytes)
            val salt = ByteArray(SALT_LENGTH)
            buffer.get(salt)

            val iv = ByteArray(IV_LENGTH)
            buffer.get(iv)

            val cipherTextWithTag = ByteArray(buffer.remaining())
            buffer.get(cipherTextWithTag)

            val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO)
            val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
            val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val decryptedBytes = cipher.doFinal(cipherTextWithTag)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            jsonToConfig(jsonString)
        }
    }

    fun generateRandomSyncKey(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val sb = StringBuilder()
        for (i in 0 until 12) {
            if (i > 0 && i % 4 == 0) sb.append("-")
            sb.append(chars[secureRandom.nextInt(chars.length)])
        }
        return sb.toString()
    }
}
