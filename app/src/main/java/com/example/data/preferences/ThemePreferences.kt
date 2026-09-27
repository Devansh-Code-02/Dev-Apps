package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

enum class UIStyleTheme {
    GLASSMORPHISM,
    NEOMORPHISM
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_STYLE_KEY = stringPreferencesKey("theme_style")
        private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        private val DEVICE_NAME_KEY = stringPreferencesKey("local_device_name")
        private val IS_DISCOVERABLE_KEY = booleanPreferencesKey("is_discoverable")
        private val LOCAL_MAC_KEY = stringPreferencesKey("local_mac_address")
        private val PUBLIC_KEY_KEY = stringPreferencesKey("local_public_key")
        private val PRIVATE_KEY_KEY = stringPreferencesKey("local_private_key")
    }

    val themeStyleFlow: Flow<UIStyleTheme> = context.dataStore.data.map { prefs ->
        val name = prefs[THEME_STYLE_KEY] ?: UIStyleTheme.GLASSMORPHISM.name
        try {
            UIStyleTheme.valueOf(name)
        } catch (e: Exception) {
            UIStyleTheme.GLASSMORPHISM
        }
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE_KEY] ?: true
    }

    val deviceNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEVICE_NAME_KEY] ?: "Speaker-Node-01"
    }

    val isDiscoverableFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DISCOVERABLE_KEY] ?: true
    }

    suspend fun setThemeStyle(style: UIStyleTheme) {
        context.dataStore.edit { prefs ->
            prefs[THEME_STYLE_KEY] = style.name
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE_KEY] = isDark
        }
    }

    suspend fun setDeviceName(newName: String) {
        context.dataStore.edit { prefs ->
            prefs[DEVICE_NAME_KEY] = newName
        }
    }

    suspend fun setDiscoverable(discoverable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DISCOVERABLE_KEY] = discoverable
        }
    }
}
