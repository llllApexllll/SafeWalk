package com.example

import com.example.data.model.AlertType
import com.example.data.model.AppThemeMode
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.FirestoreUserProfile
import com.example.data.model.ReportCategory
import com.example.data.model.RouteOption
import com.example.data.model.RoutePoint
import com.example.data.model.RouteType
import com.example.data.model.SeverityLevel
import com.example.data.model.VerificationStatus
import com.example.service.RealtimeAlertMonitor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SafetyWalkUnitTest {

  @Test
  fun `realtime alert monitor prioritizes critical and proximate threats`() {
    val context = RuntimeEnvironment.getApplication()
    val monitor = RealtimeAlertMonitor(context)

    val criticalNearAlert = FirestoreRealtimeAlert(
      id = "alert_1",
      title = "Sudden Police Activity on 5th Ave",
      message = "Police cordoned sidewalk",
      type = AlertType.SUDDEN_POLICE_ACTIVITY.name,
      severity = SeverityLevel.CRITICAL.name,
      latitude = 37.7745,
      longitude = -122.4190,
      radiusMeters = 200.0,
      affectedStreet = "5th Ave"
    )

    val mediumFarAlert = FirestoreRealtimeAlert(
      id = "alert_2",
      title = "Pothole on 10th St",
      message = "Minor hazard",
      type = AlertType.ACTIVE_HAZARD.name,
      severity = SeverityLevel.MEDIUM.name,
      latitude = 37.7900,
      longitude = -122.4000,
      radiusMeters = 50.0,
      affectedStreet = "10th St"
    )

    val sampleRoute = RouteOption(
      id = "route_1",
      type = RouteType.SAFEST,
      title = "Safest",
      distanceKm = 1.2f,
      estimatedMinutes = 15,
      safetyScore = 95,
      lightingPercentage = 98,
      safeHavensCount = 2,
      hazardsAvoidedCount = 2,
      unlitStreetsCount = 0,
      summaryNote = "Safe route",
      pathPoints = listOf(
        RoutePoint(37.7710, -122.4150, "Start", 100),
        RoutePoint(37.7745, -122.4190, "5th Ave", 95)
      ),
      navigationSteps = emptyList()
    )

    val prioritized = monitor.evaluateAlerts(
      alerts = listOf(mediumFarAlert, criticalNearAlert),
      userLat = 37.7740,
      userLng = -122.4185,
      activeRoute = sampleRoute
    )

    assertEquals(2, prioritized.size)
    // Critical alert near user & on route must be ranked #1
    assertEquals("alert_1", prioritized.first().alert.id)
    assertTrue(prioritized.first().isOnRoute)
    assertTrue(prioritized.first().priorityRank > prioritized.last().priorityRank)
  }

  @Test
  fun `user profile holds medical Phcap and chronic conditions`() {
    val profile = FirestoreUserProfile(
      userId = "user_test",
      displayName = "Jane Doe",
      email = "jane@example.com",
      phoneNumber = "+15551234567",
      bloodType = "O+",
      physicalCapacity = "Level 1 (Full Mobility)",
      chronicIllnesses = "Asthma",
      medications = "Albuterol Inhaler",
      allergies = "Penicillin",
      emergencyNotes = "Inhaler in backpack side pouch",
      themePreference = AppThemeMode.AUTO.name
    )

    assertEquals("O+", profile.bloodType)
    assertEquals("Asthma", profile.chronicIllnesses)
    assertEquals("Albuterol Inhaler", profile.medications)
    assertEquals(AppThemeMode.AUTO.name, profile.themePreference)
  }
}
