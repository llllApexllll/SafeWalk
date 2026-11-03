package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppThemeMode(val displayName: String, val subtitle: String) {
  DARK("Dark Theme", "High-contrast night walking visual clarity (Recommended)"),
  LIGHT("Light Theme", "Bright high-daylight clarity"),
  SYSTEM("System Default", "Follows device Android system theme"),
  AUTO("Auto Night Switch", "Light during day, automatically switches to Dark after 18:00")
}

enum class ReportCategory(val displayName: String) {
  BROKEN_LIGHTING("Broken Street Light"),
  DARK_ALLEY("Unlit & Isolated Alley"),
  SUSPICIOUS_ACTIVITY("Suspicious Activity"),
  HARASSMENT("Harassment Reported"),
  ACCIDENT("Accident / Street Hazard"),
  SUDDEN_POLICE_ACTIVITY("Sudden Police Activity"),
  ENVIRONMENTAL_HAZARD("Environmental Hazard"),
  SAFE_HAVEN_24_7("24/7 Safe Haven / Open Store"),
  WELL_LIT_CORRIDOR("Well-Lit Safe Corridor")
}

enum class SeverityLevel(val label: String) {
  LOW("Low Risk / Note"),
  MEDIUM("Caution Required"),
  HIGH("High Danger / Avoid"),
  CRITICAL("CRITICAL IMMEDIATE THREAT")
}

enum class VerificationStatus {
  PENDING_REVIEW,
  VERIFIED,
  COMMUNITY_CONFIRMED
}

enum class AlertType(val displayName: String) {
  SUDDEN_POLICE_ACTIVITY("Sudden Police Activity"),
  ACCIDENT("Accident / Road Hazard"),
  CRIME_SURGE("Recent Crime Surge"),
  STREET_BLACKOUT("Street Lighting Blackout"),
  ACTIVE_HAZARD("Active Danger Zone")
}

// Room entity for local persistence cache
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
  val userReported: Boolean = false,
  val isAnonymous: Boolean = true,
  val verificationStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val firestoreId: String = "",
  val name: String,
  val phoneNumber: String,
  val relationship: String,
  val isPrimary: Boolean = false,
  val notifyOnSos: Boolean = true
)

// Firestore User Profile with customizable medical info and theme
@IgnoreExtraProperties
data class FirestoreUserProfile(
  @DocumentId val id: String = "primary",
  val userId: String = "",
  val displayName: String = "SafeWalk User",
  val email: String = "",
  val phoneNumber: String = "",
  val bloodType: String = "O+",
  val physicalCapacity: String = "Level 1 (Full Mobility)",
  val chronicIllnesses: String = "None declared",
  val medications: String = "None declared",
  val allergies: String = "No known allergies",
  val emergencyNotes: String = "",
  val themePreference: String = AppThemeMode.DARK.name,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

// Firestore Favorite Routes
@IgnoreExtraProperties
data class FirestoreFavoriteRoute(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val startAddress: String = "",
  val destinationName: String = "",
  val routeType: String = RouteType.SAFEST.name,
  val safetyScore: Long = 95L,
  val lightingPercentage: Long = 98L,
  @ServerTimestamp val createdAt: Timestamp? = null
)

// Firestore Community Report
@IgnoreExtraProperties
data class FirestoreCommunityReport(
  @DocumentId val id: String = "",
  val userId: String = "",
  val authorName: String = "Anonymous Pedestrian",
  val title: String = "",
  val description: String = "",
  val category: String = ReportCategory.BROKEN_LIGHTING.name,
  val severity: String = SeverityLevel.MEDIUM.name,
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val addressName: String = "",
  @get:JvmName("getIsVerified") val isVerified: Boolean = false,
  val verificationStatus: String = VerificationStatus.PENDING_REVIEW.name,
  val upvotes: Long = 1L,
  @get:JvmName("getIsAnonymous") val isAnonymous: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreRealtimeAlert(
  @DocumentId val id: String = "",
  val title: String = "",
  val message: String = "",
  val type: String = AlertType.SUDDEN_POLICE_ACTIVITY.name,
  val severity: String = SeverityLevel.HIGH.name,
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val radiusMeters: Double = 250.0,
  val affectedStreet: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreEmergencyContact(
  @DocumentId val id: String = "",
  val userId: String = "",
  val name: String = "",
  val phoneNumber: String = "",
  val relationship: String = "",
  val isPrimary: Boolean = false,
  val notifyOnSos: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreSosLog(
  @DocumentId val id: String = "",
  val userId: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val address: String = "",
  val distressMessage: String = "",
  val contactsNotified: String = "",
  val emergencyDialed: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)

data class HazardZone(
  val id: String,
  val name: String,
  val description: String,
  val dangerType: String,
  val lat: Double,
  val lng: Double,
  val radiusMeters: Float,
  val riskScore: Int,
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
