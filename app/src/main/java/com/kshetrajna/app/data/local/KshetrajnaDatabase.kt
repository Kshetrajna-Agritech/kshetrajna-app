package com.kshetrajna.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kshetrajna.app.data.local.converter.Converters
import com.kshetrajna.app.data.local.dao.CropProfileDao
import com.kshetrajna.app.data.local.dao.FarmDao
import com.kshetrajna.app.data.local.dao.IrrigationDao
import com.kshetrajna.app.data.local.dao.ManualPHDao
import com.kshetrajna.app.data.local.dao.NodeDao
import com.kshetrajna.app.data.local.dao.NpkResultDao
import com.kshetrajna.app.data.local.dao.SafetyDao
import com.kshetrajna.app.data.local.dao.SensorReadingDao
import com.kshetrajna.app.data.local.dao.SoilAnalysisDao
import com.kshetrajna.app.data.local.dao.SyncRecordDao
import com.kshetrajna.app.data.local.dao.WeatherDataDao
import com.kshetrajna.app.data.local.entity.AlertEntity
import com.kshetrajna.app.data.local.entity.CropProfileEntity
import com.kshetrajna.app.data.local.entity.FarmEntity
import com.kshetrajna.app.data.local.entity.IrrigationCommandEntity
import com.kshetrajna.app.data.local.entity.IrrigationStateEntity
import com.kshetrajna.app.data.local.entity.ManualPHEntity
import com.kshetrajna.app.data.local.entity.NodeEntity
import com.kshetrajna.app.data.local.entity.NpkResultEntity
import com.kshetrajna.app.data.local.entity.SafetyStateEntity
import com.kshetrajna.app.data.local.entity.SensorReadingEntity
import com.kshetrajna.app.data.local.entity.SoilAnalysisEntity
import com.kshetrajna.app.data.local.entity.SyncRecordEntity
import com.kshetrajna.app.data.local.entity.WeatherDataEntity

/**
 * Core Room database for Kshetrajna offline-first architecture.
 */
@Database(
    entities = [
        FarmEntity::class,
        CropProfileEntity::class,
        NodeEntity::class,
        SensorReadingEntity::class,
        ManualPHEntity::class,
        WeatherDataEntity::class,
        SoilAnalysisEntity::class,
        NpkResultEntity::class,
        IrrigationStateEntity::class,
        IrrigationCommandEntity::class,
        SafetyStateEntity::class,
        AlertEntity::class,
        SyncRecordEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KshetrajnaDatabase : RoomDatabase() {

    abstract fun farmDao(): FarmDao
    abstract fun cropProfileDao(): CropProfileDao
    abstract fun nodeDao(): NodeDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun manualPHDao(): ManualPHDao
    abstract fun weatherDataDao(): WeatherDataDao
    abstract fun soilAnalysisDao(): SoilAnalysisDao
    abstract fun npkResultDao(): NpkResultDao
    abstract fun irrigationDao(): IrrigationDao
    abstract fun safetyDao(): SafetyDao
    abstract fun syncRecordDao(): SyncRecordDao

    companion object {
        private const val DATABASE_NAME = "kshetrajna.db"

        @Volatile
        private var INSTANCE: KshetrajnaDatabase? = null

        fun getInstance(context: Context): KshetrajnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KshetrajnaDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
