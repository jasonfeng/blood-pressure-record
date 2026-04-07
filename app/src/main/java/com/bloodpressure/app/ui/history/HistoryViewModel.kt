package com.bloodpressure.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.usecase.DeleteRecordUseCase
import com.bloodpressure.app.domain.usecase.GetRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TimeRange(val label: String, val days: Int) {
    TODAY("今日", 0),
    YESTERDAY("昨日", 1),
    WEEK("近一周", 7),
    MONTH("近一个月", 30),
    THREE_MONTHS("近三个月", 90)
}

data class HistoryUiState(
    val records: List<BloodPressureRecord> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val recordToDelete: BloodPressureRecord? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getRecordsUseCase: GetRecordsUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadRecords(TimeRange.WEEK)
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange, isLoading = true)
        loadRecords(timeRange)
    }

    private fun loadRecords(timeRange: TimeRange) {
        val today = LocalDate.now()
        val (startDate, endDate) = when (timeRange) {
            TimeRange.TODAY -> today to today
            TimeRange.YESTERDAY -> today.minusDays(1) to today.minusDays(1)
            else -> today.minusDays(timeRange.days.toLong()) to today
        }

        getRecordsUseCase.getRecordsByDateRange(startDate, endDate)
            .onEach { records ->
                _uiState.value = _uiState.value.copy(
                    records = records,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun showDeleteConfirmation(record: BloodPressureRecord) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            recordToDelete = record
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            recordToDelete = null
        )
    }

    fun confirmDelete() {
        val record = _uiState.value.recordToDelete ?: return
        viewModelScope.launch {
            deleteRecordUseCase(record)
            _uiState.value = _uiState.value.copy(
                showDeleteDialog = false,
                recordToDelete = null
            )
            loadRecords(_uiState.value.selectedTimeRange)
        }
    }
}
