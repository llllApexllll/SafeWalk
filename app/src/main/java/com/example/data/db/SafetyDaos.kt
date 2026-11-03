package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EmergencyContactEntity
import com.example.data.model.SafetyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyReportDao {
  @Query("SELECT * FROM safety_reports ORDER BY id DESC")
  fun getAllReports(): Flow<List<SafetyReportEntity>>

  @Query("SELECT * FROM safety_reports WHERE isLightingRelated = 1 ORDER BY id DESC")
  fun getLightingReports(): Flow<List<SafetyReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: SafetyReportEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReports(reports: List<SafetyReportEntity>)

  @Query("UPDATE safety_reports SET upvotes = upvotes + 1 WHERE id = :reportId")
  suspend fun upvoteReport(reportId: Long)

  @Query("SELECT COUNT(*) FROM safety_reports")
  suspend fun getReportCount(): Int
}

@Dao
interface EmergencyContactDao {
  @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, id ASC")
  fun getAllContacts(): Flow<List<EmergencyContactEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContact(contact: EmergencyContactEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContacts(contacts: List<EmergencyContactEntity>)

  @Update
  suspend fun updateContact(contact: EmergencyContactEntity)

  @Delete
  suspend fun deleteContact(contact: EmergencyContactEntity)

  @Query("SELECT COUNT(*) FROM emergency_contacts")
  suspend fun getContactCount(): Int
}
