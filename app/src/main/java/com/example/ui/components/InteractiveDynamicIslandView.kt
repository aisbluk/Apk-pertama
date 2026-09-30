package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SolunaConfig
import com.example.state.BatteryInfo
import com.example.state.MusicInfo
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DynamicIslandGreen
import com.example.ui.theme.DynamicIslandOrange
import com.example.ui.theme.DynamicIslandRed
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun InteractiveDynamicIslandView(
    config: SolunaConfig,
    battery: BatteryInfo,
    music: MusicInfo,
    isPowerSaving: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBg = Color(0xFF141418)
    val pillBorder = DarkCardBorder

    Box(
        modifier = modifier
            .testTag("dynamic_island_container")
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        if (!isExpanded) {
            // Collapsed Dynamic Island Pill
            Row(
                modifier = Modifier
                    .width(config.islandWidthDp.dp)
                    .height(config.islandHeightDp.dp)
                    .clip(RoundedCornerShape((config.islandHeightDp / 2).dp))
                    .background(pillBg)
                    .border(1.dp, pillBorder, RoundedCornerShape((config.islandHeightDp / 2).dp))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 12.dp)
                    .testTag("dynamic_island_pill"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Battery Indicator
                if (config.enableBatteryWidget) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val batColor = when {
                            battery.isCharging -> DynamicIslandGreen
                            battery.level <= 20 -> DynamicIslandRed
                            battery.level <= 45 -> DynamicIslandOrange
                            else -> DynamicIslandGreen
                        }

                        if (battery.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Charging",
                                tint = DynamicIslandGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(9.dp)
                                    .border(1.dp, TextSecondary, RoundedCornerShape(2.dp))
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(battery.level / 100f)
                                        .height(7.dp)
                                        .background(batColor, RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        Text(
                            text = "${battery.level}%",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Music Visualizer or Mini Indicator
                if (config.enableMusicWidget) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val barCount = if (isPowerSaving) 4 else 6
                        for (i in 0 until barCount) {
                            val bandVal = if (i < music.visualizerBands.size) music.visualizerBands[i] else 0.2f
                            val barHeight = if (music.isPlaying) (12 * bandVal).coerceIn(3f, 12f) else 3f
                            Box(
                                modifier = Modifier
                                    .width(2.5.dp)
                                    .height(barHeight.dp)
                                    .background(
                                        if (music.isPlaying) DynamicIslandGreen else TextTertiary,
                                        CircleShape
                                    )
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        } else {
            // Expanded Dynamic Island Card
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(DarkCard)
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(28.dp))
                    .padding(16.dp)
                    .testTag("dynamic_island_expanded_card"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (battery.isCharging) DynamicIslandGreen else SolarGold, CircleShape)
                        )
                        Text(
                            text = "Dynamic Island",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // FPS / Power status pill
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isPowerSaving) DynamicIslandOrange.copy(alpha = 0.2f) else DynamicIslandGreen.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isPowerSaving) "${config.targetFpsPowerSave} FPS • Hemat Daya" else "${config.targetFpsNormal} FPS • Optimal",
                                color = if (isPowerSaving) DynamicIslandOrange else DynamicIslandGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Island",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Battery Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F14), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (battery.isCharging) DynamicIslandGreen.copy(alpha = 0.2f) else Color(0xFF1E1E26),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (battery.isCharging) Icons.Default.Bolt else Icons.Default.BatteryChargingFull,
                                contentDescription = "Battery Status",
                                tint = if (battery.isCharging) DynamicIslandGreen else TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (battery.isCharging) "Mengisi Daya (${battery.plugType})" else "Daya Baterai",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${battery.level}%",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${battery.temperatureCelsius}°C",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Kondisi: ${battery.healthStatus}",
                            color = DynamicIslandGreen,
                            fontSize = 11.sp
                        )
                    }
                }

                // Music Playback Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F14), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(SolarGold.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Music Playing",
                                    tint = SolarGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = music.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = music.artist,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Playback Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onPrevTrack,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Lagu Sebelumnya",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onTogglePlayPause,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TextPrimary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (music.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Putar atau Jeda",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onNextTrack,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Lagu Selanjutnya",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Live Waveform Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(Color(0xFF09090C), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        music.visualizerBands.forEach { band ->
                            val barH = if (music.isPlaying) (18 * band).coerceIn(3f, 18f) else 3f
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barH.dp)
                                    .background(
                                        if (music.isPlaying) DynamicIslandGreen else TextTertiary,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
