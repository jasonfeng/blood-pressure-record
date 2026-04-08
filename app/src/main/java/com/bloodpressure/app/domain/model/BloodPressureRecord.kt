package com.bloodpressure.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class Period {
    MORNING,
    EVENING
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

enum class BpLevel(val label: String, val colorHex: String, val description: String) {
    NORMAL("正常", "#4CAF50", "血压在正常范围内，请继续保持"),
    ELEVATED("偏高", "#FFC107", "血压偏高，建议改善生活方式"),
    STAGE1("1期高血压", "#FF9800", "血压偏高，请咨询医生"),
    STAGE2("2期高血压", "#F44336", "血压较高，需要医疗干预"),
    CRISIS("危机", "#D32F2F", "立即就医！血压极度升高")
}

fun BpLevel.toComposeColor(): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(this.colorHex))
}

fun classifyBloodPressure(systolic: Int, diastolic: Int): BpLevel {
    return when {
        systolic > 180 || diastolic > 120 -> BpLevel.CRISIS
        systolic >= 140 || diastolic >= 90 -> BpLevel.STAGE2
        systolic >= 130 || diastolic >= 80 -> BpLevel.STAGE1
        systolic >= 120 && systolic < 130 && diastolic < 80 -> BpLevel.ELEVATED
        else -> BpLevel.NORMAL
    }
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
) {
    fun formattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return date.format(formatter)
    }
    
    fun formattedTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return createdAt.format(formatter)
    }

    fun bpLevel(): BpLevel = classifyBloodPressure(systolic, diastolic)
}

fun determinePeriod(hour: Int): Period {
    return when (hour) {
        in 4..11 -> Period.MORNING
        in 17..22 -> Period.EVENING
        else -> if (hour < 12) Period.MORNING else Period.EVENING
    }
}
