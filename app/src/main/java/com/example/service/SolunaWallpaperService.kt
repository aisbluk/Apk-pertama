package com.example.service

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.example.data.SolunaPreferences
import com.example.model.SolunaConfig
import com.example.rendering.SolunaCanvasRenderer
import com.example.state.DeviceStatusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.sin

class SolunaWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return SolunaEngine()
    }

    inner class SolunaEngine : Engine() {
        private val renderer = SolunaCanvasRenderer()
        private val prefs = SolunaPreferences.getInstance(this@SolunaWallpaperService)
        private val statusManager = DeviceStatusManager.getInstance(this@SolunaWallpaperService)

        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false
        private var surfaceWidth = 1080f
        private var surfaceHeight = 2400f
        private var density = resources.displayMetrics.density

        private var activeConfig: SolunaConfig = prefs.getConfig()
        private val scope = CoroutineScope(Dispatchers.Main + Job())

        private var pulsePhase = 0.0f
        private var vibrator: Vibrator? = null

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (isVisible) {
                    drawFrame()
                    val fps = statusManager.getTargetFps(activeConfig)
                    val isMusicPlaying = statusManager.musicState.value.isPlaying
                    val isPowerSave = statusManager.isPowerSavingActive(activeConfig)

                    // Smart animation throttle:
                    // If no music is playing and device is idle in power save, throttle to 5 FPS to maximize battery.
                    val effectiveFps = if (isPowerSave && !isMusicPlaying) {
                        10
                    } else {
                        fps
                    }
                    val frameIntervalMs = (1000L / effectiveFps.coerceIn(5, 60))
                    handler.postDelayed(this, frameIntervalMs)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            prefs.configFlow.onEach { newConfig ->
                activeConfig = newConfig
                if (isVisible) drawFrame()
            }.launchIn(scope)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                handler.removeCallbacks(drawRunnable)
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width.toFloat()
            surfaceHeight = height.toFloat()
            density = resources.displayMetrics.density
            if (isVisible) drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunnable)
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            if (event?.action == MotionEvent.ACTION_DOWN) {
                val hit = renderer.isTapInsideIsland(
                    event.x,
                    event.y,
                    surfaceWidth,
                    density,
                    activeConfig
                )
                if (hit) {
                    // Trigger gentle haptic tap
                    triggerHapticFeedback()
                    // Toggle music state or toggle island
                    statusManager.toggleMusicPlayback()
                    drawFrame()
                }
            }
        }

        private fun triggerHapticFeedback() {
            vibrator?.let { vib ->
                if (vib.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(35)
                    }
                }
            }
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            var canvas: android.graphics.Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    pulsePhase += 0.05f
                    val pulse = (sin(pulsePhase) + 1.0f) * 0.5f

                    val battery = statusManager.batteryState.value
                    val music = statusManager.musicState.value
                    val isPowerSave = statusManager.isPowerSavingActive(activeConfig)

                    renderer.render(
                        canvas = canvas,
                        width = surfaceWidth,
                        height = surfaceHeight,
                        density = density,
                        config = activeConfig,
                        battery = battery,
                        music = music,
                        isPowerSaving = isPowerSave,
                        animPulse = pulse
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunnable)
            scope.cancel()
        }
    }
}
