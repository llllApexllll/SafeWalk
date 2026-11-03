package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReportCategory(val displayName: String, val iconName: String) {
  BROKEN_LIGHTING("Broken Street Light", "ic_light_off"),
  DARK_ALLEY("Unlit & Isolated Alley", "ic_dark_alley"),
  SUSPICIOUS_ACTIVITY("Suspicious Activity", "ic_suspicious"),
  HARASSMENT("Harassment Reported", "ic_alert"),
  HIGH_CRIME_SPOT("Recent Crime Incident", "ic_crime"),
  POLICE_PATROL("Active Police / Security", "ic_police"),
  SAFE_HAVEN_24_7("24/7 Safe Haven / Open Store", "ic_haven"),
  WELL_LIT_CORRIDOR("Well-Lit Safe Corridor", "ic_sun")
}

enum class SeverityLevel(val label: String) {
  LOW("Low Risk / Note"),
  MEDIUM("Caution Required"),
  HIGH("High Danger / Avoid")
}

@Entity(tableName = "safety_reports")
data class SafetyReportEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val description: String,
  val category: ReportCategory,
  val severity: SeverityLevel,
  val latitude: Double,
  val longitude: Double,
  val addressName: String,
  val timestampFormatted: String,
  val upvotes: Int = 1,
  val isVerified: Boolean = false,
  val isLightingRelated: Boolean = false,
  val userReported: Boolean = false
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val phoneNumber: String,
  val relationship: String,
  val isPrimary: Boolean = false,
  val notifyOnSos: Boolean = true
)

data class HazardZone(
  val id: String,
  val name: String,
  val description: String,
  val dangerType: String,
  val lat: Double,
  val lng: Double,
  val radiusMeters: Float,
  val riskScore: Int, // 0 - 100
  val isUnlit: Boolean
)

enum class SafeHavenType {
  POLICE_STATION,
  PHARMACY_24_7,
  CONVENIENCE_STORE_24_7,
  CAMPUS_SECURITY,
  HOSPITAL_ER,
  FIRE_STATION
}

data class SafeHaven(
  val id: String,
  val name: String,
  val type: SafeHavenType,
  val address: String,
  val lat: Double,
  val lng: Double,
  val isOpenNow: Boolean = true,
  val staffPresent: Boolean = true,
  val phone: String = ""
)
