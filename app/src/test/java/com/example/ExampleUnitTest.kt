package com.example

import com.example.crypto.SolunaCrypto
import com.example.model.SolunaConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
    @Test
    fun testDefaultConfigIntegrity() {
        val config = SolunaConfig()
        assertEquals(110f, config.sunSizeDp, 0.01f)
        assertEquals(95f, config.moonSizeDp, 0.01f)
        assertTrue(config.enableDynamicIsland)
        assertTrue(config.smartPowerSaveEnabled)
    }

    @Test
    fun testPresetGeneration() {
        val minimalist = SolunaConfig.fromPreset(SolunaConfig.Preset.MINIMALIST)
        assertEquals(75f, minimalist.sunSizeDp, 0.01f)
        assertEquals(65f, minimalist.moonSizeDp, 0.01f)

        val grand = SolunaConfig.fromPreset(SolunaConfig.Preset.GRAND_SOLAR)
        assertEquals(165f, grand.sunSizeDp, 0.01f)
        assertEquals(140f, grand.moonSizeDp, 0.01f)
    }

    @Test
    fun testJsonSerializationCycle() {
        val original = SolunaConfig(
            sunSizeDp = 145f,
            moonSizeDp = 120f,
            sunGlowIntensity = 0.85f,
            moonGlowIntensity = 0.45f,
            islandWidthDp = 210f,
            islandHeightDp = 42f,
            islandYOffsetDp = 22f,
            smartPowerSaveEnabled = true,
            powerSaveThreshold = 25
        )
        val json = SolunaCrypto.configToJson(original)
        val deserialized = SolunaCrypto.jsonToConfig(json)

        assertEquals(original.sunSizeDp, deserialized.sunSizeDp, 0.01f)
        assertEquals(original.moonSizeDp, deserialized.moonSizeDp, 0.01f)
        assertEquals(original.sunGlowIntensity, deserialized.sunGlowIntensity, 0.01f)
        assertEquals(original.moonGlowIntensity, deserialized.moonGlowIntensity, 0.01f)
        assertEquals(original.islandWidthDp, deserialized.islandWidthDp, 0.01f)
        assertEquals(original.powerSaveThreshold, deserialized.powerSaveThreshold)
    }

    @Test
    fun testEndToEndEncryptionAndDecryption() {
        val original = SolunaConfig(
            sunSizeDp = 130f,
            moonSizeDp = 105f,
            smartPowerSaveEnabled = true,
            powerSaveThreshold = 30
        )
        val password = "SuperSecurePassword123!"

        val encryptedPayload = SolunaCrypto.encrypt(original, password)
        assertTrue(encryptedPayload.startsWith("SOLUNA-SYNC-v1:"))

        val decryptResult = SolunaCrypto.decrypt(encryptedPayload, password)
        assertTrue(decryptResult.isSuccess)

        val decrypted = decryptResult.getOrNull()
        assertNotNull(decrypted)
        assertEquals(original.sunSizeDp, decrypted!!.sunSizeDp, 0.01f)
        assertEquals(original.moonSizeDp, decrypted.moonSizeDp, 0.01f)
        assertEquals(original.powerSaveThreshold, decrypted.powerSaveThreshold)
    }

    @Test
    fun testDecryptionFailsWithWrongPassword() {
        val original = SolunaConfig()
        val correctPassword = "CorrectKey123"
        val wrongPassword = "WrongKey456"

        val encryptedPayload = SolunaCrypto.encrypt(original, correctPassword)
        val decryptResult = SolunaCrypto.decrypt(encryptedPayload, wrongPassword)

        assertFalse(decryptResult.isSuccess)
    }

    @Test
    fun testTamperedCiphertextIsRejectedByGCM() {
        val original = SolunaConfig()
        val password = "SecretPassphrase"

        val encryptedPayload = SolunaCrypto.encrypt(original, password)
        // Corrupt one character in the payload
        val corruptedPayload = if (encryptedPayload.endsWith("A")) {
            encryptedPayload.dropLast(1) + "B"
        } else {
            encryptedPayload.dropLast(1) + "A"
        }

        val decryptResult = SolunaCrypto.decrypt(corruptedPayload, password)
        assertFalse("Tampered payload must be rejected by AEAD authentication", decryptResult.isSuccess)
    }
}
