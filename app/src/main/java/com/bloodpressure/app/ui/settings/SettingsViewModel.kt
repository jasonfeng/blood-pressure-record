package com.bloodpressure.app.ui.settings

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.alarm.AlarmScheduler
import com.bloodpressure.app.data.preferences.SettingsRepository
import com.bloodpressure.app.data.remote.FeishuResult
import com.bloodpressure.app.data.remote.FeishuService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val isFeishuConnected: Boolean = false,
    val syncEnabled: Boolean = false,
    val feishuAppId: String = "",
    val feishuAppSecret: String = "",
    val feishuTableToken: String = "",
    val morningReminderEnabled: Boolean = false,
    val morningReminderTime: String = "09:00",
    val eveningReminderEnabled: Boolean = false,
    val eveningReminderTime: String = "21:00",
    val darkMode: String = "跟随系统",
    val isLoading: Boolean = false,
    val showFeishuDialog: Boolean = false,
    val showTimePickerDialog: Boolean = false,
    val editingReminderType: String = "",
    val connectionStatus: String = "",
    val needsNotificationPermission: Boolean = false,
    val pendingReminderToggle: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val feishuService: FeishuService,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private var activity: Activity? = null

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            val pending = _uiState.value.pendingReminderToggle
            when (pending) {
                "morning" -> toggleMorningReminderInternal(true)
                "evening" -> toggleEveningReminderInternal(true)
            }
        }
        _uiState.value = _uiState.value.copy(
            pendingReminderToggle = "",
            needsNotificationPermission = false
        )
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            settingsRepository.isFeishuConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(isFeishuConnected = isConnected)
            }
        }
        viewModelScope.launch {
            settingsRepository.syncEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(syncEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.feishuAppId.collect { id ->
                _uiState.value = _uiState.value.copy(feishuAppId = id)
            }
        }
        viewModelScope.launch {
            settingsRepository.feishuAppSecret.collect { secret ->
                _uiState.value = _uiState.value.copy(feishuAppSecret = secret)
            }
        }
        viewModelScope.launch {
            settingsRepository.feishuTableToken.collect { token ->
                _uiState.value = _uiState.value.copy(feishuTableToken = token)
            }
        }
        viewModelScope.launch {
            settingsRepository.morningReminderEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(morningReminderEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.morningReminderEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(morningReminderEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.morningReminderTime.collect { time ->
                _uiState.value = _uiState.value.copy(morningReminderTime = time)
            }
        }
        viewModelScope.launch {
            settingsRepository.eveningReminderEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(eveningReminderEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.eveningReminderTime.collect { time ->
                _uiState.value = _uiState.value.copy(eveningReminderTime = time)
            }
        }
        viewModelScope.launch {
            settingsRepository.darkMode.collect { mode ->
                _uiState.value = _uiState.value.copy(darkMode = mode)
            }
        }
    }

    fun toggleSync(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSyncEnabled(enabled)
        }
    }

    fun showFeishuDialog() {
        _uiState.value = _uiState.value.copy(showFeishuDialog = true, connectionStatus = "")
    }

    fun dismissFeishuDialog() {
        _uiState.value = _uiState.value.copy(showFeishuDialog = false)
    }

    fun updateFeishuAppId(id: String) {
        _uiState.value = _uiState.value.copy(feishuAppId = id)
    }

    fun updateFeishuAppSecret(secret: String) {
        _uiState.value = _uiState.value.copy(feishuAppSecret = secret)
    }

    fun updateFeishuTableToken(token: String) {
        _uiState.value = _uiState.value.copy(feishuTableToken = token)
    }

    fun saveFeishuConfig() {
        val appId = _uiState.value.feishuAppId
        val appSecret = _uiState.value.feishuAppSecret
        val tableToken = _uiState.value.feishuTableToken

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, connectionStatus = "正在连接飞书...")

            val result = feishuService.testConnection(appId, appSecret, tableToken)

            when (result) {
                is FeishuResult.Success -> {
                    settingsRepository.saveFeishuConfig(appId, appSecret, tableToken)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showFeishuDialog = false,
                        isFeishuConnected = true,
                        connectionStatus = "连接成功"
                    )
                }
                is FeishuResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        connectionStatus = result.message
                    )
                }
            }
        }
    }

    fun disconnectFeishu() {
        viewModelScope.launch {
            settingsRepository.clearFeishuConfig()
            feishuService.clearCache()
        }
    }

    fun showTimePicker(type: String) {
        _uiState.value = _uiState.value.copy(
            showTimePickerDialog = true,
            editingReminderType = type
        )
    }

    fun dismissTimePicker() {
        _uiState.value = _uiState.value.copy(
            showTimePickerDialog = false,
            editingReminderType = ""
        )
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        val time = String.format("%02d:%02d", hour, minute)
        viewModelScope.launch {
            when (_uiState.value.editingReminderType) {
                "morning" -> settingsRepository.setMorningReminderTime(time)
                "evening" -> settingsRepository.setEveningReminderTime(time)
            }
            _uiState.value = _uiState.value.copy(
                showTimePickerDialog = false,
                editingReminderType = ""
            )
        }
    }

    fun toggleMorningReminder(enabled: Boolean) {
        if (enabled && !checkNotificationPermission()) {
            _uiState.value = _uiState.value.copy(
                pendingReminderToggle = "morning",
                needsNotificationPermission = true
            )
            return
        }
        toggleMorningReminderInternal(enabled)
    }

    private fun toggleMorningReminderInternal(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMorningReminderEnabled(enabled)
            if (enabled) {
                val time = _uiState.value.morningReminderTime
                val parts = time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                alarmScheduler.scheduleExactAlarm(
                    hour, minute,
                    AlarmScheduler.REQUEST_CODE_MORNING,
                    "测量血压提醒",
                    "早上好，该测量血压了"
                )
            } else {
                alarmScheduler.cancelAlarm(AlarmScheduler.REQUEST_CODE_MORNING)
            }
        }
    }

    fun toggleEveningReminder(enabled: Boolean) {
        if (enabled && !checkNotificationPermission()) {
            _uiState.value = _uiState.value.copy(
                pendingReminderToggle = "evening",
                needsNotificationPermission = true
            )
            return
        }
        toggleEveningReminderInternal(enabled)
    }

    private fun toggleEveningReminderInternal(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEveningReminderEnabled(enabled)
            if (enabled) {
                val time = _uiState.value.eveningReminderTime
                val parts = time.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                alarmScheduler.scheduleExactAlarm(
                    hour, minute,
                    AlarmScheduler.REQUEST_CODE_EVENING,
                    "测量血压提醒",
                    "晚上好，该测量血压了"
                )
            } else {
                alarmScheduler.cancelAlarm(AlarmScheduler.REQUEST_CODE_EVENING)
            }
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } ?: false
        } else {
            true
        }
    }
}