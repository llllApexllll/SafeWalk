package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CityDestination
import com.example.data.model.EmergencyContactEntity
import com.example.data.model.HazardZone
import com.example.data.model.ReportCategory
import com.example.data.model.RouteOption
import com.example.data.model.RouteType
import com.example.data.model.SafeHaven
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel
import com.example.data.repository.SafetyRepository
import com.example.service.SafetyToolkitManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapLayerSettings(
  val showLighting: Boolean = true,
  val showCrimeZones: Boolean = true,
  val showSafeHavens: Boolean = true,
  val showCrowdReports: Boolean = true
)

class SafeWalkViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val repository = SafetyRepository(database.safetyReportDao(), database.emergencyContactDao())
  val toolkit = SafetyToolkitManager(application)

  val allContacts: StateFlow<List<EmergencyContactEntity>> = repository.allContacts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _reportFilterCategory = MutableStateFlow<ReportCategory?>(null)
  val reportFilterCategory: StateFlow<ReportCategory?> = _reportFilterCategory.asStateFlow()

  val filteredReports: StateFlow<List<SafetyReportEntity>> = combine(
    repository.allReports,
    _reportFilterCategory
  ) { reports, filter ->
    if (filter == null) reports else reports.filter { it.category == filter }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val hazardZones: List<HazardZone> = repository.getHazardZones()
  val safeHavens: List<SafeHaven> = repository.getSafeHavens()
  val popularDestinations: List<CityDestination> = repository.getPopularDestinations()

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

  private val _arrivalTimerSecondsRemaining = MutableStateFlow(900) // 15 mins default
  val arrivalTimerSecondsRemaining: StateFlow<Int> = _arrivalTimerSecondsRemaining.asStateFlow()

  private var arrivalTimerJob: Job? = null
  private var navigationSimJob: Job? = null

  init {
    viewModelScope.launch {
      repository.initializePreseededDataIfEmpty()
      recalculateRoutes(_selectedDestination.value)
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
    val routes = repository.calculateRouteOptions(
      startLat = _userLat.value,
      startLng = _userLng.value,
      dest = destination
    )
    _routeOptions.value = routes
    _selectedRoute.value = routes.firstOrNull { it.type == RouteType.SAFEST } ?: routes.firstOrNull()
  }

  fun selectRoute(route: RouteOption) {
    _selectedRoute.value = route
  }

  fun startNavigation() {
    _isNavigating.value = true
    _currentStepIndex.value = 0

    // Start auto-advancing location simulation along route path for visual immersion
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
        delay(6000) // Progress checkpoint every 6 seconds
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
      else -> cur
    }
  }

  // --- SOS Logic ---
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
      activateSosProtocol()
    }
  }

  fun cancelSosCountdown() {
    sosCountdownJob?.cancel()
    sosCountdownJob = null
    _isSosCountingDown.value = false
    _sosCountdown.value = 3
  }

  fun activateSosProtocol() {
    _isSosActive.value = true
    val phoneList = allContacts.value.filter { it.notifyOnSos }.map { it.phoneNumber }
    toolkit.triggerFullSos(
      phoneNumbers = phoneList,
      currentAddress = "Beacon Blvd near 450 Grand Ave",
      lat = _userLat.value,
      lng = _userLng.value
    )
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
        // Timer expired without check-in! Trigger safety protocol
        activateSosProtocol()
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
    isLighting: Boolean
  ) {
    viewModelScope.launch {
      val newReport = SafetyReportEntity(
        title = title,
        description = description,
        category = category,
        severity = severity,
        latitude = _userLat.value + (Math.random() - 0.5) * 0.004,
        longitude = _userLng.value + (Math.random() - 0.5) * 0.004,
        addressName = "Reported at Current GPS Pin",
        timestampFormatted = "Just now",
        upvotes = 1,
        isVerified = false,
        isLightingRelated = isLighting,
        userReported = true
      )
      repository.addReport(newReport)
    }
  }

  fun upvoteReport(id: Long) {
    viewModelScope.launch {
      repository.upvoteReport(id)
    }
  }

  // --- Contacts Management ---
  fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
    viewModelScope.launch {
      val contact = EmergencyContactEntity(
        name = name,
        phoneNumber = phone,
        relationship = relationship,
        isPrimary = isPrimary,
        notifyOnSos = true
      )
      repository.addContact(contact)
    }
  }

  fun deleteContact(contact: EmergencyContactEntity) {
    viewModelScope.launch {
      repository.deleteContact(contact)
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
