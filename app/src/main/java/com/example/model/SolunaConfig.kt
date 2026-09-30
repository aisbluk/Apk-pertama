package com.example.model

data class SolunaConfig(
    val sunSizeDp: Float = 110f,
    val moonSizeDp: Float = 95f,
    val sunGlowIntensity: Float = 0.75f,
    val moonGlowIntensity: Float = 0.6f,
    val celestialArcHeight: Float = 0.35f,
    val islandWidthDp: Float = 190f,
    val islandHeightDp: Float = 36f,
    val islandYOffsetDp: Float = 16f,
    val enableDynamicIsland: Boolean = true,
    val enableBatteryWidget: Boolean = true,
    val enableMusicWidget: Boolean = true,
    val enableWaveform: Boolean = true,
    val smartPowerSaveEnabled: Boolean = true,
    val powerSaveThreshold: Int = 20,
    val targetFpsNormal: Int = 60,
    val targetFpsPowerSave: Int = 24,
    val autoDayNightSchedule: Boolean = true,
    val sunriseHour: Int = 6,
    val sunriseMinute: Int = 0,
    val sunsetHour: Int = 18,
    val sunsetMinute: Int = 0,
    val moonPhase: Float = 0.55f, // 0.0: New Moon, 0.25: First Quarter, 0.5: Full Moon, 0.75: Waning
    val colorPreset: String = "AUREATE",
    val simulationMode: Boolean = false,
    val simulatedHour: Float = 12.0f
) {
    enum class Preset(val title: String, val desc: String) {
        MINIMALIST("Minimalist Murni", "Matahari & bulan ramping 75dp, island ringkas"),
        BALANCED("Keseimbangan Zen", "Proporsi harmonis 110dp / 95dp, glow lembut"),
        GRAND_SOLAR("Grand Celestial", "Matahari dramatis 165dp, korona tebal berkilau"),
        OLED_STEALTH("OLED Stealth", "Siluet monokrom kontras tinggi, hemat daya maksimal")
    }

    companion object {
        fun fromPreset(preset: Preset): SolunaConfig {
            return when (preset) {
                Preset.MINIMALIST -> SolunaConfig(
                    sunSizeDp = 75f,
                    moonSizeDp = 65f,
                    sunGlowIntensity = 0.4f,
                    moonGlowIntensity = 0.35f,
                    islandWidthDp = 160f,
                    islandHeightDp = 32f,
                    celestialArcHeight = 0.32f
                )
                Preset.BALANCED -> SolunaConfig(
                    sunSizeDp = 110f,
                    moonSizeDp = 95f,
                    sunGlowIntensity = 0.75f,
                    moonGlowIntensity = 0.6f,
                    islandWidthDp = 190f,
                    islandHeightDp = 36f,
                    celestialArcHeight = 0.35f
                )
                Preset.GRAND_SOLAR -> SolunaConfig(
                    sunSizeDp = 165f,
                    moonSizeDp = 140f,
                    sunGlowIntensity = 0.95f,
                    moonGlowIntensity = 0.85f,
                    islandWidthDp = 210f,
                    islandHeightDp = 40f,
                    celestialArcHeight = 0.42f
                )
                Preset.OLED_STEALTH -> SolunaConfig(
                    sunSizeDp = 85f,
                    moonSizeDp = 75f,
                    sunGlowIntensity = 0.2f,
                    moonGlowIntensity = 0.2f,
                    islandWidthDp = 175f,
                    islandHeightDp = 34f,
                    smartPowerSaveEnabled = true,
                    targetFpsPowerSave = 15,
                    colorPreset = "MONO"
                )
            }
        }
    }
}
