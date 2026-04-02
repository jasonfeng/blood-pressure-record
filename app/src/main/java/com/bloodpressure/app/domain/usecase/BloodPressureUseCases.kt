package com.bloodpressure.app.domain.usecase

import com.bloodpressure.app.data.repository.BloodPressureRepository
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.Period
import com.bloodpressure.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class GetRecordsUseCase @Inject constructor(
    private val repository: BloodPressureRepository
) {
    fun getAllRecords(): Flow<List<BloodPressureRecord>> = repository.getAllRecords()

    fun getRecordsByDate(date: LocalDate): Flow<List<BloodPressureRecord>> = 
        repository.getRecordsByDate(date)

    fun getRecordsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<BloodPressureRecord>> =
        repository.getRecordsByDateRange(startDate, endDate)
}

class SaveRecordUseCase @Inject constructor(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(record: BloodPressureRecord): Long {
        val now = LocalDateTime.now()
        val recordToSave = record.copy(
            updatedAt = now,
            createdAt = if (record.id == 0L) now else record.createdAt,
            syncStatus = SyncStatus.PENDING
        )
        return repository.saveRecord(recordToSave)
    }
}

class DeleteRecordUseCase @Inject constructor(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(record: BloodPressureRecord) {
        repository.deleteRecord(record)
    }
}

class GetTodayRecordsUseCase @Inject constructor(
    private val repository: BloodPressureRepository
) {
    operator fun invoke(): Flow<List<BloodPressureRecord>> {
        return repository.getRecordsByDate(LocalDate.now())
    }
}

class GetRecordByDateAndPeriodUseCase @Inject constructor(
    private val repository: BloodPressureRepository
) {
    suspend operator fun invoke(date: LocalDate, period: Period): BloodPressureRecord? {
        return repository.getRecordByDateAndPeriod(date, period)
    }
}
