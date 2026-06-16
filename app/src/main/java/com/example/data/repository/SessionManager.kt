package com.example.data.repository

import android.content.Context
import android.provider.Settings
import java.util.UUID

class SessionManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("wolf_trader_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_ACTIVATED = "is_activated"
        private const val KEY_PLAN = "activation_plan"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_FAVORITES = "favorite_pairs"
        private const val KEY_API_URL = "api_url"
        private const val DEFAULT_API_URL = "https://YOUR_API_SERVER.com"
        private const val KEY_DEMO_MODE = "demo_mode_active"
        private const val KEY_LANG = "app_language"
    }

    init {
        // Enforce a cached stable unique device id
        if (prefs.getString(KEY_DEVICE_ID, null) == null) {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val stableDeviceId = if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
                UUID.randomUUID().toString().substring(0, 12)
            } else {
                androidId
            }
            prefs.edit().putString(KEY_DEVICE_ID, stableDeviceId).apply()
        }
        // Force-enable activated status and premium plan for instant preview access
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, true)
            .putString(KEY_PLAN, "pro")
            .apply()
    }

    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, "WOLF_DEVICE_001") ?: "WOLF_DEVICE_001"

    var isActivated: Boolean
        get() = prefs.getBoolean(KEY_IS_ACTIVATED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_ACTIVATED, value).apply()

    var activationPlan: String
        get() = prefs.getString(KEY_PLAN, "pro") ?: "pro"
        set(value) = prefs.edit().putString(KEY_PLAN, value).apply()

    var apiUrl: String
        get() = prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        set(value) = prefs.edit().putString(KEY_API_URL, value).apply()

    var isDemoModeMode: Boolean
        get() = prefs.getBoolean(KEY_DEMO_MODE, true) // Default to true so users see working signals off-the-shelf
        set(value) = prefs.edit().putBoolean(KEY_DEMO_MODE, value).apply()

    var appLanguage: String
        get() = prefs.getString(KEY_LANG, "ar") ?: "ar"
        set(value) = prefs.edit().putString(KEY_LANG, value).apply()

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(pair: String) {
        val current = getFavorites().toMutableSet()
        if (current.contains(pair)) {
            current.remove(pair)
        } else {
            current.add(pair)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, false)
            .putString(KEY_PLAN, "")
            .apply()
    }
}
