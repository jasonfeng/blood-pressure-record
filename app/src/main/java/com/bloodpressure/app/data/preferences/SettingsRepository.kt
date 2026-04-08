package com.bloodpressure.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Encrypted preferences for sensitive data (feishu credentials)
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Regular preferences for non-sensitive settings
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // In-memory cache for flows
    private val _feishuAppId = MutableStateFlow(encryptedPrefs.getString(KEY_FEISHU_APP_ID, "") ?: "")
    private val _feishuAppSecret = MutableStateFlow(encryptedPrefs.getString(KEY_FEISHU_APP_SECRET, "") ?: "")
    private val _feishuTableToken = MutableStateFlow(encryptedPrefs.getString(KEY_FEISHU_TABLE_TOKEN, "") ?: "")
    private val _syncEnabled = MutableStateFlow(prefs.getBoolean(KEY_SYNC_ENABLED, false))

    private val _morningReminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_MORNING_REMINDER_ENABLED, false))
    private val _morningReminderTime = MutableStateFlow(prefs.getString(KEY_MORNING_REMINDER_TIME, "09:00") ?: "09:00")
    private val _eveningReminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_EVENING_REMINDER_ENABLED, false))
    private val _eveningReminderTime = MutableStateFlow(prefs.getString(KEY_EVENING_REMINDER_TIME, "21:00") ?: "21:00")
    private val _darkMode = MutableStateFlow(prefs.getString(KEY_DARK_MODE, "跟随系统") ?: "跟随系统")

    companion object {
        // Encrypted keys (sensitive)
        private const val KEY_FEISHU_APP_ID = "feishu_app_id"
        private const val KEY_FEISHU_APP_SECRET = "feishu_app_secret"
        private const val KEY_FEISHU_TABLE_TOKEN = "feishu_table_token"

        // Regular keys (non-sensitive)
        private const val KEY_SYNC_ENABLED = "sync_enabled"
        private const val KEY_MORNING_REMINDER_ENABLED = "morning_reminder_enabled"
        private const val KEY_MORNING_REMINDER_TIME = "morning_reminder_time"
        private const val KEY_EVENING_REMINDER_ENABLED = "evening_reminder_enabled"
        private const val KEY_EVENING_REMINDER_TIME = "evening_reminder_time"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    // Encrypted flows (sensitive - feishu credentials)
    val feishuAppId: Flow<String> = _feishuAppId.asStateFlow()
    val feishuAppSecret: Flow<String> = _feishuAppSecret.asStateFlow()
    val feishuTableToken: Flow<String> = _feishuTableToken.asStateFlow()

    val isFeishuConnected: Flow<Boolean> = _feishuAppId.map { appId ->
        val secret = _feishuAppSecret.value
        val token = _feishuTableToken.value
        appId.isNotEmpty() && secret.isNotEmpty() && token.isNotEmpty()
    }

    val syncEnabled: Flow<Boolean> = _syncEnabled.asStateFlow()

    // Reminder settings (non-sensitive)
    val morningReminderEnabled: Flow<Boolean> = _morningReminderEnabled.asStateFlow()
    val morningReminderTime: Flow<String> = _morningReminderTime.asStateFlow()
    val eveningReminderEnabled: Flow<Boolean> = _eveningReminderEnabled.asStateFlow()
    val eveningReminderTime: Flow<String> = _eveningReminderTime.asStateFlow()

    // Display settings (non-sensitive)
    val darkMode: Flow<String> = _darkMode.asStateFlow()

    // Save feishu config (encrypted)
    fun saveFeishuConfig(appId: String, appSecret: String, tableToken: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_FEISHU_APP_ID, appId)
            putString(KEY_FEISHU_APP_SECRET, appSecret)
            putString(KEY_FEISHU_TABLE_TOKEN, tableToken)
            apply()
        }
        _feishuAppId.value = appId
        _feishuAppSecret.value = appSecret
        _feishuTableToken.value = tableToken
    }

    fun clearFeishuConfig() {
        encryptedPrefs.edit().apply {
            remove(KEY_FEISHU_APP_ID)
            remove(KEY_FEISHU_APP_SECRET)
            remove(KEY_FEISHU_TABLE_TOKEN)
            apply()
        }
        _feishuAppId.value = ""
        _feishuAppSecret.value = ""
        _feishuTableToken.value = ""
    }

    fun setSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYNC_ENABLED, enabled).apply()
        _syncEnabled.value = enabled
    }

    fun setMorningReminderTime(time: String) {
        prefs.edit().putString(KEY_MORNING_REMINDER_TIME, time).apply()
        _morningReminderTime.value = time
    }

    fun setMorningReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MORNING_REMINDER_ENABLED, enabled).apply()
        _morningReminderEnabled.value = enabled
    }

    fun setEveningReminderTime(time: String) {
        prefs.edit().putString(KEY_EVENING_REMINDER_TIME, time).apply()
        _eveningReminderTime.value = time
    }

    fun setEveningReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EVENING_REMINDER_ENABLED, enabled).apply()
        _eveningReminderEnabled.value = enabled
    }

    fun setDarkMode(mode: String) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply()
        _darkMode.value = mode
    }
}