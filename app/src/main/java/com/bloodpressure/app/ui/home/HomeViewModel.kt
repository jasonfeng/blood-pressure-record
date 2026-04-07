package com.bloodpressure.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.Period
import com.bloodpressure.app.domain.usecase.GetRecordsUseCase
import com.bloodpressure.app.domain.usecase.GetTodayRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val todayRecords: List<BloodPressureRecord> = emptyList(),
    val weekRecords: List<BloodPressureRecord> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayRecordsUseCase: GetTodayRecordsUseCase,
    private val getRecordsUseCase: GetRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayRecords()
        loadWeekRecords()
    }

    private fun loadTodayRecords() {
        getTodayRecordsUseCase()
            .onEach { records ->
                _uiState.value = _uiState.value.copy(
                    todayRecords = records,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadWeekRecords() {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(6)
        getRecordsUseCase.getRecordsByDateRange(startDate, endDate)
            .onEach { records ->
                _uiState.value = _uiState.value.copy(weekRecords = records)
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadTodayRecords()
        loadWeekRecords()
    }

    fun getMorningRecord(): BloodPressureRecord? {
        return _uiState.value.todayRecords.firstOrNull { it.period == Period.MORNING }
    }

    fun getEveningRecord(): BloodPressureRecord? {
        return _uiState.value.todayRecords.firstOrNull { it.period == Period.EVENING }
    }
}
