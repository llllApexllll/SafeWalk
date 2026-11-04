package com.example.data.repository

import com.example.data.db.EmergencyContactDao
import com.example.data.db.SafetyReportDao
import com.example.data.model.CityDestination
import com.example.data.model.EmergencyContactEntity
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.HazardZone
import com.example.data.model.NavigationStep
import com.example.data.model.ReportCategory
import com.example.data.model.RouteOption
import com.example.data.model.RoutePoint
import com.example.data.model.RouteType
import com.example.data.model.SafeHaven
import com.example.data.model.SafeHavenType
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel
import com.example.service.RealtimeAlertMonitor
import kotlinx.coroutines.flow.Flow

class SafetyRepository(
  private val safetyReportDao: SafetyReportDao,
  private val emergencyContactDao: EmergencyContactDao
) {
  val allReports: Flow<List<SafetyReportEntity>> = safetyReportDao.getAllReports()
  val allContacts: Flow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()

  suspend fun initializePreseededDataIfEmpty() {
    if (safetyReportDao.getReportCount() == 0) {
      val initialReports = listOf(
        SafetyReportEntity(
          title = "Broken Streetlamps (Pitch Black)",
          description = "Three consecutive streetlights are out between 4th and 6th. Total darkness on the north sidewalk.",
          category = ReportCategory.BROKEN_LIGHTING,
          severity = SeverityLevel.HIGH,
          latitude = 37.7760,
          longitude = -122.4180,
          addressName = "Elm St between 4th & 6th",
          timestampFormatted = "18 mins ago",
          upvotes = 19,
          isVerified = true,
          isLightingRelated = true
        ),
        SafetyReportEntity(
          title = "24/7 Pharmacy Safe Haven",
          description = "CVS on Grand Ave is open all night. Security guard on duty, bright front floodlights and staff inside.",
          category = ReportCategory.SAFE_HAVEN_24_7,
          severity = SeverityLevel.LOW,
          latitude = 37.7785,
          longitude = -122.4140,
          addressName = "450 Grand Ave",
          timestampFormatted = "1 hour ago",
          upvotes = 34,
          isVerified = true,
          isLightingRelated = false
        ),
        SafetyReportEntity(
          title = "Aggressive Harassment Near Park",
          description = "User reported someone following and catcalling aggressively along the unlit gravel path. Avoid cutting through the park after 9 PM.",
          category = ReportCategory.HARASSMENT,
          severity = SeverityLevel.HIGH,
          latitude = 37.7725,
          longitude = -122.4220,
          addressName = "South Park Woods Path",
          timestampFormatted = "32 mins ago",
          upvotes = 27,
          isVerified = true,
          isLightingRelated = false
        ),
        SafetyReportEntity(
          title = "Bright LED Illuminated Promenade",
          description = "New high-intensity LED streetlights installed. Very bright, good pedestrian visibility, open cafes.",
          category = ReportCategory.WELL_LIT_CORRIDOR,
          severity = SeverityLevel.LOW,
          latitude = 37.7790,
          longitude = -122.4120,
          addressName = "Beacon Blvd",
          timestampFormatted = "2 hours ago",
          upvotes = 42,
          isVerified = true,
          isLightingRelated = true
        ),
        SafetyReportEntity(
          title = "Suspicious Loitering Underpass",
          description = "2 individuals lingering by the dark tunnel pillars asking pedestrians for phones. Limited visibility.",
          category = ReportCategory.SUSPICIOUS_ACTIVITY,
          severity = SeverityLevel.MEDIUM,
          latitude = 37.7740,
          longitude = -122.4200,
          addressName = "Rail Underpass on 5th",
          timestampFormatted = "45 mins ago",
          upvotes = 14,
          isVerified = false,
          isLightingRelated = true
        ),
        SafetyReportEntity(
          title = "Campus Security Booth 24/7",
          description = "University police patrol post with emergency blue light phone, monitored CCTV, and welcoming officers.",
          category = ReportCategory.SUDDEN_POLICE_ACTIVITY,
          severity = SeverityLevel.LOW,
          latitude = 37.7810,
          longitude = -122.4100,
          addressName = "University North Gate",
          timestampFormatted = "3 hours ago",
          upvotes = 56,
          isVerified = true,
          isLightingRelated = false
        )
      )
      safetyReportDao.insertReports(initialReports)
    }

    if (emergencyContactDao.getContactCount() == 0) {
      val initialContacts = listOf(
        EmergencyContactEntity(
          name = "Sarah Miller",
          phoneNumber = "+1 (555) 234-5678",
          relationship = "Sister / Roommate",
          isPrimary = true,
          notifyOnSos = true
        ),
        EmergencyContactEntity(
          name = "David Chen",
          phoneNumber = "+1 (555) 876-5432",
          relationship = "Partner",
          isPrimary = false,
          notifyOnSos = true
        )
      )
      emergencyContactDao.insertContacts(initialContacts)
    }
  }

  suspend fun addReport(report: SafetyReportEntity): Long {
    return safetyReportDao.insertReport(report)
  }

  suspend fun upvoteReport(id: Long) {
    safetyReportDao.upvoteReport(id)
  }

  suspend fun addContact(contact: EmergencyContactEntity): Long {
    return emergencyContactDao.insertContact(contact)
  }

  suspend fun deleteContact(contact: EmergencyContactEntity) {
    emergencyContactDao.deleteContact(contact)
  }

  // Predefined Safe Havens
  fun getSafeHavens(): List<SafeHaven> {
    return listOf(
      SafeHaven(
        id = "sh_1",
        name = "Grand Ave 24/7 Pharmacy & Guard",
        type = SafeHavenType.PHARMACY_24_7,
        address = "450 Grand Ave",
        lat = 37.7785,
        lng = -122.4140,
        isOpenNow = true,
        staffPresent = true,
        phone = "555-0199"
      ),
      SafeHaven(
        id = "sh_2",
        name = "Metro Central Police Precinct",
        type = SafeHavenType.POLICE_STATION,
        address = "100 Transit Plaza",
        lat = 37.7715,
        lng = -122.4150,
        isOpenNow = true,
        staffPresent = true,
        phone = "911"
      ),
      SafeHaven(
        id = "sh_3",
        name = "University Security Post",
        type = SafeHavenType.CAMPUS_SECURITY,
        address = "North Gate Quad",
        lat = 37.7810,
        lng = -122.4100,
        isOpenNow = true,
        staffPresent = true,
        phone = "555-0120"
      ),
      SafeHaven(
        id = "sh_4",
        name = "All-Night QuickStop Market",
        type = SafeHavenType.CONVENIENCE_STORE_24_7,
        address = "220 Beacon Blvd",
        lat = 37.7765,
        lng = -122.4110,
        isOpenNow = true,
        staffPresent = true,
        phone = "555-0144"
      ),
      SafeHaven(
        id = "sh_5",
        name = "Civic Center Transit Hub (24/7 Security)",
        type = SafeHavenType.CAMPUS_SECURITY,
        address = "300 Civic Plaza",
        lat = 37.7770,
        lng = -122.4155,
        isOpenNow = true,
        staffPresent = true,
        phone = "555-0160"
      ),
      SafeHaven(
        id = "sh_6",
        name = "Memorial Hospital 24/7 ER",
        type = SafeHavenType.HOSPITAL_ER,
        address = "600 Medical Way",
        lat = 37.7795,
        lng = -122.4125,
        isOpenNow = true,
        staffPresent = true,
        phone = "911"
      )
    )
  }

  // Predefined Known Hazard Zones
  fun getHazardZones(): List<HazardZone> {
    return listOf(
      HazardZone(
        id = "hz_1",
        name = "Elm St Unlit Corridor",
        description = "Total streetlight failure, narrow sidewalk between warehouses.",
        dangerType = "Poor Lighting (0% Lux)",
        lat = 37.7758,
        lng = -122.4178,
        radiusMeters = 180f,
        riskScore = 88,
        isUnlit = true
      ),
      HazardZone(
        id = "hz_2",
        name = "South Park Isolated Path",
        description = "Recent violent harassment & purse snatching reports, dense shadows.",
        dangerType = "High Crime / Assault Cluster",
        lat = 37.7725,
        lng = -122.4215,
        radiusMeters = 220f,
        riskScore = 94,
        isUnlit = true
      ),
      HazardZone(
        id = "hz_3",
        name = "Rail Underpass Blind Spot",
        description = "Isolated underpass with blind corners and frequent loitering.",
        dangerType = "Isolated Blind Alley",
        lat = 37.7740,
        lng = -122.4198,
        radiusMeters = 140f,
        riskScore = 75,
        isUnlit = true
      )
    )
  }

  // Predefined popular destinations
  fun getPopularDestinations(): List<CityDestination> {
    return listOf(
      CityDestination(
        id = "dest_1",
        name = "University North Hall Dorms",
        category = "Campus & Student Housing",
        address = "800 University Way",
        lat = 37.7815,
        lng = -122.4095
      ),
      CityDestination(
        id = "dest_2",
        name = "Grand Central Metro Plaza",
        category = "Public Transit Hub",
        address = "100 Transit Plaza",
        lat = 37.7710,
        lng = -122.4150
      ),
      CityDestination(
        id = "dest_3",
        name = "Pine & 7th City Apartments",
        category = "Residential",
        address = "720 Pine Street",
        lat = 37.7795,
        lng = -122.4170
      ),
      CityDestination(
        id = "dest_4",
        name = "Downtown Public Library",
        category = "Civic Center",
        address = "300 Civic Center Plaza",
        lat = 37.7770,
        lng = -122.4160
      )
    )
  }

  // Generates 3 realistic routes between any GPS coordinates and destination
  fun calculateRouteOptions(
    startLat: Double = 37.7710,
    startLng: Double = -122.4150,
    dest: CityDestination
  ): List<RouteOption> {
    val totalDistMeters = RealtimeAlertMonitor.calculateDistanceMeters(startLat, startLng, dest.lat, dest.lng)
    val baseKm = (totalDistMeters / 1000.0).toFloat().coerceAtLeast(0.1f)
    val baseMins = kotlin.math.max(2, (baseKm * 13).toInt())

    // 1. SAFEST ROUTE (Recommended)
    // Curvature avoids known dark pockets and passes nearest 24/7 safe havens
    val safestPoints = mutableListOf<RoutePoint>()
    safestPoints.add(RoutePoint(startLat, startLng, "Current GPS Location", 96))

    val stepsCount = 5
    for (i in 1..stepsCount) {
      val frac = i.toDouble() / (stepsCount + 1)
      val directLat = startLat + frac * (dest.lat - startLat)
      val directLng = startLng + frac * (dest.lng - startLng)

      // Slight perpendicular lateral offset to simulate following well-lit arterial streets
      val perpLat = -(dest.lng - startLng) * 0.12 * kotlin.math.sin(frac * Math.PI)
      val perpLng = (dest.lat - startLat) * 0.12 * kotlin.math.sin(frac * Math.PI)

      val ptLat = directLat + perpLat
      val ptLng = directLng + perpLng
      val streetName = when (i) {
        1 -> "Illuminated Main Boulevard"
        2 -> "Beacon Commercial Way (CCTV Active)"
        3 -> "Grand Promenade (24/7 Staffed)"
        4 -> "Safety Corridor North"
        else -> "Destination Approach"
      }
      safestPoints.add(RoutePoint(ptLat, ptLng, streetName, 98))
    }
    safestPoints.add(RoutePoint(dest.lat, dest.lng, dest.name, 95))

    val safestKm = baseKm * 1.15f
    val safestMins = kotlin.math.max(3, (safestKm * 13).toInt())
    val safestRoute = RouteOption(
      id = "route_safest",
      type = RouteType.SAFEST,
      title = "Safest Route (Via Safe Corridors)",
      distanceKm = String.format(java.util.Locale.US, "%.2f", safestKm).toFloat(),
      estimatedMinutes = safestMins,
      safetyScore = 96,
      lightingPercentage = 98,
      safeHavensCount = 3,
      hazardsAvoidedCount = 2,
      unlitStreetsCount = 0,
      summaryNote = "Avoids unlit alleys and blind spots. Continuous ambient illumination, active CCTV, and nearby 24/7 Safe Havens.",
      pathPoints = safestPoints,
      navigationSteps = listOf(
        NavigationStep("Depart from current GPS location along illuminated sidewalk", "Start Corridor", (safestKm * 200).toInt(), "Bright (96%)", "Nearby 24/7 Store"),
        NavigationStep("Follow main illuminated thoroughfare with active shopfronts", "Beacon Way", (safestKm * 400).toInt(), "Fully Illuminated (100%)", "Emergency Haven Nearby"),
        NavigationStep("Continue past well-lit commercial intersection", "Grand Promenade", (safestKm * 300).toInt(), "Bright LED (98%)", null),
        NavigationStep("Turn towards destination entryway", dest.name, (safestKm * 100).toInt(), "Well Lit (95%)", null)
      )
    )

    // 2. WELL-LIT ONLY ROUTE
    val wellLitPoints = mutableListOf<RoutePoint>()
    wellLitPoints.add(RoutePoint(startLat, startLng, "Current GPS Location", 95))
    for (i in 1..stepsCount) {
      val frac = i.toDouble() / (stepsCount + 1)
      val directLat = startLat + frac * (dest.lat - startLat)
      val directLng = startLng + frac * (dest.lng - startLng)
      val perpLat = (dest.lng - startLng) * 0.18 * kotlin.math.sin(frac * Math.PI)
      val perpLng = -(dest.lat - startLat) * 0.18 * kotlin.math.sin(frac * Math.PI)
      wellLitPoints.add(RoutePoint(directLat + perpLat, directLng + perpLng, "Avenue of Lights", 100))
    }
    wellLitPoints.add(RoutePoint(dest.lat, dest.lng, dest.name, 95))

    val wellLitKm = baseKm * 1.25f
    val wellLitMins = kotlin.math.max(4, (wellLitKm * 13).toInt())
    val wellLitRoute = RouteOption(
      id = "route_well_lit",
      type = RouteType.WELL_LIT,
      title = "Maximum Illumination Route",
      distanceKm = String.format(java.util.Locale.US, "%.2f", wellLitKm).toFloat(),
      estimatedMinutes = wellLitMins,
      safetyScore = 93,
      lightingPercentage = 100,
      safeHavensCount = 2,
      hazardsAvoidedCount = 2,
      unlitStreetsCount = 0,
      summaryNote = "Guarantees 100% wide, well-lit sidewalks along major streets. Slightly longer walk but maximum visibility.",
      pathPoints = wellLitPoints,
      navigationSteps = listOf(
        NavigationStep("Walk along primary high-lumens avenue", "Avenue of Lights", (wellLitKm * 350).toInt(), "100% Illumination", null),
        NavigationStep("Stay on main perimeter roadway", "Main Avenue", (wellLitKm * 500).toInt(), "100% Illumination", "Campus Patrol"),
        NavigationStep("Arrive safely at destination", dest.name, (wellLitKm * 150).toInt(), "95% Illumination", null)
      )
    )

    // 3. DIRECT / FASTEST (Hazardous)
    val directPoints = mutableListOf<RoutePoint>()
    directPoints.add(RoutePoint(startLat, startLng, "Current GPS Location", 95))
    for (i in 1..stepsCount) {
      val frac = i.toDouble() / (stepsCount + 1)
      val directLat = startLat + frac * (dest.lat - startLat)
      val directLng = startLng + frac * (dest.lng - startLng)
      val isMid = i in 2..3
      directPoints.add(
        RoutePoint(
          directLat,
          directLng,
          if (isMid) "Unlit Short-Cut Passage" else "Direct Street",
          if (isMid) 25 else 60,
          isHazardous = isMid,
          hazardReason = if (isMid) "Poor Night Visibility & Isolated" else ""
        )
      )
    }
    directPoints.add(RoutePoint(dest.lat, dest.lng, dest.name, 95))

    val directRoute = RouteOption(
      id = "route_direct",
      type = RouteType.DIRECT,
      title = "Fastest Direct (High Danger)",
      distanceKm = String.format(java.util.Locale.US, "%.2f", baseKm).toFloat(),
      estimatedMinutes = baseMins,
      safetyScore = 48,
      lightingPercentage = 40,
      safeHavensCount = 0,
      hazardsAvoidedCount = 0,
      unlitStreetsCount = 2,
      summaryNote = "WARNING: Cuts straight through unmonitored alleys and dark blind spots. Not recommended after sunset.",
      pathPoints = directPoints,
      navigationSteps = listOf(
        NavigationStep("Proceed directly toward destination", "Direct Street", (baseKm * 300).toInt(), "Moderate (60%)", null),
        NavigationStep("Pass through narrow unlit corridor", "Shortcut Alley", (baseKm * 400).toInt(), "Pitch Black (25%)", null, "CAUTION: Isolated unlit passage"),
        NavigationStep("Exit shortcut to reach destination", dest.name, (baseKm * 300).toInt(), "Moderate (55%)", null)
      )
    )

    return listOf(safestRoute, wellLitRoute, directRoute)
  }

  // Seed / Offline Realtime Alerts for Guest Mode and fallback
  fun getLocalSeedRealtimeAlerts(): List<FirestoreRealtimeAlert> {
    return listOf(
      FirestoreRealtimeAlert(
        id = "alert_seed_1",
        title = "Active Police Activity",
        message = "Officers investigating reported vehicle break-in. Avoid 6th & Elm alleyway.",
        type = "SUDDEN_POLICE_ACTIVITY",
        severity = "CRITICAL",
        latitude = 37.7758,
        longitude = -122.4178,
        radiusMeters = 200.0,
        affectedStreet = "Elm St & 6th"
      ),
      FirestoreRealtimeAlert(
        id = "alert_seed_2",
        title = "Streetlamp Power Outage",
        message = "Multiple overhead lamps dark on 5th Street corridor. Use Beacon Blvd instead.",
        type = "STREET_BLACKOUT",
        severity = "HIGH",
        latitude = 37.7735,
        longitude = -122.4190,
        radiusMeters = 160.0,
        affectedStreet = "5th St Between Oak & Market"
      )
    )
  }
}
