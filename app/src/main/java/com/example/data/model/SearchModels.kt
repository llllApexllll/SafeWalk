package com.example.data.model

data class SearchResultItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: String, // "COORDINATES", "ESTABLISHMENT", "ADDRESS", "SAFE_HAVEN"
  val lat: Double,
  val lng: Double,
  val distanceMeters: Int? = null,
  val isSafeHaven: Boolean = false,
  val safeHavenPhone: String? = null
)

object CoordinateParser {
  /**
   * Parses coordinates formatted as "lat, lng" or "lat lng" (e.g. "37.7749, -122.4194").
   * Validates bounds (-90 <= lat <= 90 and -180 <= lng <= 180).
   */
  fun parse(query: String): Pair<Double, Double>? {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return null

    // Match patterns like "37.7749, -122.4194" or "37.7749 -122.4194" or "37.7749,-122.4194"
    val regex = Regex("""^([+-]?\d{1,3}(?:\.\d+)?)[,\s]+([+-]?\d{1,3}(?:\.\d+)?)$""")
    val match = regex.find(trimmed)
    if (match != null) {
      val lat = match.groupValues[1].toDoubleOrNull()
      val lng = match.groupValues[2].toDoubleOrNull()
      if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
        return Pair(lat, lng)
      }
    }
    return null
  }
}
