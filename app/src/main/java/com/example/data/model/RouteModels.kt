package com.example.data.model

enum class RouteType(val label: String, val badge: String) {
  SAFEST("Safest Route", "Recommended"),
  WELL_LIT("Well-Lit Corridor", "High Illumination"),
  DIRECT("Direct Route", "Hazardous Warning")
}

data class RoutePoint(
  val lat: Double,
  val lng: Double,
  val streetName: String,
  val lightingPercentage: Int, // 0 - 100
  val isHazardous: Boolean = false,
  val hazardReason: String = ""
)

data class NavigationStep(
  val instruction: String,
  val streetName: String,
  val distanceMeters: Int,
  val lightingLevel: String,
  val nearbySafeHaven: String? = null,
  val warningNote: String? = null
)

data class RouteOption(
  val id: String,
  val type: RouteType,
  val title: String,
  val distanceKm: Float,
  val estimatedMinutes: Int,
  val safetyScore: Int,           // 0 to 100
  val lightingPercentage: Int,   // e.g. 98%
  val safeHavensCount: Int,
  val hazardsAvoidedCount: Int,
  val unlitStreetsCount: Int,
  val summaryNote: String,
  val pathPoints: List<RoutePoint>,
  val navigationSteps: List<NavigationStep>
)

data class CityDestination(
  val id: String,
  val name: String,
  val category: String,
  val address: String,
  val lat: Double,
  val lng: Double
)
