package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.AlertType
import com.example.data.model.AppThemeMode
import com.example.data.model.FirestoreCommunityReport
import com.example.data.model.FirestoreEmergencyContact
import com.example.data.model.FirestoreFavoriteRoute
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.FirestoreSosLog
import com.example.data.model.FirestoreUserProfile
import com.example.data.model.ReportCategory
import com.example.data.model.SeverityLevel
import com.example.data.model.VerificationStatus
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  LIST("list"),
  GET("get"),
  WRITE("write")
}

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
  val auth = Firebase.auth
  val currentUser = auth.currentUser

  val providerInfoList = currentUser?.providerData?.map { provider ->
    JSONObject().apply {
      put("providerId", provider.providerId)
      put("email", provider.email)
    }
  } ?: emptyList()

  val authInfoJson = JSONObject().apply {
    put("userId", currentUser?.uid)
    put("email", currentUser?.email)
    put("emailVerified", currentUser?.isEmailVerified)
    put("tenantId", currentUser?.tenantId)
    put("providerInfo", JSONArray(providerInfoList))
  }

  val errorInfoJson = JSONObject().apply {
    put("error", exception.message ?: exception.toString())
    put("operationType", operationType.value)
    put("path", path)
    put("authInfo", authInfoJson)
  }

  val jsonString = errorInfoJson.toString()
  Log.e("FirestoreError", "Firestore Error: $jsonString")
  return jsonString
}

class FirestoreSafetyRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth: FirebaseAuth get() = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // --- Real-time Observe User Profile (Personal & Medical Info) ---
  fun observeUserProfile(): Flow<FirestoreUserProfile?> {
    val uid = auth.currentUser?.uid ?: return flow { emit(null) }
    val path = "users/$uid/profile_data/primary"
    return db.collection("users").document(uid).collection("profile_data").document("primary")
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(FirestoreUserProfile::class.java)
        } else {
          // Default initial profile
          FirestoreUserProfile(
            userId = uid,
            displayName = auth.currentUser?.displayName ?: "SafeWalk Pedestrian",
            email = auth.currentUser?.email ?: "",
            themePreference = AppThemeMode.DARK.name
          )
        }
      }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
        emit(null)
      }
  }

  // --- Save / Update User Profile ---
  suspend fun saveUserProfile(profile: FirestoreUserProfile): Result<Unit> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("profile_data").document("primary")
      val data = mapOf(
        "id" to "primary",
        "userId" to uid,
        "displayName" to profile.displayName,
        "email" to (auth.currentUser?.email ?: profile.email),
        "phoneNumber" to profile.phoneNumber,
        "bloodType" to profile.bloodType,
        "physicalCapacity" to profile.physicalCapacity,
        "chronicIllnesses" to profile.chronicIllnesses,
        "medications" to profile.medications,
        "allergies" to profile.allergies,
        "emergencyNotes" to profile.emergencyNotes,
        "themePreference" to profile.themePreference,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(data).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}/profile_data/primary")
      Result.failure(e)
    }
  }

  // --- Real-time Observe Favorite Routes ---
  fun observeFavoriteRoutes(): Flow<List<FirestoreFavoriteRoute>> {
    val uid = auth.currentUser?.uid ?: return flow { emit(emptyList()) }
    val path = "users/$uid/favorite_routes"
    return db.collection("users").document(uid).collection("favorite_routes")
      .snapshots()
      .map { snapshot -> snapshot.toObjects(FirestoreFavoriteRoute::class.java) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        emit(emptyList())
      }
  }

  // --- Add Favorite Route ---
  suspend fun addFavoriteRoute(
    title: String,
    startAddress: String,
    destinationName: String,
    routeType: String,
    safetyScore: Long,
    lightingPercentage: Long
  ): Result<String> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("favorite_routes").document()
      val data = mapOf(
        "id" to docRef.id,
        "userId" to uid,
        "title" to title,
        "startAddress" to startAddress,
        "destinationName" to destinationName,
        "routeType" to routeType,
        "safetyScore" to safetyScore,
        "lightingPercentage" to lightingPercentage,
        "createdAt" to FieldValue.serverTimestamp()
      )
      docRef.set(data).await()
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/favorite_routes")
      Result.failure(e)
    }
  }

  // --- Delete Favorite Route ---
  suspend fun deleteFavoriteRoute(routeId: String): Result<Unit> {
    return try {
      val uid = requireUserId()
      db.collection("users").document(uid).collection("favorite_routes").document(routeId).delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/favorite_routes/$routeId")
      Result.failure(e)
    }
  }

  // --- Real-time Observe Community Reports ---
  fun observeCommunityReports(): Flow<List<FirestoreCommunityReport>> {
    if (auth.currentUser == null) return flow { emit(emptyList()) }
    val path = "community_reports"
    return db.collection(path)
      .snapshots()
      .map { snapshot -> snapshot.toObjects(FirestoreCommunityReport::class.java) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        emit(emptyList())
      }
  }

  // --- Real-time Observe Realtime Safety Alerts ---
  fun observeRealtimeAlerts(): Flow<List<FirestoreRealtimeAlert>> {
    if (auth.currentUser == null) return flow { emit(emptyList()) }
    val path = "realtime_alerts"
    return db.collection(path)
      .snapshots()
      .map { snapshot -> snapshot.toObjects(FirestoreRealtimeAlert::class.java) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        emit(emptyList())
      }
  }

  // --- Real-time Observe Emergency Contacts ---
  fun observeEmergencyContacts(): Flow<List<FirestoreEmergencyContact>> {
    val uid = auth.currentUser?.uid ?: return flow { emit(emptyList()) }
    val path = "users/$uid/emergency_contacts"
    return db.collection("users").document(uid).collection("emergency_contacts")
      .snapshots()
      .map { snapshot -> snapshot.toObjects(FirestoreEmergencyContact::class.java) }
      .catch { error ->
        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
        emit(emptyList())
      }
  }

  // --- Add Community Report (Anonymous or Attributed) ---
  suspend fun submitReport(
    title: String,
    description: String,
    category: ReportCategory,
    severity: SeverityLevel,
    latitude: Double,
    longitude: Double,
    addressName: String,
    isAnonymous: Boolean
  ): Result<String> {
    return try {
      val uid = requireUserId()
      val author = if (isAnonymous) "Anonymous Pedestrian" else (auth.currentUser?.displayName ?: "Verified Pedestrian")
      val docRef = db.collection("community_reports").document()
      val data = mapOf(
        "id" to docRef.id,
        "userId" to uid,
        "authorName" to author,
        "title" to title,
        "description" to description,
        "category" to category.name,
        "severity" to severity.name,
        "latitude" to latitude,
        "longitude" to longitude,
        "addressName" to addressName,
        "isVerified" to false,
        "verificationStatus" to VerificationStatus.PENDING_REVIEW.name,
        "upvotes" to 1L,
        "isAnonymous" to isAnonymous,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(data).await()
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, "community_reports")
      Result.failure(e)
    }
  }

  // --- Upvote / Confirm Report (Accuracy Review Pipeline) ---
  suspend fun upvoteReport(report: FirestoreCommunityReport): Result<Unit> {
    return try {
      val docRef = db.collection("community_reports").document(report.id)
      val newUpvotes = report.upvotes + 1
      val newStatus = if (newUpvotes >= 3L) VerificationStatus.COMMUNITY_CONFIRMED.name else report.verificationStatus
      val isNowVerified = report.isVerified || (newUpvotes >= 3L)

      docRef.update(
        mapOf(
          "upvotes" to FieldValue.increment(1),
          "verificationStatus" to newStatus,
          "isVerified" to isNowVerified,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, "community_reports/${report.id}")
      Result.failure(e)
    }
  }

  // --- Seed Initial Cloud Alerts & Reports If Empty ---
  suspend fun seedInitialCloudDataIfEmpty() {
    try {
      if (auth.currentUser == null) return
      val snapshot = db.collection("realtime_alerts").limit(1).get().await()
      if (snapshot.isEmpty) {
        val alerts = listOf(
          mapOf(
            "title" to "Sudden Police Activity on 5th Ave",
            "message" to "High-intensity police response and cordoned sidewalk near 5th Ave & Market. Pedestrians advised to reroute.",
            "type" to AlertType.SUDDEN_POLICE_ACTIVITY.name,
            "severity" to SeverityLevel.CRITICAL.name,
            "latitude" to 37.7745,
            "longitude" to -122.4190,
            "radiusMeters" to 220.0,
            "affectedStreet" to "5th Ave & Market Corridor",
            "createdAt" to FieldValue.serverTimestamp()
          ),
          mapOf(
            "title" to "Traffic Accident with Glass on Crosswalk",
            "message" to "Two-car collision blocking crosswalk at Elm St. Emergency vehicles on scene.",
            "type" to AlertType.ACCIDENT.name,
            "severity" to SeverityLevel.MEDIUM.name,
            "latitude" to 37.7762,
            "longitude" to -122.4175,
            "radiusMeters" to 150.0,
            "affectedStreet" to "Elm St",
            "createdAt" to FieldValue.serverTimestamp()
          ),
          mapOf(
            "title" to "Complete Streetlight Blackout",
            "message" to "Power failure reported across South Park bypass. Dense shadows, zero sidewalk lighting.",
            "type" to AlertType.STREET_BLACKOUT.name,
            "severity" to SeverityLevel.HIGH.name,
            "latitude" to 37.7725,
            "longitude" to -122.4215,
            "radiusMeters" to 260.0,
            "affectedStreet" to "South Park Perimeter",
            "createdAt" to FieldValue.serverTimestamp()
          )
        )
        for (alert in alerts) {
          db.collection("realtime_alerts").add(alert).await()
        }
      }

      // Check community reports
      val reportsSnapshot = db.collection("community_reports").limit(1).get().await()
      if (reportsSnapshot.isEmpty) {
        val uid = auth.currentUser?.uid ?: "system_pedestrian"
        val seedReports = listOf(
          mapOf(
            "userId" to uid,
            "authorName" to "Anonymous Pedestrian",
            "title" to "3 Broken Lampposts Near Warehouse",
            "description" to "North sidewalk is completely dark. High risk of tripping or unseen pedestrians.",
            "category" to ReportCategory.BROKEN_LIGHTING.name,
            "severity" to SeverityLevel.HIGH.name,
            "latitude" to 37.7760,
            "longitude" to -122.4180,
            "addressName" to "Elm St between 4th & 6th",
            "isVerified" to true,
            "verificationStatus" to VerificationStatus.VERIFIED.name,
            "upvotes" to 14L,
            "isAnonymous" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          ),
          mapOf(
            "userId" to uid,
            "authorName" to "Local Night Commuter",
            "title" to "Open 24/7 CVS Safe Haven & Guard",
            "description" to "Well-lit exterior floodlights, security staff inside, open all night.",
            "category" to ReportCategory.SAFE_HAVEN_24_7.name,
            "severity" to SeverityLevel.LOW.name,
            "latitude" to 37.7785,
            "longitude" to -122.4140,
            "addressName" to "450 Grand Ave",
            "isVerified" to true,
            "verificationStatus" to VerificationStatus.COMMUNITY_CONFIRMED.name,
            "upvotes" to 28L,
            "isAnonymous" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        )
        for (rep in seedReports) {
          db.collection("community_reports").add(rep).await()
        }
      }

      // Seed default favorite route if empty
      val favSnapshot = db.collection("users").document(auth.currentUser!!.uid).collection("favorite_routes").limit(1).get().await()
      if (favSnapshot.isEmpty) {
        val defaultFav = mapOf(
          "userId" to auth.currentUser!!.uid,
          "title" to "Campus Dorms via Beacon Blvd",
          "startAddress" to "Metro Plaza Entrance",
          "destinationName" to "University North Hall Dorms",
          "routeType" to "SAFEST",
          "safetyScore" to 96L,
          "lightingPercentage" to 98L,
          "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(auth.currentUser!!.uid).collection("favorite_routes").add(defaultFav).await()
      }
    } catch (e: Exception) {
      Log.w("FirestoreRepo", "Cloud seed skipped: ${e.message}")
    }
  }

  // --- Add Emergency Contact ---
  suspend fun addEmergencyContact(
    name: String,
    phone: String,
    relationship: String,
    isPrimary: Boolean
  ): Result<String> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("emergency_contacts").document()
      val data = mapOf(
        "id" to docRef.id,
        "userId" to uid,
        "name" to name,
        "phoneNumber" to phone,
        "relationship" to relationship,
        "isPrimary" to isPrimary,
        "notifyOnSos" to true,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
      docRef.set(data).await()
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/emergency_contacts")
      Result.failure(e)
    }
  }

  // --- Delete Emergency Contact ---
  suspend fun deleteEmergencyContact(contactId: String): Result<Unit> {
    return try {
      val uid = requireUserId()
      db.collection("users").document(uid).collection("emergency_contacts").document(contactId).delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "users/${auth.currentUser?.uid}/emergency_contacts/$contactId")
      Result.failure(e)
    }
  }

  // --- Log SOS Activation Event ---
  suspend fun logSosActivation(
    lat: Double,
    lng: Double,
    address: String,
    distressMessage: String,
    contactsNotified: String,
    dialedNumber: String
  ): Result<String> {
    return try {
      val uid = requireUserId()
      val docRef = db.collection("users").document(uid).collection("sos_history").document()
      val data = mapOf(
        "id" to docRef.id,
        "userId" to uid,
        "latitude" to lat,
        "longitude" to lng,
        "address" to address,
        "distressMessage" to distressMessage,
        "contactsNotified" to contactsNotified,
        "emergencyDialed" to dialedNumber,
        "createdAt" to FieldValue.serverTimestamp()
      )
      docRef.set(data).await()
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, "users/${auth.currentUser?.uid}/sos_history")
      Result.failure(e)
    }
  }
}
