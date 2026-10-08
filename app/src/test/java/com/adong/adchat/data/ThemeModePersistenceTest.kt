package com.adong.adchat.data

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
class ThemeModePersistenceTest {
    private fun config(mode: String) = AppConfig(
        listOf(ApiProfile(id = "theme-test", baseUrl = "https://example.com")),
        "theme-test", "theme-test",
        themeMode = mode
    )

    @Test fun freshInstallDefaultsToLight() {
        val context = RuntimeEnvironment.getApplication()
        assertEquals(THEME_MODE_ASTER, ConfigStore(context).load().themeMode)
    }

    @Test fun darkAndSystemModesSurviveStoreReload() {
        val context = RuntimeEnvironment.getApplication()
        ConfigStore(context).save(config(THEME_MODE_DARK))
        assertEquals(THEME_MODE_DARK, ConfigStore(context).load().themeMode)
        ConfigStore(context).save(config(THEME_MODE_SYSTEM))
        assertEquals(THEME_MODE_SYSTEM, ConfigStore(context).load().themeMode)
    }

    @Test fun fontWeightPersistsAndClampsToSupportedRange() {
        val context = RuntimeEnvironment.getApplication()
        val store = ConfigStore(context)
        store.save(config(THEME_MODE_ASTER).copy(fontWeight = 550))
        assertEquals(550, store.load().fontWeight)

        val prefs = context.getSharedPreferences("adchat_api_config", Context.MODE_PRIVATE)
        val raw = prefs.getString("appConfigV2", null).orEmpty()
        prefs.edit()
            .putString("appConfigV2", raw.replace("\"fontWeight\":550", "\"fontWeight\":999"))
            .commit()
        assertEquals(FONT_WEIGHT_MAX, store.load().fontWeight)
    }

    @Test fun unknownStoredValueFallsBackToLight() {
        val context = RuntimeEnvironment.getApplication()
        val store = ConfigStore(context)
        store.save(config(THEME_MODE_DARK))
        val prefs = context.getSharedPreferences("adchat_api_config", Context.MODE_PRIVATE)
        val raw = prefs.getString("appConfigV2", null).orEmpty()
        assertTrue(raw.contains("\"themeMode\":\"" + THEME_MODE_DARK + "\""))
        prefs.edit()
            .putString("appConfigV2", raw.replace("\"themeMode\":\"" + THEME_MODE_DARK + "\"", "\"themeMode\":\"weird\""))
            .commit()
        assertEquals(THEME_MODE_ASTER, store.load().themeMode)
    }
}
