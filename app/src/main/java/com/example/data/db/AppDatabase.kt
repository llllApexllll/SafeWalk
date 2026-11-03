package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.EmergencyContactEntity
import com.example.data.model.ReportCategory
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel

class Converters {
  @TypeConverter
  fun fromCategory(category: ReportCategory?): String? = category?.name

  @TypeConverter
  fun toCategory(name: String?): ReportCategory? =
    name?.let { runCatching { ReportCategory.valueOf(it) }.getOrDefault(ReportCategory.BROKEN_LIGHTING) }

  @TypeConverter
  fun fromSeverity(severity: SeverityLevel?): String? = severity?.name

  @TypeConverter
  fun toSeverity(name: String?): SeverityLevel? =
    name?.let { runCatching { SeverityLevel.valueOf(it) }.getOrDefault(SeverityLevel.MEDIUM) }
}

@Database(
  entities = [SafetyReportEntity::class, EmergencyContactEntity::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun safetyReportDao(): SafetyReportDao
  abstract fun emergencyContactDao(): EmergencyContactDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "safewalk_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
