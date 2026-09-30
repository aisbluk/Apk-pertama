package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.crypto.SolunaCrypto
import com.example.model.SolunaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SolunaPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<SolunaConfig> = _configFlow.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_CONFIG_JSON) {
            _configFlow.value = loadConfig()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun getConfig(): SolunaConfig = _configFlow.value

    fun updateConfig(newConfig: SolunaConfig) {
        val json = SolunaCrypto.configToJson(newConfig)
        prefs.edit().putString(KEY_CONFIG_JSON, json).apply()
        _configFlow.value = newConfig
    }

    private fun loadConfig(): SolunaConfig {
        val json = prefs.getString(KEY_CONFIG_JSON, null) ?: return SolunaConfig()
        return try {
            SolunaCrypto.jsonToConfig(json)
        } catch (_: Exception) {
            SolunaConfig()
        }
    }

    companion object {
        private const val PREFS_NAME = "soluna_wallpaper_prefs"
        private const val KEY_CONFIG_JSON = "key_soluna_config_json"

        @Volatile
        private var instance: SolunaPreferences? = null

        fun getInstance(context: Context): SolunaPreferences {
            return instance ?: synchronized(this) {
                instance ?: SolunaPreferences(context).also { instance = it }
            }
        }
    }
}
