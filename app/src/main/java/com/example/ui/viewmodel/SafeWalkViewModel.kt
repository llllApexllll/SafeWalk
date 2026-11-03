package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AppThemeMode
import com.example.data.model.CityDestination
import com.example.data.model.EmergencyContactEntity
import com.example.data.model.FirestoreCommunityReport
import com.example.data.model.FirestoreFavoriteRoute
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.FirestoreUserProfile
import com.example.data.model.HazardZone
import com.example.data.model.ReportCategory
import com.example.data.model.RouteOption
import com.example.data.model.RoutePoint
import com.example.data.model.RouteType
import com.example.data.model.SafeHaven
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel
import com.example.data.model.VerificationStatus
import com.example.data.repository.FirestoreSafetyRepository
import com.example.data.repository.SafetyRepository
import com.example.service.PrioritizedAlert
import com.example.service.RealtimeAlertMonitor
import com.example.service.SafetyToolkitManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapLayerSettings(
  val showLighting: Boolean = true,
  val showCrimeZones: Boolean = true,
  val showSafeHavens: Boolean = true,
  val showCrowdReports: Boolean = true,
  val showRealtimeAlerts: Boolean = true
)

class SafeWalkViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val localRepo = SafetyRepository(database.safetyReportDao(), database.emergencyContactDao())
  val firestoreRepo = FirestoreSafetyRepository(application)
  val alertMonitor = RealtimeAlertMonitor(application)
  val toolkit = SafetyToolkitManager(application)

  // User Profile & Theme State
  val userProfile: StateFlow<FirestoreUserProfile?> = firestoreRepo.observeUserProfile()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  // Favorite Routes
  val favoriteRoutes: StateFlow<List<FirestoreFavoriteRoute>> = firestoreRepo.observeFavoriteRoutes()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Community Reports (Cloud & Local)
  val cloudReports: StateFlow<List<FirestoreCommunityReport>> = firestoreRepo.observeCommunityReports()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val localReports: StateFlow<List<SafetyReportEntity>> = localRepo.allReports
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Previously reported alerts by current user
  val myReportedAlerts: StateFlow<List<FirestoreCommunityReport>> = cloudReports.combine(
    MutableStateFlow(Firebase.auth.currentUser?.uid ?: "")
  ) { reports, uid ->
    if (uid.isBlank()) emptyList() else reports.filter { it.userId == uid }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Real-time Safety Alerts (uses local seed alerts in guest mode or until cloud alerts stream in)
  val realtimeAlerts: StateFlow<List<FirestoreRealtimeAlert>> = firestoreRepo.observeRealtimeAlerts()
    .map { list -> if (list.isNotEmpty()) list else localRepo.getLocalSeedRealtimeAlerts() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), localRepo.getLocalSeedRealtimeAlerts())

  // Emergency Guardians
  val localContacts: StateFlow<List<EmergencyContactEntity>> = localRepo.allContacts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _reportFilterCategory = MutableStateFlow<ReportCategory?>(null)
  val reportFilterCategory: StateFlow<ReportCategory?> = _reportFilterCategory.asStateFlow()

  val hazardZones: List<HazardZone> = localRepo.getHazardZones()
  val safeHavens: List<SafeHaven> = localRepo.getSafeHavens()
  val popularDestinations: List<CityDestination> = localRepo.getPopularDestinations()

  // Selected destination & route planning state
  private val _selectedDestination = MutableStateFlow(popularDestinations.first())
  val selectedDestination: StateFlow<CityDestination> = _selectedDestination.asStateFlow()

  private val _routeOptions = MutableStateFlow<List<RouteOption>>(emptyList())
  val routeOptions: StateFlow<List<RouteOption>> = _routeOptions.asStateFlow()

  private val _selectedRoute = MutableStateFlow<RouteOption?>(null)
  val selectedRoute: StateFlow<RouteOption?> = _selectedRoute.asStateFlow()

  // Active turn-by-turn safe navigation state
  private val _isNavigating = MutableStateFlow(false)
  val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

  private val _currentStepIndex = MutableStateFlow(0)
  val currentStepIndex: StateFlow<Int> = _currentStepIndex.asStateFlow()

  private val _userLat = MutableStateFlow(37.7710)
  val userLat: StateFlow<Double> = _userLat.asStateFlow()

  private val _userLng = MutableStateFlow(-122.4150)
  val userLng: StateFlow<Double> = _userLng.asStateFlow()

  // Map layers
  private val _mapLayers = MutableStateFlow(MapLayerSettings())
  val mapLayers: StateFlow<MapLayerSettings> = _mapLayers.asStateFlow()

  // Prioritized Real-time Safety Alerts (evaluated along route & user proximity)
  private val _prioritizedAlerts = MutableStateFlow<List<PrioritizedAlert>>(emptyList())
  val prioritizedAlerts: StateFlow<List<PrioritizedAlert>> = _prioritizedAlerts.asStateFlow()

  val topRouteAlert: StateFlow<PrioritizedAlert?> = _prioritizedAlerts.combine(_isNavigating) { alerts, _ ->
    alerts.firstOrNull { it.isOnRoute || it.distanceToUserMeters < 300 }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // SOS Emergency & Safety Toolkit State
  private val _isSosActive = MutableStateFlow(false)
  val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

  private val _isSosCountingDown = MutableStateFlow(false)
  val isSosCountingDown: StateFlow<Boolean> = _isSosCountingDown.asStateFlow()

  private val _sosCountdown = MutableStateFlow(3)
  val sosCountdown: StateFlow<Int> = _sosCountdown.asStateFlow()

  private var sosCountdownJob: Job? = null

  // Fake Call simulation
  private val _isFakeCallActive = MutableStateFlow(false)
  val isFakeCallActive: StateFlow<Boolean> = _isFakeCallActive.asStateFlow()

  private val _fakeCallerName = MutableStateFlow("Mom (Mobile)")
  val fakeCallerName: StateFlow<String> = _fakeCallerName.asStateFlow()

  // Safe Arrival Timer
  private val _isArrivalTimerActive = MutableStateFlow(false)
  val isArrivalTimerActive: StateFlow<Boolean> = _isArrivalTimerActive.asStateFlow()

  private val _arrivalTimerSecondsRemaining = MutableStateFlow(900)
  val arrivalTimerSecondsRemaining: StateFlow<Int> = _arrivalTimerSecondsRemaining.asStateFlow()

  private var arrivalTimerJob: Job? = null
  private var navigationSimJob: Job? = null

  init {
    viewModelScope.launch {
      localRepo.initializePreseededDataIfEmpty()
      firestoreRepo.seedInitialCloudDataIfEmpty()
      recalculateRoutes(_selectedDestination.value)
    }

    // Monitor alerts against location and route
    viewModelScope.launch {
      combine(
        realtimeAlerts,
        userLat,
        userLng,
        selectedRoute
      ) { alerts, lat, lng, route ->
        alertMonitor.evaluateAlerts(alerts, lat, lng, route)
      }.collect { prioritized ->
        _prioritizedAlerts.value = prioritized
      }
    }

    // Sync theme from profile if available
    viewModelScope.launch {
      userProfile.collect { profile ->
        profile?.themePreference?.let { pref ->
          runCatching { AppThemeMode.valueOf(pref) }.getOrNull()?.let { mode ->
            _themeMode.value = mode
          }
        }
      }
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
    val current = userProfile.value ?: FirestoreUserProfile(
      userId = Firebase.auth.currentUser?.uid ?: "",
      displayName = Firebase.auth.currentUser?.displayName ?: "User"
    )
    viewModelScope.launch {
      firestoreRepo.saveUserProfile(current.copy(themePreference = mode.name))
    }
  }

  fun saveProfile(
    displayName: String,
    phoneNumber: String,
    bloodType: String,
    physicalCapacity: String,
    chronicIllnesses: String,
    medications: String,
    allergies: String,
    emergencyNotes: String
  ) {
    viewModelScope.launch {
      val uid = Firebase.auth.currentUser?.uid ?: return@launch
      val profile = FirestoreUserProfile(
        userId = uid,
        displayName = displayName,
        email = Firebase.auth.currentUser?.email ?: "",
        phoneNumber = phoneNumber,
        bloodType = bloodType,
        physicalCapacity = physicalCapacity,
        chronicIllnesses = chronicIllnesses,
        medications = medications,
        allergies = allergies,
        emergencyNotes = emergencyNotes,
        themePreference = _themeMode.value.name
      )
      firestoreRepo.saveUserProfile(profile)
    }
  }

  fun saveCurrentRouteAsFavorite(title: String) {
    val route = selectedRoute.value ?: return
    viewModelScope.launch {
      firestoreRepo.addFavoriteRoute(
        title = title.ifBlank { route.title },
        startAddress = "Current Location",
        destinationName = selectedDestination.value.name,
        routeType = route.type.name,
        safetyScore = route.safetyScore.toLong(),
        lightingPercentage = route.lightingPercentage.toLong()
      )
    }
  }

  fun deleteFavoriteRoute(routeId: String) {
    viewModelScope.launch {
      firestoreRepo.deleteFavoriteRoute(routeId)
    }
  }

  fun setFilterCategory(category: ReportCategory?) {
    _reportFilterCategory.value = category
  }

  fun selectDestination(destination: CityDestination) {
    _selectedDestination.value = destination
    recalculateRoutes(destination)
  }

  private fun recalculateRoutes(destination: CityDestination) {
    val baseRoutes = localRepo.calculateRouteOptions(
      startLat = _userLat.value,
      startLng = _userLng.value,
      dest = destination
    )
    _routeOptions.value = baseRoutes
    _selectedRoute.value = baseRoutes.find { it.type == RouteType.SAFEST } ?: baseRoutes.firstOrNull()
  }

  fun selectRoute(route: RouteOption) {
    _selectedRoute.value = route
    _prioritizedAlerts.value = alertMonitor.evaluateAlerts(
      realtimeAlerts.value,
      _userLat.value,
      _userLng.value,
      route
    )
  }

  fun getSafeHavensAlongRoute(route: RouteOption? = _selectedRoute.value): List<Pair<SafeHaven, Int>> {
    val r = route ?: return emptyList()
    val points = r.pathPoints
    if (points.isEmpty()) return emptyList()

    return safeHavens.mapNotNull { haven ->
      val distMeters = RealtimeAlertMonitor.calculateDistanceToRoute(haven.lat, haven.lng, points)
      if (distMeters <= 220.0) { // within 220 meters of the walking path
        haven to distMeters.toInt()
      } else null
    }.sortedBy { it.second }
  }

  fun navigateToSafeHaven(haven: SafeHaven) {
    val destination = CityDestination(
      id = "haven_${haven.id}",
      name = "🛡️ ${haven.name}",
      category = "Safe Haven",
      address = haven.address,
      lat = haven.lat,
      lng = haven.lng
    )
    selectDestination(destination)
    startNavigation()
  }

  fun startNavigation() {
    _isNavigating.value = true
    _currentStepIndex.value = 0

    navigationSimJob?.cancel()
    navigationSimJob = viewModelScope.launch {
      val route = _selectedRoute.value ?: return@launch
      val steps = route.navigationSteps
      val points = route.pathPoints

      for (i in points.indices) {
        if (!_isNavigating.value) break
        _userLat.value = points[i].lat
        _userLng.value = points[i].lng
        val stepIdx = ((i.toFloat() / points.size) * steps.size).toInt().coerceAtMost(steps.size - 1)
        _currentStepIndex.value = stepIdx

        val evaluated = alertMonitor.evaluateAlerts(
          realtimeAlerts.value,
          points[i].lat,
          points[i].lng,
          route
        )
        _prioritizedAlerts.value = evaluated

        val imminentThreat = evaluated.firstOrNull { it.isOnRoute && it.distanceToUserMeters < 250 }
        if (imminentThreat != null) {
          alertMonitor.pushSafetyConcernNotification(imminentThreat)
        }

        delay(6000)
      }
    }
  }

  fun stopNavigation() {
    _isNavigating.value = false
    navigationSimJob?.cancel()
    navigationSimJob = null
    _currentStepIndex.value = 0
  }

  fun nextStep() {
    val maxSteps = _selectedRoute.value?.navigationSteps?.size ?: 1
    if (_currentStepIndex.value < maxSteps - 1) {
      _currentStepIndex.value++
    }
  }

  fun previousStep() {
    if (_currentStepIndex.value > 0) {
      _currentStepIndex.value--
    }
  }

  fun toggleLayer(layer: String) {
    val cur = _mapLayers.value
    _mapLayers.value = when (layer) {
      "lighting" -> cur.copy(showLighting = !cur.showLighting)
      "crime" -> cur.copy(showCrimeZones = !cur.showCrimeZones)
      "havens" -> cur.copy(showSafeHavens = !cur.showSafeHavens)
      "reports" -> cur.copy(showCrowdReports = !cur.showCrowdReports)
      "alerts" -> cur.copy(showRealtimeAlerts = !cur.showRealtimeAlerts)
      else -> cur
    }
  }

  // --- SOS Logic with Medical Info and Location Broadcast ---
  fun startSosHoldCountdown() {
    if (_isSosActive.value) return
    _isSosCountingDown.value = true
    _sosCountdown.value = 3

    sosCountdownJob?.cancel()
    sosCountdownJob = viewModelScope.launch {
      for (i in 3 downTo 1) {
        _sosCountdown.value = i
        delay(1000)
      }
      _isSosCountingDown.value = false
      activateSosProtocol("911")
    }
  }

  fun cancelSosCountdown() {
    sosCountdownJob?.cancel()
    sosCountdownJob = null
    _isSosCountingDown.value = false
    _sosCountdown.value = 3
  }

  fun activateSosProtocol(preferredEmergencyNumber: String = "911") {
    _isSosActive.value = true
    val phoneList = localContacts.value.filter { it.notifyOnSos }.map { it.phoneNumber }
    val currentAddr = "Beacon Blvd near 450 Grand Ave"
    val profile = userProfile.value

    val medicalSnippet = if (profile != null && profile.medications != "None declared") {
      " [Medical Note: ${profile.physicalCapacity}, Meds: ${profile.medications}]"
    } else ""

    val distressMsg = "EMERGENCY: I am walking alone and in danger! Current GPS location: https://maps.google.com/?q=${_userLat.value},${_userLng.value} ($currentAddr).$medicalSnippet Please check on me or dispatch help immediately!"

    toolkit.triggerFullSos(
      phoneNumbers = phoneList,
      currentAddress = currentAddr,
      lat = _userLat.value,
      lng = _userLng.value
    )

    viewModelScope.launch {
      firestoreRepo.logSosActivation(
        lat = _userLat.value,
        lng = _userLng.value,
        address = currentAddr,
        distressMessage = distressMsg,
        contactsNotified = phoneList.joinToString(", "),
        dialedNumber = preferredEmergencyNumber
      )
    }
  }

  fun dismissSos() {
    _isSosActive.value = false
    _isSosCountingDown.value = false
    toolkit.stopAllEmergencyProtocols()
  }

  // --- Fake Call Logic ---
  fun triggerFakeCall(callerName: String = "Mom (Mobile)") {
    _fakeCallerName.value = callerName
    _isFakeCallActive.value = true
    toolkit.triggerSosVibration()
  }

  fun dismissFakeCall() {
    _isFakeCallActive.value = false
  }

  // --- Safe Arrival Timer ---
  fun startArrivalTimer(minutes: Int = 15) {
    _arrivalTimerSecondsRemaining.value = minutes * 60
    _isArrivalTimerActive.value = true

    arrivalTimerJob?.cancel()
    arrivalTimerJob = viewModelScope.launch {
      while (_arrivalTimerSecondsRemaining.value > 0 && _isArrivalTimerActive.value) {
        delay(1000)
        _arrivalTimerSecondsRemaining.value--
      }
      if (_isArrivalTimerActive.value && _arrivalTimerSecondsRemaining.value == 0) {
        activateSosProtocol("911")
      }
    }
  }

  fun stopArrivalTimer() {
    _isArrivalTimerActive.value = false
    arrivalTimerJob?.cancel()
  }

  // --- Crowd-Sourced Reports ---
  fun submitReport(
    title: String,
    description: String,
    category: ReportCategory,
    severity: SeverityLevel,
    isLighting: Boolean,
    isAnonymous: Boolean = true
  ) {
    viewModelScope.launch {
      firestoreRepo.submitReport(
        title = title,
        description = description,
        category = category,
        severity = severity,
        latitude = _userLat.value + (Math.random() - 0.5) * 0.003,
        longitude = _userLng.value + (Math.random() - 0.5) * 0.003,
        addressName = "Current GPS Location",
        isAnonymous = isAnonymous
      )

      val localEntity = SafetyReportEntity(
        title = title,
        description = description,
        category = category,
        severity = severity,
        latitude = _userLat.value,
        longitude = _userLng.value,
        addressName = "Reported at Current GPS Location",
        timestampFormatted = "Just now",
        upvotes = 1,
        isVerified = false,
        isLightingRelated = isLighting,
        userReported = true,
        isAnonymous = isAnonymous,
        verificationStatus = VerificationStatus.PENDING_REVIEW
      )
      localRepo.addReport(localEntity)
      recalculateRoutes(_selectedDestination.value)
    }
  }

  fun upvoteReport(cloudReport: FirestoreCommunityReport) {
    viewModelScope.launch {
      firestoreRepo.upvoteReport(cloudReport)
      recalculateRoutes(_selectedDestination.value)
    }
  }

  fun upvoteLocalReport(id: Long) {
    viewModelScope.launch {
      localRepo.upvoteReport(id)
    }
  }

  // --- Contacts Management ---
  fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
    viewModelScope.launch {
      firestoreRepo.addEmergencyContact(name, phone, relationship, isPrimary)
      val contact = EmergencyContactEntity(
        name = name,
        phoneNumber = phone,
        relationship = relationship,
        isPrimary = isPrimary,
        notifyOnSos = true
      )
      localRepo.addContact(contact)
    }
  }

  fun deleteContact(contact: EmergencyContactEntity) {
    viewModelScope.launch {
      if (contact.firestoreId.isNotBlank()) {
        firestoreRepo.deleteEmergencyContact(contact.firestoreId)
      }
      localRepo.deleteContact(contact)
    }
  }

  override fun onCleared() {
    super.onCleared()
    toolkit.stopAllEmergencyProtocols()
    navigationSimJob?.cancel()
    arrivalTimerJob?.cancel()
    sosCountdownJob?.cancel()
  }
}
