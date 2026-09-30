package com.example.state

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.example.model.SolunaConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class BatteryInfo(
    val level: Int = 85,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val temperatureCelsius: Float = 31.5f,
    val healthStatus: String = "Baik",
    val plugType: String = "Baterai",
    val voltageMv: Int = 4120
)

data class MusicInfo(
    val title: String = "Celestial Harmonies",
    val artist: String = "Soluna Resonance",
    val isPlaying: Boolean = true,
    val positionMs: Long = 48000,
    val durationMs: Long = 214000,
    val visualizerBands: List<Float> = List(16) { 0.2f }
)

class DeviceStatusManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _batteryState = MutableStateFlow(BatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    private val _musicState = MutableStateFlow(MusicInfo())
    val musicState: StateFlow<MusicInfo> = _musicState.asStateFlow()

    private var simulationBatteryOverride: BatteryInfo? = null

    private val playlist = listOf(
        Pair("Celestial Harmonies", "Soluna Resonance"),
        Pair("Midnight Eclipse", "Aether Strings"),
        Pair("Solar Flare Sonata", "Horizon Acoustic"),
        Pair("Lunar Reflections", "Nocturne Studio"),
        Pair("Aurora Borealis", "Ambient Glow")
    )
    private var playlistIndex = 0

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED && simulationBatteryOverride == null) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 80

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val plugType = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "Pengisi Daya AC"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Nirkabel"
                    else -> "Baterai"
                }

                val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300)
                val tempC = tempTenths / 10.0f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)

                val isSysPowerSave = powerManager?.isPowerSaveMode ?: false

                _batteryState.value = BatteryInfo(
                    level = pct,
                    isCharging = isCharging,
                    isPowerSaveMode = isSysPowerSave,
                    temperatureCelsius = tempC,
                    healthStatus = "Sangat Baik",
                    plugType = plugType,
                    voltageMv = voltage
                )
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        // Waveform generator loop
        scope.launch {
            var phase = 0.0
            while (isActive) {
                if (_musicState.value.isPlaying) {
                    phase += 0.15
                    val currentPos = (_musicState.value.positionMs + 100) % _musicState.value.durationMs
                    val bands = List(16) { index ->
                        val base = (sin(phase + index * 0.45).toFloat() + 1f) * 0.4f
                        val jitter = (Random.nextFloat() * 0.2f)
                        (base + jitter).coerceIn(0.08f, 1.0f)
                    }
                    _musicState.value = _musicState.value.copy(
                        positionMs = currentPos,
                        visualizerBands = bands
                    )
                } else {
                    val decayed = _musicState.value.visualizerBands.map { (it * 0.85f).coerceAtLeast(0.04f) }
                    _musicState.value = _musicState.value.copy(visualizerBands = decayed)
                }
                delay(80)
            }
        }
    }

    fun toggleMusicPlayback() {
        val current = _musicState.value
        _musicState.value = current.copy(isPlaying = !current.isPlaying)
    }

    fun nextTrack() {
        playlistIndex = (playlistIndex + 1) % playlist.size
        val (title, artist) = playlist[playlistIndex]
        _musicState.value = _musicState.value.copy(
            title = title,
            artist = artist,
            positionMs = 0,
            isPlaying = true
        )
    }

    fun prevTrack() {
        playlistIndex = if (playlistIndex - 1 < 0) playlist.size - 1 else playlistIndex - 1
        val (title, artist) = playlist[playlistIndex]
        _musicState.value = _musicState.value.copy(
            title = title,
            artist = artist,
            positionMs = 0,
            isPlaying = true
        )
    }

    fun simulateBattery(level: Int, charging: Boolean) {
        val simulated = BatteryInfo(
            level = level,
            isCharging = charging,
            isPowerSaveMode = level <= 20 && !charging,
            temperatureCelsius = if (charging) 36.2f else 29.8f,
            healthStatus = "Optimal",
            plugType = if (charging) "Pengisi Daya AC" else "Baterai",
            voltageMv = if (charging) 4250 else 3850
        )
        simulationBatteryOverride = simulated
        _batteryState.value = simulated
    }

    fun clearBatterySimulation() {
        simulationBatteryOverride = null
        // Re-read system
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        if (intent != null) {
            batteryReceiver.onReceive(context, intent)
        }
    }

    fun isPowerSavingActive(config: SolunaConfig): Boolean {
        if (!config.smartPowerSaveEnabled) return false
        val battery = _batteryState.value
        if (battery.isCharging) return false // No power saving needed while charging
        return battery.isPowerSaveMode || battery.level <= config.powerSaveThreshold
    }

    fun getTargetFps(config: SolunaConfig): Int {
        return if (isPowerSavingActive(config)) {
            config.targetFpsPowerSave
        } else {
            config.targetFpsNormal
        }
    }

    fun unregister() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var instance: DeviceStatusManager? = null

        fun getInstance(context: Context): DeviceStatusManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceStatusManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
