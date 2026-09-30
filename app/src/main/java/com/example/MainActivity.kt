package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.SolunaPreferences
import com.example.state.DeviceStatusManager
import com.example.ui.screens.SolunaHomeScreen
import com.example.ui.theme.SolunaTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferences: SolunaPreferences
    private lateinit var statusManager: DeviceStatusManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = SolunaPreferences.getInstance(this)
        statusManager = DeviceStatusManager.getInstance(this)

        setContent {
            SolunaTheme {
                SolunaHomeScreen(
                    preferences = preferences,
                    statusManager = statusManager
                )
            }
        }
    }
}
