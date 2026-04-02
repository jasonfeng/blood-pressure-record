package com.bloodpressure.app.data.repository

import com.bloodpressure.app.data.local.dao.BloodPressureDao
import com.bloodpressure.app.data.local.entity.BloodPressureRecordEntity
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloodPressureRepository @Inject constructor(
    private val bloodPressureDao: BloodPressureDao
) {
    fun getAllRecords(): Flow<List<BloodPressureRecord>> {
        return bloodPressureDao.getAllRecords().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRecordsByDate(date: LocalDate): Flow<List<BloodPressureRecord>> {
        return bloodPressureDao.getRecordsByDate(date.toString()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRecordsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<BloodPressureRecord>> {
        return bloodPressureDao.getRecordsByDateRange(startDate.toString(), endDate.toString()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getRecordByDateAndPeriod(date: LocalDate, period: com.bloodpressure.app.domain.model.Period): BloodPressureRecord? {
        return bloodPressureDao.getRecordByDateAndPeriod(date.toString(), period.name)?.toDomain()
    }

    suspend fun getPendingSyncRecords(): List<BloodPressureRecord> {
        return bloodPressureDao.getRecordsBySyncStatus(SyncStatus.PENDING.name).map { it.toDomain() }
    }

    suspend fun saveRecord(record: BloodPressureRecord): Long {
        return bloodPressureDao.insertRecord(BloodPressureRecordEntity.fromDomain(record))
    }

    suspend fun updateRecord(record: BloodPressureRecord) {
        bloodPressureDao.updateRecord(BloodPressureRecordEntity.fromDomain(record))
    }

    suspend fun deleteRecord(record: BloodPressureRecord) {
        bloodPressureDao.deleteRecord(BloodPressureRecordEntity.fromDomain(record))
    }

    suspend fun deleteAllRecords() {
        bloodPressureDao.deleteAllRecords()
    }

    suspend fun updateSyncStatus(recordId: Long, status: SyncStatus) {
        val record = bloodPressureDao.getRecordsBySyncStatus(SyncStatus.PENDING.name)
            .find { it.id == recordId }
        record?.let {
            bloodPressureDao.updateRecord(it.copy(
                syncStatus = status.name,
                syncTime = if (status == SyncStatus.SYNCED) java.time.LocalDateTime.now().toString() else it.syncTime
            ))
        }
    }
}
