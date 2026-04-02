package com.bloodpressure.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.bloodpressure.app.data.local.dao.BloodPressureDao
import com.bloodpressure.app.data.local.entity.BloodPressureRecordEntity
import com.bloodpressure.app.domain.model.BloodPressureRecord
import com.bloodpressure.app.domain.model.Period
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bloodPressureDao: BloodPressureDao
) {
    suspend fun exportToCsv(): Uri? = withContext(Dispatchers.IO) {
        try {
            val records = bloodPressureDao.getAllRecords().first()
            val csvContent = buildString {
                appendLine("日期,时段,高压,低压,心率,备注,创建时间")
                records.forEach { record ->
                    val period = if (record.period == "MORNING") "早上" else "晚上"
                    val note = record.note?.replace(",", ";") ?: ""
                    val heartRate = record.heartRate?.toString() ?: ""
                    val createdAt = record.createdAt
                    appendLine("${record.date},$period,${record.systolic},${record.diastolic},$heartRate,$note,$createdAt")
                }
            }

            val fileName = "blood_pressure_${System.currentTimeMillis()}.csv"
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val file = File(exportDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(csvContent)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importFromCsv(uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext 0
            val lines = inputStream.bufferedReader().readLines()
            inputStream.close()

            var importedCount = 0
            for (i in lines.indices) {
                if (i == 0) continue
                val line = lines[i]
                if (line.isBlank()) continue

                val parts = line.split(",")
                if (parts.size < 4) continue

                try {
                    val date = parts[0].trim()
                    val period = if (parts[1].trim() == "早上") "MORNING" else "EVENING"
                    val systolic = parts[2].trim().toIntOrNull() ?: continue
                    val diastolic = parts[3].trim().toIntOrNull() ?: continue
                    val heartRate = parts.getOrNull(4)?.trim()?.toIntOrNull()
                    val note = parts.getOrNull(5)?.trim()?.ifBlank { null }

                    val record = BloodPressureRecordEntity(
                        date = date,
                        period = period,
                        systolic = systolic,
                        diastolic = diastolic,
                        heartRate = heartRate,
                        note = note,
                        createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        updatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        syncStatus = "LOCAL",
                        syncTime = null
                    )

                    bloodPressureDao.insertRecord(record)
                    importedCount++
                } catch (e: Exception) {
                    continue
                }
            }
            importedCount
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}