package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SolunaConfig
import com.example.state.BatteryInfo
import com.example.state.MusicInfo
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DynamicIslandGreen
import com.example.ui.theme.DynamicIslandOrange
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TextPrimary
import java.util.Calendar
import kotlin.math.abs

@Composable
fun CelestialPreviewCanvas(
    config: SolunaConfig,
    battery: BatteryInfo,
    music: MusicInfo,
    isPowerSaving: Boolean,
    isIslandExpanded: Boolean,
    onToggleIslandExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density

    // Subtle breathing pulse for solar corona
    val infiniteTransition = rememberInfiniteTransition(label = "corona_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corona_pulse_val"
    )

    // Calculate current time & celestial progress
    val currentHour = remember(config.simulationMode, config.simulatedHour) {
        if (config.simulationMode) {
            config.simulatedHour
        } else {
            val cal = Calendar.getInstance()
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            val s = cal.get(Calendar.SECOND)
            h + m / 60.0f + s / 3600.0f
        }
    }

    val sunrise = config.sunriseHour + config.sunriseMinute / 60.0f
    val sunset = config.sunsetHour + config.sunsetMinute / 60.0f

    val isDay = currentHour in sunrise..sunset
    val celestialProgress = if (isDay) {
        ((currentHour - sunrise) / (sunset - sunrise)).coerceIn(0.0f, 1.0f)
    } else {
        val nightSpan = (24.0f - sunset) + sunrise
        val elapsed = if (currentHour >= sunset) currentHour - sunset else (24.0f - sunset) + currentHour
        (elapsed / nightSpan).coerceIn(0.0f, 1.0f)
    }

    Box(
        modifier = modifier
            .background(PureBlack)
            .testTag("celestial_preview_container")
    ) {
        // Pure Canvas drawing Sun / Moon on OLED solid black background (NO STARS)
        Canvas(modifier = Modifier.fillMaxSize().testTag("celestial_canvas")) {
            val canvasW = size.width
            val canvasH = size.height

            // Calculate celestial position along parabolic arc
            val posX = canvasW * (0.14f + 0.72f * celestialProgress)
            val arcApexY = canvasH * config.celestialArcHeight
            val arcBaseDrop = canvasH * 0.40f
            val offsetFromApex = celestialProgress - 0.5f
            val posY = arcApexY + arcBaseDrop * (4.0f * offsetFromApex * offsetFromApex)

            if (isDay) {
                // RENDER SUN
                val sunRadius = (config.sunSizeDp * density) / 2.0f
                val glow = config.sunGlowIntensity.coerceIn(0.0f, 1.0f)

                // Proximity to sunset/sunrise gives warmer amber hues
                val horizonProximity = abs(celestialProgress - 0.5f) * 2.0f
                val coreCenterColor = if (horizonProximity > 0.65f) Color(0xFFFFE082) else Color(0xFFFFFDE7)
                val coreEdgeColor = if (horizonProximity > 0.65f) Color(0xFFFF6F00) else Color(0xFFFFD54F)
                val coronaBaseColor = if (horizonProximity > 0.65f) Color(0xFFFF5722) else Color(0xFFFFB300)

                // Corona Glow
                if (glow > 0.05f) {
                    val coronaRadius = sunRadius * (1.6f + 1.8f * glow) * (if (isPowerSaving) 1.0f else pulseAnim)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                coronaBaseColor.copy(alpha = glow * 0.55f),
                                coronaBaseColor.copy(alpha = glow * 0.20f),
                                Color.Transparent
                            ),
                            center = Offset(posX, posY),
                            radius = coronaRadius
                        ),
                        radius = coronaRadius,
                        center = Offset(posX, posY)
                    )

                    if (!isPowerSaving) {
                        val innerFlareRadius = sunRadius * 1.35f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFECB3).copy(alpha = glow * 0.75f),
                                    Color.Transparent
                                ),
                                center = Offset(posX, posY),
                                radius = innerFlareRadius
                            ),
                            radius = innerFlareRadius,
                            center = Offset(posX, posY)
                        )
                    }
                }

                // Sun Core Disk
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(coreCenterColor, coreEdgeColor),
                        center = Offset(posX, posY),
                        radius = sunRadius
                    ),
                    radius = sunRadius,
                    center = Offset(posX, posY)
                )
            } else {
                // RENDER MOON
                val moonRadius = (config.moonSizeDp * density) / 2.0f
                val glow = config.moonGlowIntensity.coerceIn(0.0f, 1.0f)

                // Lunar Halo
                if (glow > 0.05f) {
                    val haloRadius = moonRadius * (1.5f + 1.4f * glow)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFB0BEC5).copy(alpha = glow * 0.45f),
                                Color(0xFF90A4AE).copy(alpha = glow * 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(posX, posY),
                            radius = haloRadius
                        ),
                        radius = haloRadius,
                        center = Offset(posX, posY)
                    )
                }

                // Moon Disc
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF5F7FA), Color(0xFFCFD8DC)),
                        start = Offset(posX - moonRadius, posY - moonRadius),
                        end = Offset(posX + moonRadius, posY + moonRadius)
                    ),
                    radius = moonRadius,
                    center = Offset(posX, posY)
                )

                // Subtle Crater Maria
                if (!isPowerSaving) {
                    val craterColor = Color(0x35788291)
                    drawCircle(
                        color = craterColor,
                        radius = moonRadius * 0.24f,
                        center = Offset(posX - moonRadius * 0.30f, posY - moonRadius * 0.20f)
                    )
                    drawCircle(
                        color = craterColor,
                        radius = moonRadius * 0.18f,
                        center = Offset(posX + moonRadius * 0.25f, posY + moonRadius * 0.15f)
                    )
                    drawCircle(
                        color = craterColor,
                        radius = moonRadius * 0.14f,
                        center = Offset(posX - moonRadius * 0.15f, posY + moonRadius * 0.35f)
                    )
                }

                // Moon Phase Crescent Shadow
                val phase = config.moonPhase.coerceIn(0.0f, 1.0f)
                if (phase < 0.45f || phase > 0.55f) {
                    val shadowOffset = if (phase < 0.5f) {
                        (0.5f - phase) * moonRadius * 1.8f
                    } else {
                        -(phase - 0.5f) * moonRadius * 1.8f
                    }
                    val moonClip = Path().apply {
                        addOval(
                            androidx.compose.ui.geometry.Rect(
                                posX - moonRadius,
                                posY - moonRadius,
                                posX + moonRadius,
                                posY + moonRadius
                            )
                        )
                    }
                    clipPath(moonClip) {
                        drawCircle(
                            color = PureBlack,
                            radius = moonRadius * 1.05f,
                            center = Offset(posX + shadowOffset, posY)
                        )
                    }
                }
            }
        }

        // Overlay: Dynamic Island at top
        if (config.enableDynamicIsland) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = config.islandYOffsetDp.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                InteractiveDynamicIslandView(
                    config = config,
                    battery = battery,
                    music = music,
                    isPowerSaving = isPowerSaving,
                    isExpanded = isIslandExpanded,
                    onToggleExpand = onToggleIslandExpand,
                    onTogglePlayPause = onTogglePlayPause,
                    onNextTrack = onNextTrack,
                    onPrevTrack = onPrevTrack
                )
            }
        }

        // Floating Status Badge at bottom of preview
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color(0xD90C0C10), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            val formattedHour = String.format("%02d:%02d", currentHour.toInt(), ((currentHour % 1f) * 60).toInt())
            val celestialLabel = if (isDay) "Matahari (${config.sunSizeDp.toInt()}dp)" else "Bulan (${config.moonSizeDp.toInt()}dp)"
            val phaseLabel = if (isDay) "Siang Hari" else "Malam Hari"

            Text(
                text = "$phaseLabel • $formattedHour • $celestialLabel",
                color = if (isDay) SolarGold else Color(0xFFCFD8DC),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
