package com.example.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.SolunaCrypto
import com.example.data.SolunaPreferences
import com.example.model.SolunaConfig
import com.example.service.SolunaWallpaperService
import com.example.state.DeviceStatusManager
import com.example.ui.components.CelestialPreviewCanvas
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DynamicIslandGreen
import com.example.ui.theme.DynamicIslandOrange
import com.example.ui.theme.DynamicIslandRed
import com.example.ui.theme.LunarSilver
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolunaHomeScreen(
    preferences: SolunaPreferences,
    statusManager: DeviceStatusManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val config by preferences.configFlow.collectAsState()
    val battery by statusManager.batteryState.collectAsState()
    val music by statusManager.musicState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isIslandExpandedInPreview by remember { mutableStateOf(false) }

    // Sync state
    var syncPassphrase by remember { mutableStateOf("SolunaSecretKey2026") }
    var exportedPayload by remember { mutableStateOf("") }
    var importPayloadInput by remember { mutableStateOf("") }
    var syncStatusMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val isPowerSaving = statusManager.isPowerSavingActive(config)

    fun applyConfig(newConf: SolunaConfig) {
        preferences.updateConfig(newConf)
    }

    fun launchWallpaperPicker() {
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, SolunaWallpaperService::class.java)
                )
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Buka Pengaturan > Wallpaper untuk memilih Soluna Live Wallpaper", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(SolarGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(PureBlack, CircleShape)
                            )
                        }
                        Column {
                            Text(
                                text = "Soluna",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Live Wallpaper OLED Minimalis",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = { launchWallpaperPicker() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolarGold,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("apply_wallpaper_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Terapkan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE INTERACTIVE WALLPAPER PREVIEW WINDOW
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.5.dp, DarkCardBorder, RoundedCornerShape(24.dp))
                    ) {
                        CelestialPreviewCanvas(
                            config = config,
                            battery = battery,
                            music = music,
                            isPowerSaving = isPowerSaving,
                            isIslandExpanded = isIslandExpandedInPreview,
                            onToggleIslandExpand = { isIslandExpandedInPreview = !isIslandExpandedInPreview },
                            onTogglePlayPause = { statusManager.toggleMusicPlayback() },
                            onNextTrack = { statusManager.nextTrack() },
                            onPrevTrack = { statusManager.prevTrack() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Time Scrubbing Bar for Fast Previewing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCard, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (config.simulationMode) Icons.Default.Tune else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (config.simulationMode) SolarGold else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (config.simulationMode) "Mode Simulasi Waktu" else "Sinkron Waktu Nyata",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = config.simulationMode,
                            onCheckedChange = { applyConfig(config.copy(simulationMode = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureBlack,
                                checkedTrackColor = SolarGold,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = DarkSurface
                            ),
                            modifier = Modifier.testTag("simulation_mode_switch")
                        )
                    }

                    // Simulation Slider if active
                    AnimatedVisibility(visible = config.simulationMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color(0xFF0F0F14), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val hourInt = config.simulatedHour.toInt()
                            val minInt = ((config.simulatedHour % 1f) * 60).toInt()
                            val timeStr = String.format("%02d:%02d", hourInt, minInt)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Uji Siklus Langit (Geser Waktu)",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$timeStr WIB",
                                    color = SolarGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Slider(
                                value = config.simulatedHour,
                                onValueChange = { applyConfig(config.copy(simulatedHour = it)) },
                                valueRange = 0f..23.99f,
                                colors = SliderDefaults.colors(
                                    thumbColor = SolarGold,
                                    activeTrackColor = SolarGold,
                                    inactiveTrackColor = DarkCardBorder
                                ),
                                modifier = Modifier.testTag("simulated_hour_slider")
                            )

                            // Quick Time Anchor Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(
                                    Pair("07:00 (Pagi)", 7.0f),
                                    Pair("12:00 (Siang)", 12.0f),
                                    Pair("17:45 (Senja)", 17.75f),
                                    Pair("23:00 (Malam)", 23.0f)
                                ).forEach { (label, hr) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (kotlin.math.abs(config.simulatedHour - hr) < 0.5f) SolarGold.copy(alpha = 0.2f) else DarkCard)
                                            .border(1.dp, if (kotlin.math.abs(config.simulatedHour - hr) < 0.5f) SolarGold else DarkCardBorder, RoundedCornerShape(8.dp))
                                            .clickable { applyConfig(config.copy(simulatedHour = hr)) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. FEATURE TABS
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = PureBlack,
                    contentColor = SolarGold,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SolarGold
                        )
                    }
                ) {
                    val tabs = listOf(
                        "Kustomisasi Ukuran",
                        "Dynamic Island",
                        "Jadwal & Daya",
                        "Sinkronisasi E2E"
                    )
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedTabIndex == index) SolarGold else TextSecondary
                                )
                            },
                            modifier = Modifier.testTag("tab_$index")
                        )
                    }
                }
            }

            // 3. TAB CONTENT
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: KUSTOMISASI UKURAN & VISUAL
                    item {
                        SizeCustomizationSection(
                            config = config,
                            onConfigChange = { applyConfig(it) }
                        )
                    }
                }
                1 -> {
                    // TAB 1: DYNAMIC ISLAND
                    item {
                        DynamicIslandSection(
                            config = config,
                            battery = battery,
                            music = music,
                            statusManager = statusManager,
                            onConfigChange = { applyConfig(it) }
                        )
                    }
                }
                2 -> {
                    // TAB 2: JADWAL & HEMAT DAYA
                    item {
                        ScheduleAndPowerSection(
                            config = config,
                            isPowerSaving = isPowerSaving,
                            onConfigChange = { applyConfig(it) }
                        )
                    }
                }
                3 -> {
                    // TAB 3: SINKRONISASI E2E TERENKRIPSI
                    item {
                        EncryptedSyncSection(
                            config = config,
                            passphrase = syncPassphrase,
                            onPassphraseChange = { syncPassphrase = it },
                            exportedPayload = exportedPayload,
                            onExport = {
                                exportedPayload = SolunaCrypto.encrypt(config, syncPassphrase)
                                clipboardManager.setText(AnnotatedString(exportedPayload))
                                syncStatusMessage = Pair(true, "Konfigurasi terenkripsi disalin ke clipboard!")
                            },
                            importPayloadInput = importPayloadInput,
                            onImportPayloadChange = { importPayloadInput = it },
                            onImport = {
                                val result = SolunaCrypto.decrypt(importPayloadInput, syncPassphrase)
                                result.onSuccess { restored ->
                                    applyConfig(restored)
                                    syncStatusMessage = Pair(true, "Berhasil memulihkan & menyinkronkan pengaturan!")
                                }.onFailure { error ->
                                    syncStatusMessage = Pair(false, "Gagal memulihkan: ${error.localizedMessage ?: "Kunci sandi salah atau data rusak"}")
                                }
                            },
                            onGenerateRandomKey = {
                                syncPassphrase = SolunaCrypto.generateRandomSyncKey()
                                syncStatusMessage = Pair(true, "Kunci enkripsi baru dibuat")
                            },
                            statusMessage = syncStatusMessage
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ---------------------- SUB-SECTIONS ---------------------- //

@Composable
private fun SizeCustomizationSection(
    config: SolunaConfig,
    onConfigChange: (SolunaConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Presets
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Prasetel Cepat (Presets)",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Pilih rasio ukuran yang dirancang untuk keselarasan visual minimalis.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SolunaConfig.Preset.values().forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F0F14))
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                                .clickable { onConfigChange(SolunaConfig.fromPreset(preset)) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.title,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Ukuran Matahari Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Ukuran Matahari (Siang Hari)",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Diameter cakram matahari di langit",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = "${config.sunSizeDp.toInt()} dp",
                        color = SolarGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Slider(
                    value = config.sunSizeDp,
                    onValueChange = { onConfigChange(config.copy(sunSizeDp = it)) },
                    valueRange = 50f..240f,
                    colors = SliderDefaults.colors(
                        thumbColor = SolarGold,
                        activeTrackColor = SolarGold,
                        inactiveTrackColor = DarkSurface
                    ),
                    modifier = Modifier.testTag("sun_size_slider")
                )

                // Quick Size Chips for Sun
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("Kecil 75dp", 75f),
                        Pair("Ideal 110dp", 110f),
                        Pair("Besar 150dp", 150f),
                        Pair("Mega 200dp", 200f)
                    ).forEach { (label, sz) ->
                        FilterChip(
                            selected = kotlin.math.abs(config.sunSizeDp - sz) < 5f,
                            onClick = { onConfigChange(config.copy(sunSizeDp = sz)) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolarGold.copy(alpha = 0.2f),
                                selectedLabelColor = SolarGold
                            )
                        )
                    }
                }
            }
        }

        // Ukuran Bulan Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = null,
                            tint = LunarSilver,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Ukuran Bulan (Malam Hari)",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Diameter piringan bulan perak",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = "${config.moonSizeDp.toInt()} dp",
                        color = LunarSilver,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Slider(
                    value = config.moonSizeDp,
                    onValueChange = { onConfigChange(config.copy(moonSizeDp = it)) },
                    valueRange = 40f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = LunarSilver,
                        activeTrackColor = LunarSilver,
                        inactiveTrackColor = DarkSurface
                    ),
                    modifier = Modifier.testTag("moon_size_slider")
                )

                // Quick Size Chips for Moon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Pair("Ramping 65dp", 65f),
                        Pair("Zen 95dp", 95f),
                        Pair("Besar 135dp", 135f),
                        Pair("Purnama 180dp", 180f)
                    ).forEach { (label, sz) ->
                        FilterChip(
                            selected = kotlin.math.abs(config.moonSizeDp - sz) < 5f,
                            onClick = { onConfigChange(config.copy(moonSizeDp = sz)) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LunarSilver.copy(alpha = 0.2f),
                                selectedLabelColor = LunarSilver
                            )
                        )
                    }
                }
            }
        }

        // Glow & Corona Intensity
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pendaran Cahaya & Halo Atmosfer",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Sun Glow
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pendaran Korona Matahari", color = TextSecondary, fontSize = 12.sp)
                        Text("${(config.sunGlowIntensity * 100).toInt()}%", color = SolarGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.sunGlowIntensity,
                        onValueChange = { onConfigChange(config.copy(sunGlowIntensity = it)) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = SolarGold,
                            activeTrackColor = SolarGold,
                            inactiveTrackColor = DarkSurface
                        )
                    )
                }

                // Moon Halo
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Halo Cahaya Bulan", color = TextSecondary, fontSize = 12.sp)
                        Text("${(config.moonGlowIntensity * 100).toInt()}%", color = LunarSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.moonGlowIntensity,
                        onValueChange = { onConfigChange(config.copy(moonGlowIntensity = it)) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = LunarSilver,
                            activeTrackColor = LunarSilver,
                            inactiveTrackColor = DarkSurface
                        )
                    )
                }

                // Arc Trajectory Height
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tinggi Puncak Lintasan Langit (Arc Apex)", color = TextSecondary, fontSize = 12.sp)
                        Text("${(config.celestialArcHeight * 100).toInt()}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.celestialArcHeight,
                        onValueChange = { onConfigChange(config.copy(celestialArcHeight = it)) },
                        valueRange = 0.15f..0.55f,
                        colors = SliderDefaults.colors(
                            thumbColor = TextPrimary,
                            activeTrackColor = TextPrimary,
                            inactiveTrackColor = DarkSurface
                        )
                    )
                }
            }
        }

        // Dynamic Island Dimensions Customization
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Dimensi Dynamic Island",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Island Width
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lebar Dynamic Island", color = TextSecondary, fontSize = 12.sp)
                        Text("${config.islandWidthDp.toInt()} dp", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.islandWidthDp,
                        onValueChange = { onConfigChange(config.copy(islandWidthDp = it)) },
                        valueRange = 130f..270f
                    )
                }

                // Island Height
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tinggi Dynamic Island", color = TextSecondary, fontSize = 12.sp)
                        Text("${config.islandHeightDp.toInt()} dp", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.islandHeightDp,
                        onValueChange = { onConfigChange(config.copy(islandHeightDp = it)) },
                        valueRange = 28f..50f
                    )
                }

                // Island Y-Offset
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jarak dari Atas Layar (Y-Offset)", color = TextSecondary, fontSize = 12.sp)
                        Text("${config.islandYOffsetDp.toInt()} dp", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = config.islandYOffsetDp,
                        onValueChange = { onConfigChange(config.copy(islandYOffsetDp = it)) },
                        valueRange = 4f..60f
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicIslandSection(
    config: SolunaConfig,
    battery: com.example.state.BatteryInfo,
    music: com.example.state.MusicInfo,
    statusManager: DeviceStatusManager,
    onConfigChange: (SolunaConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Island Switch
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktifkan Dynamic Island",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Menampilkan pill interaktif pemantau status baterai dan musik di bagian atas wallpaper",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = config.enableDynamicIsland,
                    onCheckedChange = { onConfigChange(config.copy(enableDynamicIsland = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = DynamicIslandGreen
                    )
                )
            }
        }

        // Battery Monitor Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = DynamicIslandGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pemantau Status Baterai",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = config.enableBatteryWidget,
                        onCheckedChange = { onConfigChange(config.copy(enableBatteryWidget = it)) }
                    )
                }

                Text(
                    text = "Uji Simulasi Status Baterai:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F0F14))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .clickable { statusManager.clearBatterySimulation() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Baterai Asli", color = TextPrimary, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F0F14))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .clickable { statusManager.simulateBattery(88, true) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("88% Mengisi", color = DynamicIslandGreen, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F0F14))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .clickable { statusManager.simulateBattery(14, false) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("14% Lemah", color = DynamicIslandRed, fontSize = 11.sp)
                    }
                }
            }
        }

        // Music Monitor Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Pemutar Musik & Waveform",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = config.enableMusicWidget,
                        onCheckedChange = { onConfigChange(config.copy(enableMusicWidget = it)) }
                    )
                }

                Text(
                    text = "Saat musik aktif, equalizer bergelombang dinamis ditampilkan di sisi kanan Dynamic Island.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0F14), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = music.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = music.artist, color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { statusManager.toggleMusicPlayback() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (music.isPlaying) DynamicIslandOrange else DynamicIslandGreen,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (music.isPlaying) "Jeda" else "Putar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleAndPowerSection(
    config: SolunaConfig,
    isPowerSaving: Boolean,
    onConfigChange: (SolunaConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Automatic Day / Night Solar Schedule
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Jadwal Otomatis Siang & Malam",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Berdasarkan waktu matahari terbit & terbenam",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = config.autoDayNightSchedule,
                        onCheckedChange = { onConfigChange(config.copy(autoDayNightSchedule = it)) }
                    )
                }

                Text(
                    text = "Menyesuaikan tampilan langit secara mulus antara matahari emas saat siang dan bulan perak saat malam untuk kenyamanan mata pengguna dalam berbagai kondisi pencahayaan.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Sunrise & Sunset Sliders
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Waktu Matahari Terbit (Sunrise)", color = TextSecondary, fontSize = 12.sp)
                        Text(String.format("%02d:%02d WIB", config.sunriseHour, config.sunriseMinute), color = SolarGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = config.sunriseHour.toFloat(),
                        onValueChange = { onConfigChange(config.copy(sunriseHour = it.toInt())) },
                        valueRange = 4f..8f,
                        steps = 3
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Waktu Matahari Terbenam (Sunset)", color = TextSecondary, fontSize = 12.sp)
                        Text(String.format("%02d:%02d WIB", config.sunsetHour, config.sunsetMinute), color = LunarSilver, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = config.sunsetHour.toFloat(),
                        onValueChange = { onConfigChange(config.copy(sunsetHour = it.toInt())) },
                        valueRange = 16f..21f,
                        steps = 4
                    )
                }
            }
        }

        // Smart Power Saving Mode
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isPowerSaving) DynamicIslandOrange else DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (isPowerSaving) DynamicIslandOrange else DynamicIslandGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Mode Hemat Daya Cerdas",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isPowerSaving) "Status: Aktif Membatasi Frame Rate (${config.targetFpsPowerSave} FPS)" else "Status: Optimal (${config.targetFpsNormal} FPS)",
                                color = if (isPowerSaving) DynamicIslandOrange else DynamicIslandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Switch(
                        checked = config.smartPowerSaveEnabled,
                        onCheckedChange = { onConfigChange(config.copy(smartPowerSaveEnabled = it)) }
                    )
                }

                Text(
                    text = "Secara cerdas menyesuaikan frekuensi pembaruan animasi ke ${config.targetFpsPowerSave} FPS dan mematikan kalkulasi korona berat saat baterai perangkat menipis atau saat perangkat memasuki Battery Saver. Pada layar OLED hitam pekat, ini menghasilkan penghematan daya yang sangat signifikan.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Battery Threshold Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Aktifkan otomatis jika baterai di bawah:", color = TextSecondary, fontSize = 12.sp)
                        Text("${config.powerSaveThreshold}%", color = DynamicIslandOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = config.powerSaveThreshold.toFloat(),
                        onValueChange = { onConfigChange(config.copy(powerSaveThreshold = it.toInt())) },
                        valueRange = 10f..40f
                    )
                }
            }
        }
    }
}

