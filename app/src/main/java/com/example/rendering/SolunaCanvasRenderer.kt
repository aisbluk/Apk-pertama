package com.example.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.model.SolunaConfig
import com.example.state.BatteryInfo
import com.example.state.MusicInfo
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class SolunaCanvasRenderer {

    // Paints
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val sunCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val sunCoronaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val moonDiscPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val moonHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val moonCraterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 120, 130, 145)
    }

    private val islandPillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#141416")
        style = Paint.Style.FILL
    }

    private val islandStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E2E33")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9EAE")
        textSize = 22f
    }

    private val batteryFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val visualizerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#38EF7D")
    }

    // Interactive Island State for Wallpaper Canvas
    var isIslandExpanded: Boolean = false
    var islandExpandProgress: Float = 0.0f // 0.0f = collapsed, 1.0f = expanded
    var lastInteractionTime: Long = 0L

    data class CelestialState(
        val isDay: Boolean,
        val progress: Float, // 0.0f (rising) to 1.0f (setting)
        val x: Float,
        val y: Float,
        val currentHour: Float
    )

    fun calculateCelestialState(
        width: Float,
        height: Float,
        config: SolunaConfig
    ): CelestialState {
        val hour: Float = if (config.simulationMode) {
            config.simulatedHour
        } else {
            val cal = Calendar.getInstance()
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            val s = cal.get(Calendar.SECOND)
            h + m / 60.0f + s / 3600.0f
        }

        val sunrise = config.sunriseHour + config.sunriseMinute / 60.0f
        val sunset = config.sunsetHour + config.sunsetMinute / 60.0f

        val isDay: Boolean
        val progress: Float

        if (sunrise < sunset) {
            if (hour in sunrise..sunset) {
                isDay = true
                progress = ((hour - sunrise) / (sunset - sunrise)).coerceIn(0.0f, 1.0f)
            } else {
                isDay = false
                val nightSpan = (24.0f - sunset) + sunrise
                val elapsed = if (hour >= sunset) hour - sunset else (24.0f - sunset) + hour
                progress = (elapsed / nightSpan).coerceIn(0.0f, 1.0f)
            }
        } else {
            // Inverse or high-latitude edge case
            isDay = hour in 6.0f..18.0f
            progress = ((hour % 12.0f) / 12.0f).coerceIn(0.0f, 1.0f)
        }

        // Horizontal trajectory from 14% to 86% of screen
        val x = width * (0.14f + 0.72f * progress)

        // Parabolic arc: apex at progress = 0.5
        val arcApexY = height * config.celestialArcHeight
        val arcBaseDrop = height * 0.42f
        val offsetFromApex = progress - 0.5f
        val y = arcApexY + arcBaseDrop * (4.0f * offsetFromApex * offsetFromApex)

        return CelestialState(
            isDay = isDay,
            progress = progress,
            x = x,
            y = y,
            currentHour = hour
        )
    }

    fun render(
        canvas: Canvas,
        width: Float,
        height: Float,
        density: Float,
        config: SolunaConfig,
        battery: BatteryInfo,
        music: MusicInfo,
        isPowerSaving: Boolean,
        animPulse: Float = 0.0f
    ) {
        // 1. STRICT PURE SOLID BLACK BACKGROUND (NO STARS EVER)
        canvas.drawColor(Color.BLACK)

        // 2. CELESTIAL POSITION
        val celestial = calculateCelestialState(width, height, config)

        if (celestial.isDay) {
            drawSun(canvas, celestial, density, config, isPowerSaving, animPulse)
        } else {
            drawMoon(canvas, celestial, density, config, isPowerSaving)
        }

        // 3. DYNAMIC ISLAND (IF ENABLED)
        if (config.enableDynamicIsland) {
            drawDynamicIsland(canvas, width, density, config, battery, music, isPowerSaving)
        }
    }

    private fun drawSun(
        canvas: Canvas,
        celestial: CelestialState,
        density: Float,
        config: SolunaConfig,
        isPowerSaving: Boolean,
        animPulse: Float
    ) {
        val cx = celestial.x
        val cy = celestial.y
        val baseRadius = (config.sunSizeDp * density) / 2.0f
        val glowFactor = config.sunGlowIntensity.coerceIn(0.0f, 1.0f)

        // Chromatic shift: warm gold at noon, amber-crimson near sunrise/sunset
        val proximityToHorizon = kotlin.math.abs(celestial.progress - 0.5f) * 2.0f // 0=noon, 1=horizon
        val coreColor1: Int
        val coreColor2: Int
        val coronaColor: Int

        when (config.colorPreset) {
            "MONO" -> {
                coreColor1 = Color.WHITE
                coreColor2 = Color.parseColor("#E0E0E0")
                coronaColor = Color.argb((glowFactor * 90).toInt(), 220, 220, 220)
            }
            "NORDIC" -> {
                coreColor1 = Color.parseColor("#FFF4E0")
                coreColor2 = Color.parseColor("#FFE0B2")
                coronaColor = Color.argb((glowFactor * 130).toInt(), 255, 210, 150)
            }
            else -> {
                // AUREATE / NATURAL
                if (proximityToHorizon > 0.65f) {
                    // Golden hour / twilight
                    coreColor1 = Color.parseColor("#FFE082")
                    coreColor2 = Color.parseColor("#FF6F00")
                    coronaColor = Color.argb((glowFactor * 150).toInt(), 255, 110, 30)
                } else {
                    // Midday radiance
                    coreColor1 = Color.parseColor("#FFFDE7")
                    coreColor2 = Color.parseColor("#FFD54F")
                    coronaColor = Color.argb((glowFactor * 140).toInt(), 255, 193, 7)
                }
            }
        }

        // Corona / Outer Glow (Layered)
        if (glowFactor > 0.05f) {
            val coronaRadius = baseRadius * (1.6f + 1.8f * glowFactor + animPulse * 0.1f)
            if (!isPowerSaving) {
                // High fidelity: smooth multi-stop corona
                val coronaShader = RadialGradient(
                    cx, cy, coronaRadius,
                    intArrayOf(
                        coronaColor,
                        Color.argb((Color.alpha(coronaColor) * 0.45f).toInt(), Color.red(coronaColor), Color.green(coronaColor), Color.blue(coronaColor)),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0.0f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                sunCoronaPaint.shader = coronaShader
                canvas.drawCircle(cx, cy, coronaRadius, sunCoronaPaint)

                // Secondary soft solar flare ring
                val flareRadius = baseRadius * 1.35f
                val flareShader = RadialGradient(
                    cx, cy, flareRadius,
                    intArrayOf(
                        Color.argb((Color.alpha(coronaColor) * 0.7f).toInt(), 255, 240, 180),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0.0f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                sunCoronaPaint.shader = flareShader
                canvas.drawCircle(cx, cy, flareRadius, sunCoronaPaint)
            } else {
                // Power Saving: single simple soft gradient pass
                val coronaShader = RadialGradient(
                    cx, cy, coronaRadius * 0.85f,
                    intArrayOf(coronaColor, Color.TRANSPARENT),
                    floatArrayOf(0.0f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                sunCoronaPaint.shader = coronaShader
                canvas.drawCircle(cx, cy, coronaRadius * 0.85f, sunCoronaPaint)
            }
        }

        // Sun Core
        val coreShader = RadialGradient(
            cx, cy, baseRadius,
            intArrayOf(coreColor1, coreColor2),
            floatArrayOf(0.0f, 1.0f),
            Shader.TileMode.CLAMP
        )
        sunCorePaint.shader = coreShader
        canvas.drawCircle(cx, cy, baseRadius, sunCorePaint)
    }

    private fun drawMoon(
        canvas: Canvas,
        celestial: CelestialState,
        density: Float,
        config: SolunaConfig,
        isPowerSaving: Boolean
    ) {
        val cx = celestial.x
        val cy = celestial.y
        val baseRadius = (config.moonSizeDp * density) / 2.0f
        val glowFactor = config.moonGlowIntensity.coerceIn(0.0f, 1.0f)

        // Lunar Halo
        if (glowFactor > 0.05f) {
            val haloRadius = baseRadius * (1.5f + 1.4f * glowFactor)
            val haloAlpha = (glowFactor * 100).toInt()
            val haloColor = Color.argb(haloAlpha, 185, 205, 225)

            val haloShader = RadialGradient(
                cx, cy, haloRadius,
                intArrayOf(haloColor, Color.TRANSPARENT),
                floatArrayOf(0.0f, 1.0f),
                Shader.TileMode.CLAMP
            )
            moonHaloPaint.shader = haloShader
            canvas.drawCircle(cx, cy, haloRadius, moonHaloPaint)
        }

        // Moon Disk (Pearlescent Cool Silver/White)
        val moonShader = LinearGradient(
            cx - baseRadius, cy - baseRadius,
            cx + baseRadius, cy + baseRadius,
            Color.parseColor("#F4F6F8"),
            Color.parseColor("#CFD8DC"),
            Shader.TileMode.CLAMP
        )
        moonDiscPaint.shader = moonShader
        canvas.drawCircle(cx, cy, baseRadius, moonDiscPaint)

        // Moon Craters (Minimalist soft crater maria)
        if (!isPowerSaving) {
            canvas.drawCircle(cx - baseRadius * 0.30f, cy - baseRadius * 0.20f, baseRadius * 0.24f, moonCraterPaint)
            canvas.drawCircle(cx + baseRadius * 0.25f, cy + baseRadius * 0.15f, baseRadius * 0.18f, moonCraterPaint)
            canvas.drawCircle(cx - baseRadius * 0.15f, cy + baseRadius * 0.35f, baseRadius * 0.14f, moonCraterPaint)
            canvas.drawCircle(cx + baseRadius * 0.32f, cy - baseRadius * 0.25f, baseRadius * 0.12f, moonCraterPaint)
        }

        // Moon Phase Shadow (Optional crescent mask)
        val phase = config.moonPhase.coerceIn(0.0f, 1.0f)
        if (phase < 0.45f || phase > 0.55f) {
            // Draw shadow crescent
            val shadowOffset = if (phase < 0.5f) {
                // Waxing crescent
                (0.5f - phase) * baseRadius * 1.8f
            } else {
                // Waning crescent
                -(phase - 0.5f) * baseRadius * 1.8f
            }
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            // Clip to moon circle and draw shadow
            canvas.save()
            val moonPath = Path().apply {
                addCircle(cx, cy, baseRadius, Path.Direction.CW)
            }
            canvas.clipPath(moonPath)
            canvas.drawCircle(cx + shadowOffset, cy, baseRadius * 1.05f, shadowPaint)
            canvas.restore()
        }
    }

    private fun drawDynamicIsland(
        canvas: Canvas,
        width: Float,
        density: Float,
        config: SolunaConfig,
        battery: BatteryInfo,
        music: MusicInfo,
        isPowerSaving: Boolean
    ) {
        val yOffset = config.islandYOffsetDp * density
        val pillWidth = config.islandWidthDp * density
        val pillHeight = config.islandHeightDp * density
        val cornerRadius = pillHeight / 2.0f

        val left = (width - pillWidth) / 2.0f
        val top = yOffset
        val right = left + pillWidth
        val bottom = top + pillHeight

        val pillRect = RectF(left, top, right, bottom)

        // Outer Glow/Shadow
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, islandPillPaint)
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, islandStrokePaint)

        val centerY = top + pillHeight / 2.0f

        // LEFT: Battery Widget
        if (config.enableBatteryWidget) {
            val batX = left + 14f * density
            val batW = 20f * density
            val batH = 10f * density
            val batY = centerY - batH / 2.0f

            // Battery Outline
            val batRect = RectF(batX, batY, batX + batW, batY + batH)
            val strokeBat = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = Color.parseColor("#787880")
            }
            canvas.drawRoundRect(batRect, 3f * density, 3f * density, strokeBat)

            // Battery Nipple
            val nipX = batX + batW + 1f
            val nipY = centerY - 2.5f * density
            canvas.drawRect(nipX, nipY, nipX + 2.5f * density, nipY + 5f * density, strokeBat)

            // Battery Fill
            val fillWidth = (batW - 4f) * (battery.level.coerceIn(0, 100) / 100.0f)
            batteryFillPaint.color = when {
                battery.isCharging -> Color.parseColor("#34C759")
                battery.level <= 20 -> Color.parseColor("#FF3B30")
                battery.level <= 45 -> Color.parseColor("#FF9500")
                else -> Color.parseColor("#30D158")
            }
            if (fillWidth > 0) {
                val fillRect = RectF(batX + 2f, batY + 2f, batX + 2f + fillWidth, batY + batH - 2f)
                canvas.drawRoundRect(fillRect, 2f * density, 2f * density, batteryFillPaint)
            }

            // Percentage Text
            textPaint.textSize = 11f * density
            val pctStr = "${battery.level}%"
            val textX = batX + batW + 8f * density
            val fontMetrics = textPaint.fontMetrics
            val baseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
            canvas.drawText(pctStr, textX, baseline, textPaint)
        }

        // RIGHT: Music Waveform / Visualizer
        if (config.enableMusicWidget) {
            val endX = right - 14f * density
            val barCount = if (isPowerSaving) 4 else 6
            val barW = 2.5f * density
            val gap = 2.5f * density
            val totalVisualizerW = barCount * barW + (barCount - 1) * gap
            var startBarX = endX - totalVisualizerW

            visualizerPaint.color = if (music.isPlaying) Color.parseColor("#30D158") else Color.parseColor("#636366")

            for (i in 0 until barCount) {
                val band = if (i < music.visualizerBands.size) music.visualizerBands[i] else 0.2f
                val maxBarH = pillHeight * 0.45f
                val barH = if (music.isPlaying) (maxBarH * band).coerceAtLeast(3f * density) else 3f * density
                val barTop = centerY - barH / 2.0f
                val barBottom = centerY + barH / 2.0f
                val barRect = RectF(startBarX, barTop, startBarX + barW, barBottom)
                canvas.drawRoundRect(barRect, barW / 2.0f, barW / 2.0f, visualizerPaint)
                startBarX += barW + gap
            }
        }
    }

    fun isTapInsideIsland(touchX: Float, touchY: Float, width: Float, density: Float, config: SolunaConfig): Boolean {
        if (!config.enableDynamicIsland) return false
        val yOffset = config.islandYOffsetDp * density
        val pillWidth = config.islandWidthDp * density
        val pillHeight = config.islandHeightDp * density
        val left = (width - pillWidth) / 2.0f
        val top = yOffset
        val right = left + pillWidth
        val bottom = top + pillHeight
        // Generous touch tolerance
        val pad = 16f * density
        return touchX in (left - pad)..(right + pad) && touchY in (top - pad)..(bottom + pad)
    }
}
