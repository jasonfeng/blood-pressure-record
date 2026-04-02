package com.bloodpressure.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.Period
import com.bloodpressure.app.domain.model.SyncStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "blood_pressure_records")
data class BloodPressureRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val period: String,
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int?,
    val note: String?,
    val syncStatus: String,
    val syncTime: String?,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): BloodPressureRecord {
        return BloodPressureRecord(
            id = id,
            date = LocalDate.parse(date),
            period = Period.valueOf(period),
            systolic = systolic,
            diastolic = diastolic,
            heartRate = heartRate,
            note = note,
            syncStatus = SyncStatus.valueOf(syncStatus),
            syncTime = syncTime?.let { LocalDateTime.parse(it) },
            createdAt = LocalDateTime.parse(createdAt),
            updatedAt = LocalDateTime.parse(updatedAt)
        )
    }

    companion object {
        fun fromDomain(record: BloodPressureRecord): BloodPressureRecordEntity {
            return BloodPressureRecordEntity(
                id = record.id,
                date = record.date.toString(),
                period = record.period.name,
                systolic = record.systolic,
                diastolic = record.diastolic,
                heartRate = record.heartRate,
                note = record.note,
                syncStatus = record.syncStatus.name,
                syncTime = record.syncTime?.toString(),
                createdAt = record.createdAt.toString(),
                updatedAt = record.updatedAt.toString()
            )
        }
    }
}
