package com.bloodpressure.app.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.data.preferences.SettingsRepository
import com.bloodpressure.app.data.remote.FeishuService
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.Period
import com.bloodpressure.app.domain.model.determinePeriod
import com.bloodpressure.app.domain.usecase.GetRecordByDateAndPeriodUseCase
import com.bloodpressure.app.domain.usecase.SaveRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class RecordUiState(
    val date: LocalDate = LocalDate.now(),
    val period: Period = Period.MORNING,
    val systolic: String = "",
    val diastolic: String = "",
    val heartRate: String = "",
    val note: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: String? = null
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val saveRecordUseCase: SaveRecordUseCase,
    private val getRecordByDateAndPeriodUseCase: GetRecordByDateAndPeriodUseCase,
    private val settingsRepository: SettingsRepository,
    private val feishuService: FeishuService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun initialize(dateStr: String, periodStr: String) {
        val date = try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }
        
        val period = if (periodStr == "auto" || periodStr.isEmpty()) {
            determinePeriod(LocalTime.now().hour)
        } else {
            try {
                Period.valueOf(periodStr)
            } catch (e: Exception) {
                determinePeriod(LocalTime.now().hour)
            }
        }

        _uiState.value = _uiState.value.copy(date = date, period = period)

        viewModelScope.launch {
            val existingRecord = getRecordByDateAndPeriodUseCase(date, period)
            existingRecord?.let { record ->
                _uiState.value = _uiState.value.copy(
                    systolic = record.systolic.toString(),
                    diastolic = record.diastolic.toString(),
                    heartRate = record.heartRate?.toString() ?: "",
                    note = record.note ?: "",
                    isEditing = true
                )
            }
        }
    }

    fun updateSystolic(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(systolic = value)
        }
    }

    fun updateDiastolic(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(diastolic = value)
        }
    }

    fun updateHeartRate(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(heartRate = value)
        }
    }

    fun updateNote(value: String) {
        _uiState.value = _uiState.value.copy(note = value)
    }

    fun saveRecord() {
        val state = _uiState.value
        val systolic = state.systolic.toIntOrNull()
        val diastolic = state.diastolic.toIntOrNull()

        if (systolic == null || diastolic == null) {
            _uiState.value = state.copy(errorMessage = "请输入有效的血压值")
            return
        }

        if (systolic < 60 || systolic > 250 || diastolic < 40 || diastolic > 150) {
            _uiState.value = state.copy(errorMessage = "血压值超出合理范围")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            try {
                val record = BloodPressureRecord(
                    date = state.date,
                    period = state.period,
                    systolic = systolic,
                    diastolic = diastolic,
                    heartRate = state.heartRate.toIntOrNull(),
                    note = state.note.ifBlank { null },
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                saveRecordUseCase(record)

                val syncEnabled = settingsRepository.syncEnabled.first()
                val appId = settingsRepository.feishuAppId.first()
                val appSecret = settingsRepository.feishuAppSecret.first()
                val tableToken = settingsRepository.feishuTableToken.first()

                if (syncEnabled && appId.isNotEmpty() && appSecret.isNotEmpty() && tableToken.isNotEmpty()) {
                    val syncResult = feishuService.syncToTable(appId, appSecret, tableToken, record)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true,
                        syncStatus = if (syncResult is com.bloodpressure.app.data.remote.FeishuResult.Success) 
                            "已同步到飞书" else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存失败: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
