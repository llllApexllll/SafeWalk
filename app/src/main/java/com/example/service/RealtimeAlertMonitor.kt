package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.RouteOption
import com.example.data.model.RoutePoint
import com.example.data.model.SeverityLevel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class PrioritizedAlert(
  val alert: FirestoreRealtimeAlert,
  val distanceToUserMeters: Double,
  val minDistanceToRouteMeters: Double,
  val isOnRoute: Boolean,
  val priorityRank: Int, // Higher number = higher priority
  val proximityLabel: String,
  val recommendedAction: String
)

class RealtimeAlertMonitor(private val context: Context) {

  private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
  private val CHANNEL_ID = "safewalk_realtime_alerts"

  init {
    createNotificationChannel()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "SafeWalk Safety Concerns",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Push notifications for immediate safety concerns along your route"
        enableVibration(true)
      }
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun evaluateAlerts(
    alerts: List<FirestoreRealtimeAlert>,
    userLat: Double,
    userLng: Double,
    activeRoute: RouteOption?
  ): List<PrioritizedAlert> {
    val routePoints = activeRoute?.pathPoints ?: emptyList()

    val prioritized = alerts.map { alert ->
      val distToUser = calculateDistanceMeters(userLat, userLng, alert.latitude, alert.longitude)
      val distToRoute = calculateDistanceToRoute(alert.latitude, alert.longitude, routePoints)
      val isOnRoute = distToRoute <= alert.radiusMeters

      // Priority Calculation:
      // Severity: CRITICAL = 1000, HIGH = 600, MEDIUM = 300
      val severityWeight = when (alert.severity) {
        SeverityLevel.CRITICAL.name -> 1000
        SeverityLevel.HIGH.name -> 600
        else -> 300
      }

      // Proximity Weight (Closer = higher)
      val proximityWeight = (1000.0 - distToUser.coerceAtMost(1000.0)).toInt()
      val routeWeight = if (isOnRoute) 800 else 0
      val totalRank = severityWeight + proximityWeight + routeWeight

      val proximityLabel = when {
        distToUser < 100 -> "Immediate vicinity (${distToUser.toInt()}m)"
        distToUser < 300 -> "Close proximity (${distToUser.toInt()}m)"
        distToUser < 600 -> "Nearby (${distToUser.toInt()}m)"
        else -> "${(distToUser / 1000.0).format(1)}km away"
      }

      val recommendedAction = when {
        isOnRoute && alert.severity == SeverityLevel.CRITICAL.name ->
          "CRITICAL: Directly on your planned path! We recommend immediate detour to illuminated Beacon Blvd."
        isOnRoute ->
          "Caution: Route passes near this incident on ${alert.affectedStreet}. Stay vigilant."
        distToUser < 200 ->
          "Avoid heading toward ${alert.affectedStreet}. Stick to verified safe corridors."
        else ->
          "Advisory only: Activity reported nearby on ${alert.affectedStreet}."
      }

      PrioritizedAlert(
        alert = alert,
        distanceToUserMeters = distToUser,
        minDistanceToRouteMeters = distToRoute,
        isOnRoute = isOnRoute,
        priorityRank = totalRank,
        proximityLabel = proximityLabel,
        recommendedAction = recommendedAction
      )
    }

    return prioritized.sortedByDescending { it.priorityRank }
  }

  fun pushSafetyConcernNotification(prioritizedAlert: PrioritizedAlert) {
    try {
      val alert = prioritizedAlert.alert
      val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("⚠️ ${alert.title}")
        .setContentText("${prioritizedAlert.proximityLabel}: ${alert.message}")
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText("${alert.message}\n\n👉 Recommendation: ${prioritizedAlert.recommendedAction}")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

      notificationManager?.notify(alert.id.hashCode(), builder.build())
    } catch (e: Exception) {
      // Notification permission may not yet be granted
    }
  }

  companion object {
    // Haversine distance in meters
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
      val r = 6371000.0 // Earth radius in meters
      val dLat = Math.toRadians(lat2 - lat1)
      val dLon = Math.toRadians(lon2 - lon1)
      val a = sin(dLat / 2) * sin(dLat / 2) +
          cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
          sin(dLon / 2) * sin(dLon / 2)
      val c = 2 * atan2(sqrt(a), sqrt(1 - a))
      return r * c
    }

    fun calculateDistanceToRoute(lat: Double, lng: Double, routePoints: List<RoutePoint>): Double {
      if (routePoints.isEmpty()) return Double.MAX_VALUE
      var minDistance = Double.MAX_VALUE
      for (pt in routePoints) {
        val d = calculateDistanceMeters(lat, lng, pt.lat, pt.lng)
        if (d < minDistance) {
          minDistance = d
        }
      }
      return minDistance
    }
  }

  private fun Double.format(digits: Int) = String.format("%.${digits}f", this)
}
