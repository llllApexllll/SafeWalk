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

  // Generates 3 realistic routes between coordinates
  fun calculateRouteOptions(
    startLat: Double = 37.7710,
    startLng: Double = -122.4150, // Metro Station Central
    dest: CityDestination
  ): List<RouteOption> {
    // 1. SAFEST ROUTE (Recommended)
    // Routes along illuminated Beacon Blvd, past 24/7 CVS and University Police, avoiding Elm St & Park
    val safestPoints = listOf(
      RoutePoint(startLat, startLng, "Metro Plaza Entrance", 95),
      RoutePoint(37.7730, -122.4135, "Transit Way (Bright LED)", 98),
      RoutePoint(37.7750, -122.4120, "Beacon Blvd South", 100),
      RoutePoint(37.7765, -122.4110, "Beacon Blvd (Passes 24/7 Store)", 100),
      RoutePoint(37.7785, -122.4115, "Grand Ave Junction (CVS Guard)", 98),
      RoutePoint(37.7800, -122.4105, "University Ave (Campus Patrol)", 96),
      RoutePoint(dest.lat, dest.lng, dest.name, 95)
    )
    val safestSteps = listOf(
      NavigationStep("Start walking north from Metro Plaza", "Metro Plaza", 80, "Bright (95%)", "Metro Police Station"),
      NavigationStep("Turn right onto Beacon Blvd", "Beacon Blvd", 450, "Fully Illuminated (100%)", "24/7 QuickStop Market"),
      NavigationStep("Continue straight along illuminated shopfronts", "Beacon Blvd", 350, "Bright LED (100%)", null),
      NavigationStep("Turn left onto Grand Ave near 24/7 CVS", "Grand Ave", 280, "Very Bright (98%)", "CVS Pharmacy (Staff & Guard)"),
      NavigationStep("Turn right into University Gate", "North Hall Walk", 150, "Well Lit (95%)", "Campus Security Post")
    )
    val safestRoute = RouteOption(
      id = "route_safest",
      type = RouteType.SAFEST,
      title = "Safest Route (Via Beacon Blvd)",
      distanceKm = 1.35f,
      estimatedMinutes = 17,
      safetyScore = 96,
      lightingPercentage = 98,
      safeHavensCount = 3,
      hazardsAvoidedCount = 3,
      unlitStreetsCount = 0,
      summaryNote = "Avoids 3 high-risk unlit areas. Continuous streetlamps, active CCTV, passes 2 open safe havens.",
      pathPoints = safestPoints,
      navigationSteps = safestSteps
    )

    // 2. WELL-LIT ONLY ROUTE
    // Strictly follows 100% illuminated avenues
    val wellLitPoints = listOf(
      RoutePoint(startLat, startLng, "Metro Plaza Entrance", 95),
      RoutePoint(37.7725, -122.4130, "Avenue of Lights", 100),
      RoutePoint(37.7755, -122.4110, "Beacon Blvd Central", 100),
      RoutePoint(37.7785, -122.4110, "Upper Promenade", 100),
      RoutePoint(37.7805, -122.4100, "Campus Perimeter Road", 98),
      RoutePoint(dest.lat, dest.lng, dest.name, 95)
    )
    val wellLitSteps = listOf(
      NavigationStep("Walk along Avenue of Lights", "Avenue of Lights", 300, "100% Illumination", null),
      NavigationStep("Follow Beacon Blvd pedestrian corridor", "Beacon Blvd", 600, "100% Illumination", "QuickStop Market"),
      NavigationStep("Continue on Upper Promenade", "Upper Promenade", 400, "100% Illumination", "Campus Security"),
      NavigationStep("Arrive at destination", dest.name, 100, "95% Illumination", null)
    )
    val wellLitRoute = RouteOption(
      id = "route_well_lit",
      type = RouteType.WELL_LIT,
      title = "Maximum Illumination Route",
      distanceKm = 1.48f,
      estimatedMinutes = 19,
      safetyScore = 92,
      lightingPercentage = 100,
      safeHavensCount = 2,
      hazardsAvoidedCount = 3,
      unlitStreetsCount = 0,
      summaryNote = "Guarantees 100% well-lit wide sidewalks. Slightly longer (+2 mins) but maximum visibility.",
      pathPoints = wellLitPoints,
      navigationSteps = wellLitSteps
    )

    // 3. DIRECT / FASTEST (Hazardous)
    // Cuts through Elm St (unlit) and South Park edge (crime hotspot)
    val directPoints = listOf(
      RoutePoint(startLat, startLng, "Metro Plaza Entrance", 95),
      RoutePoint(37.7728, -122.4180, "South Park Gravel Shortcut", 20, isHazardous = true, hazardReason = "Harassment Hotspot"),
      RoutePoint(37.7745, -122.4195, "Rail Underpass Tunnel", 30, isHazardous = true, hazardReason = "Poor Visibility & Loitering"),
      RoutePoint(37.7760, -122.4180, "Elm St (Broken Streetlights)", 15, isHazardous = true, hazardReason = "0% Lighting (Pitch Black)"),
      RoutePoint(37.7788, -122.4150, "4th St Cut-Through", 55),
      RoutePoint(dest.lat, dest.lng, dest.name, 95)
    )
    val directSteps = listOf(
      NavigationStep("Walk north towards South Park edge", "South Park Cut", 250, "Poor (20%)", null, "WARNING: Recent harassment reported"),
      NavigationStep("Pass under dark railway underpass", "Underpass", 180, "Dark (30%)", null, "CAUTION: Blind corner, poor visibility"),
      NavigationStep("Walk through unlit Elm St", "Elm St", 320, "Pitch Black (15%)", null, "DANGER: 3 broken street lamps reported"),
      NavigationStep("Reach destination", dest.name, 200, "Moderate (55%)", null)
    )
    val directRoute = RouteOption(
      id = "route_direct",
      type = RouteType.DIRECT,
      title = "Fastest Direct (High Danger)",
      distanceKm = 0.95f,
      estimatedMinutes = 12,
      safetyScore = 48,
      lightingPercentage = 38,
      safeHavensCount = 0,
      hazardsAvoidedCount = 0,
      unlitStreetsCount = 2,
      summaryNote = "WARNING: Passes 2 unlit blind corridors and 1 high-crime hotspot. Not recommended when walking alone after dark.",
      pathPoints = directPoints,
      navigationSteps = directSteps
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