@Composable
private fun EncryptedSyncSection(
    config: SolunaConfig,
    passphrase: String,
    onPassphraseChange: (String) -> Unit,
    exportedPayload: String,
    onExport: () -> Unit,
    importPayloadInput: String,
    onImportPayloadChange: (String) -> Unit,
    onImport: () -> Unit,
    onGenerateRandomKey: () -> Unit,
    statusMessage: Pair<Boolean, String>?
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1410)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DynamicIslandGreen.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DynamicIslandGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DynamicIslandGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Enkripsi End-to-End Terdepan",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "AES-256-GCM + PBKDF2 (10.000 iterasi). Menjamin integritas data dari pemalsuan dan menjaga kerahasiaan sinkronisasi antarperangkat.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Status Toast Message
        statusMessage?.let { (success, msg) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (success) DynamicIslandGreen.copy(alpha = 0.15f) else DynamicIslandRed.copy(alpha = 0.15f))
                    .border(1.dp, if (success) DynamicIslandGreen else DynamicIslandRed, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = msg,
                    color = if (success) DynamicIslandGreen else DynamicIslandRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Passphrase Setup Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kata Sandi / Kunci Rahasia Sinkronisasi",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Kunci ini digunakan untuk mengenkripsi dan mendekripsi bundle data di semua perangkat Anda.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = passphrase,
                    onValueChange = onPassphraseChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("passphrase_input"),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = SolarGold)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SolarGold,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Button(
                    onClick = onGenerateRandomKey,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = SolarGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Kunci Acak Kuat", fontSize = 11.sp, color = SolarGold)
                }
            }
        }

        // Export Encrypted Data
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Ekspor Konfigurasi Terenkripsi",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Paketkan semua ukuran elemen visual, dynamic island, dan jadwal ke dalam payload terenkripsi E2E.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Button(
                    onClick = onExport,
                    colors = ButtonDefaults.buttonColors(containerColor = SolarGold, contentColor = PureBlack),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_button")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enkripsi & Salin Bundle Sinkronisasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (exportedPayload.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0B0E), RoundedCornerShape(8.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedPayload,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 3
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(exportedPayload))
                                Toast.makeText(context, "Kode terenkripsi disalin!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, exportedPayload)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Kode Sinkronisasi Soluna"))
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Import & Restore
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Impor & Pulihkan Antarperangkat",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Tempel bundle payload yang diekspor dari perangkat lain dan masukkan kata sandi yang sesuai untuk mendekripsi.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = importPayloadInput,
                    onValueChange = onImportPayloadChange,
                    placeholder = { Text("Tempel kode SOLUNA-SYNC-v1:...", fontSize = 11.sp, color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DynamicIslandGreen,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Button(
                    onClick = onImport,
                    enabled = importPayloadInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DynamicIslandGreen, contentColor = PureBlack),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("import_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dekripsi & Terapkan Konfigurasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
