package com.bloodpressure.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        // 飞书配置
        val FEISHU_APP_ID = stringPreferencesKey("feishu_app_id")
        val FEISHU_APP_SECRET = stringPreferencesKey("feishu_app_secret")
        val FEISHU_TABLE_TOKEN = stringPreferencesKey("feishu_table_token")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        
        // 提醒设置
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_REMINDER_TIME = stringPreferencesKey("morning_reminder_time")
        val EVENING_REMINDER_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_REMINDER_TIME = stringPreferencesKey("evening_reminder_time")
        
        // 显示设置
        val DARK_MODE = stringPreferencesKey("dark_mode")
    }

    // 飞书配置
    val feishuAppId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FEISHU_APP_ID] ?: ""
    }

    val feishuAppSecret: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FEISHU_APP_SECRET] ?: ""
    }

    val feishuTableToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FEISHU_TABLE_TOKEN] ?: ""
    }

    val isFeishuConnected: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val appId = preferences[PreferencesKeys.FEISHU_APP_ID] ?: ""
        val secret = preferences[PreferencesKeys.FEISHU_APP_SECRET] ?: ""
        val token = preferences[PreferencesKeys.FEISHU_TABLE_TOKEN] ?: ""
        appId.isNotEmpty() && secret.isNotEmpty() && token.isNotEmpty()
    }

    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_ENABLED] ?: false
    }

    // 提醒设置
    val morningReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] ?: false
    }
    val morningReminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MORNING_REMINDER_TIME] ?: "09:00"
    }
    val eveningReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EVENING_REMINDER_ENABLED] ?: false
    }
    val eveningReminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EVENING_REMINDER_TIME] ?: "21:00"
    }

    // 显示设置
    val darkMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: "跟随系统"
    }

    // 保存飞书配置
    suspend fun saveFeishuConfig(appId: String, appSecret: String, tableToken: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FEISHU_APP_ID] = appId
            preferences[PreferencesKeys.FEISHU_APP_SECRET] = appSecret
            preferences[PreferencesKeys.FEISHU_TABLE_TOKEN] = tableToken
        }
    }

    suspend fun clearFeishuConfig() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.FEISHU_APP_ID)
            preferences.remove(PreferencesKeys.FEISHU_APP_SECRET)
            preferences.remove(PreferencesKeys.FEISHU_TABLE_TOKEN)
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_ENABLED] = enabled
        }
    }

    suspend fun setMorningReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_REMINDER_TIME] = time
        }
    }
    
    suspend fun setMorningReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setEveningReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_REMINDER_TIME] = time
        }
    }
    
    suspend fun setEveningReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = mode
        }
    }
}