package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SolunaPreferences
import com.example.model.SolunaConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Soluna", appName)
    }

    @Test
    fun `preferences stores and loads custom sun and moon sizes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = SolunaPreferences.getInstance(context)

        val customConfig = SolunaConfig(
            sunSizeDp = 180f,
            moonSizeDp = 150f,
            smartPowerSaveEnabled = true,
            powerSaveThreshold = 25
        )
        prefs.updateConfig(customConfig)

        val loaded = prefs.getConfig()
        assertEquals(180f, loaded.sunSizeDp, 0.01f)
        assertEquals(150f, loaded.moonSizeDp, 0.01f)
        assertEquals(25, loaded.powerSaveThreshold)
    }
}
