package com.bloodpressure.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bloodpressure.app.data.local.dao.BloodPressureDao
import com.bloodpressure.app.data.local.entity.BloodPressureRecordEntity

@Database(
    entities = [BloodPressureRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BloodPressureDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
}
