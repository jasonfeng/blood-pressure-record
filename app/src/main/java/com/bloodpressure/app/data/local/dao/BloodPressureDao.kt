package com.bloodpressure.app.data.local.dao

import androidx.room.*
import com.bloodpressure.app.data.local.entity.BloodPressureRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BloodPressureDao {
    @Query("SELECT * FROM blood_pressure_records ORDER BY date DESC, period DESC")
    fun getAllRecords(): Flow<List<BloodPressureRecordEntity>>

    @Query("SELECT * FROM blood_pressure_records WHERE date = :date ORDER BY period")
    fun getRecordsByDate(date: String): Flow<List<BloodPressureRecordEntity>>

    @Query("SELECT * FROM blood_pressure_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, period DESC")
    fun getRecordsByDateRange(startDate: String, endDate: String): Flow<List<BloodPressureRecordEntity>>

    @Query("SELECT * FROM blood_pressure_records WHERE date = :date AND period = :period LIMIT 1")
    suspend fun getRecordByDateAndPeriod(date: String, period: String): BloodPressureRecordEntity?

    @Query("SELECT * FROM blood_pressure_records WHERE syncStatus = :status")
    suspend fun getRecordsBySyncStatus(status: String): List<BloodPressureRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BloodPressureRecordEntity): Long

    @Update
    suspend fun updateRecord(record: BloodPressureRecordEntity)

    @Delete
    suspend fun deleteRecord(record: BloodPressureRecordEntity)

    @Query("DELETE FROM blood_pressure_records")
    suspend fun deleteAllRecords()
}
