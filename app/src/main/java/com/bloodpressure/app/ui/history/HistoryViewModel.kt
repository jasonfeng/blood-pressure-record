package com.bloodpressure.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.usecase.GetRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getRecordsUseCase: GetRecordsUseCase
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
        val endDate = LocalDate.now()
        val startDate = when (timeRange) {
            TimeRange.TODAY -> endDate
            TimeRange.YESTERDAY -> endDate.minusDays(1)
            else -> endDate.minusDays(timeRange.days.toLong())
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
}
