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

    @Test fun freshInstallDefaultsToAster() {
        val context = RuntimeEnvironment.getApplication()
        assertEquals(THEME_MODE_ASTER, ConfigStore(context).load().themeMode)
    }

    @Test fun glassModeSurvivesStoreReload() {
        val context = RuntimeEnvironment.getApplication()
        ConfigStore(context).save(config(THEME_MODE_GLASS))
        assertEquals(THEME_MODE_GLASS, ConfigStore(context).load().themeMode)
    }

    @Test fun unknownStoredValueFallsBackToAster() {
        val context = RuntimeEnvironment.getApplication()
        val store = ConfigStore(context)
        store.save(config(THEME_MODE_GLASS))
        val prefs = context.getSharedPreferences("adchat_api_config", Context.MODE_PRIVATE)
        val raw = prefs.getString("appConfigV2", null).orEmpty()
        assertTrue(raw.contains("\"themeMode\":\"" + THEME_MODE_GLASS + "\""))
        prefs.edit()
            .putString("appConfigV2", raw.replace("\"themeMode\":\"" + THEME_MODE_GLASS + "\"", "\"themeMode\":\"weird\""))
            .commit()
        assertEquals(THEME_MODE_ASTER, store.load().themeMode)
    }
}
