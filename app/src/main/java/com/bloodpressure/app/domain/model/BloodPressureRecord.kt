package com.bloodpressure.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class Period {
    MORNING,
    EVENING
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

data class BloodPressureRecord(
    val id: Long = 0,
    val date: LocalDate,
    val period: Period,
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int? = null,
    val note: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val syncTime: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

fun determinePeriod(hour: Int): Period {
    return when (hour) {
        in 4..11 -> Period.MORNING
        in 17..22 -> Period.EVENING
        else -> if (hour < 12) Period.MORNING else Period.EVENING
    }
}
