package com.example.service

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.example.data.model.CityDestination
import com.example.data.model.CoordinateParser
import com.example.data.model.SafeHaven
import com.example.data.model.SearchResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchLocationService(private val context: Context) {

  suspend fun search(
    query: String,
    userLat: Double,
    userLng: Double,
    safeHavens: List<SafeHaven>,
    popularDestinations: List<CityDestination>
  ): List<SearchResultItem> = withContext(Dispatchers.IO) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return@withContext emptyList()

    val results = mutableListOf<SearchResultItem>()

    // 1. Direct Coordinates Parsing (e.g. "37.7749, -122.4194" or "40.7128 -74.0060")
    val coords = CoordinateParser.parse(trimmed)
    if (coords != null) {
      val dist = RealtimeAlertMonitor.calculateDistanceMeters(userLat, userLng, coords.first, coords.second).toInt()
      results.add(
        SearchResultItem(
          id = "coord_${coords.first}_${coords.second}",
          title = "GPS Coordinates Destination",
          subtitle = "Lat: ${String.format(Locale.US, "%.5f", coords.first)}, Lng: ${String.format(Locale.US, "%.5f", coords.second)}",
          category = "COORDINATES",
          lat = coords.first,
          lng = coords.second,
          distanceMeters = dist
        )
      )
    }

    // 2. Safe Havens Matching (Establishment Name, Type, Address)
    safeHavens.filter { haven ->
      haven.name.contains(trimmed, ignoreCase = true) ||
      haven.address.contains(trimmed, ignoreCase = true) ||
      haven.type.name.replace("_", " ").contains(trimmed, ignoreCase = true)
    }.forEach { haven ->
      val dist = RealtimeAlertMonitor.calculateDistanceMeters(userLat, userLng, haven.lat, haven.lng).toInt()
      results.add(
        SearchResultItem(
          id = "haven_${haven.id}",
          title = haven.name,
          subtitle = "${haven.address} • 24/7 Verified Safe Haven",
          category = "SAFE_HAVEN",
          lat = haven.lat,
          lng = haven.lng,
          distanceMeters = dist,
          isSafeHaven = true,
          safeHavenPhone = haven.phone
        )
      )
    }

    // 3. Known Popular Destinations / Hubs
    popularDestinations.filter { dest ->
      dest.name.contains(trimmed, ignoreCase = true) ||
      dest.address.contains(trimmed, ignoreCase = true) ||
      dest.category.contains(trimmed, ignoreCase = true)
    }.forEach { dest ->
      if (results.none { it.lat == dest.lat && it.lng == dest.lng }) {
        val dist = RealtimeAlertMonitor.calculateDistanceMeters(userLat, userLng, dest.lat, dest.lng).toInt()
        results.add(
          SearchResultItem(
            id = "dest_${dest.id}",
            title = dest.name,
            subtitle = "${dest.address} • ${dest.category}",
            category = "ESTABLISHMENT",
            lat = dest.lat,
            lng = dest.lng,
            distanceMeters = dist
          )
        )
      }
    }

    // 4. Android Native Geocoder for Real-World Addresses & Establishments
    if (Geocoder.isPresent()) {
      try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocationName(trimmed, 5)
        addresses?.forEachIndexed { index, addr ->
          val feature = addr.featureName
          val thoroughfare = addr.thoroughfare
          val subThoroughfare = addr.subThoroughfare
          val locality = addr.locality

          val title = when {
            feature != null && feature.isNotEmpty() && feature != thoroughfare -> feature
            thoroughfare != null -> if (subThoroughfare != null) "$subThoroughfare $thoroughfare" else thoroughfare
            locality != null -> locality
            else -> trimmed
          }

          val addressLines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
          val fullAddress = if (addressLines.isNotEmpty()) addressLines.joinToString(", ") else "$title, ${addr.locality ?: ""}"
          val dist = RealtimeAlertMonitor.calculateDistanceMeters(userLat, userLng, addr.latitude, addr.longitude).toInt()

          // Deduplicate if already present
          if (results.none { Math.abs(it.lat - addr.latitude) < 0.0001 && Math.abs(it.lng - addr.longitude) < 0.0001 }) {
            results.add(
              SearchResultItem(
                id = "geo_${addr.latitude}_${addr.longitude}_$index",
                title = title,
                subtitle = fullAddress,
                category = "ADDRESS",
                lat = addr.latitude,
                lng = addr.longitude,
                distanceMeters = dist
              )
            )
          }
        }
      } catch (e: Exception) {
        Log.w("SearchLocationService", "Geocoder lookup encountered error: ${e.message}")
      }
    }

    // Sort: Coordinates first if exact match, otherwise by proximity to user
    results.sortedWith(
      compareBy<SearchResultItem> { it.category != "COORDINATES" }
        .thenBy { it.distanceMeters ?: Int.MAX_VALUE }
    )
  }
}
