package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CityDestination
import com.example.data.model.FirestoreRealtimeAlert
import com.example.data.model.HazardZone
import com.example.data.model.RouteOption
import com.example.data.model.RoutePoint
import com.example.data.model.RouteType
import com.example.data.model.SafeHaven
import com.example.data.model.SafetyReportEntity
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.service.RealtimeAlertMonitor

@Composable
fun SafeRouteMapCanvas(
  selectedRoute: RouteOption?,
  destination: CityDestination,
  userLat: Double,
  userLng: Double,
  hazardZones: List<HazardZone>,
  safeHavens: List<SafeHaven>,
  crowdReports: List<SafetyReportEntity>,
  realtimeAlerts: List<FirestoreRealtimeAlert> = emptyList(),
  showLighting: Boolean,
  showCrimeZones: Boolean,
  showSafeHavens: Boolean,
  showCrowdReports: Boolean,
  showRealtimeAlerts: Boolean = true,
  onSafeHavenClick: ((SafeHaven) -> Unit)? = null,
  forceGpsCenterTrigger: Int = 0,
  modifier: Modifier = Modifier
) {
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }

  // Recenter on GPS location whenever forceGpsCenterTrigger increments
  androidx.compose.runtime.LaunchedEffect(forceGpsCenterTrigger) {
    if (forceGpsCenterTrigger > 0) {
      panOffset = Offset.Zero
      zoomScale = 1.0f
    }
  }

  // Dynamic Geographic Bounds centered on active GPS location and destination
  val dLat = kotlin.math.abs(destination.lat - userLat)
  val dLng = kotlin.math.abs(destination.lng - userLng)
  val latSpan = maxOf(0.018, dLat * 1.5).coerceIn(0.012, 0.40)
  val lngSpan = maxOf(0.022, dLng * 1.5).coerceIn(0.015, 0.45)

  val centerLat = if (dLat < 0.25) (userLat + destination.lat) / 2.0 else userLat
  val centerLng = if (dLng < 0.25) (userLng + destination.lng) / 2.0 else userLng

  val minLat = centerLat - latSpan / 2.0
  val maxLat = centerLat + latSpan / 2.0
  val minLng = centerLng - lngSpan / 2.0
  val maxLng = centerLng + lngSpan / 2.0

  // Pulsing animation for safety beacon & alert areas
  val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
  val pulseRadiusFraction by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_radius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.55f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .onSizeChanged { canvasSize = it }
      .pointerInput(safeHavens, zoomScale, panOffset, canvasSize, minLat, maxLat, minLng, maxLng) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          val up = waitForUpOrCancellation()
          if (up != null && onSafeHavenClick != null && canvasSize.width > 0 && canvasSize.height > 0) {
            val tapPos = up.position
            val dxTotal = tapPos.x - down.position.x
            val dyTotal = tapPos.y - down.position.y
            if (dxTotal * dxTotal + dyTotal * dyTotal < 625f) { // Tap gesture (< 25px movement)
              val w = canvasSize.width.toFloat()
              val h = canvasSize.height.toFloat()
              val cx = (w / 2f) + panOffset.x
              val cy = (h / 2f) + panOffset.y

              val clickedHaven = safeHavens.find { haven ->
                val normX = ((haven.lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = 1f - ((haven.lat - minLat) / (maxLat - minLat)).toFloat()
                val screenX = (normX - 0.5f) * w * 1.35f * zoomScale + cx
                val screenY = (normY - 0.5f) * h * 1.35f * zoomScale + cy
                val dx = tapPos.x - screenX
                val dy = tapPos.y - screenY
                (dx * dx + dy * dy) <= (38.dp.toPx() * 38.dp.toPx())
              }

              if (clickedHaven != null) {
                onSafeHavenClick(clickedHaven)
              }
            }
          }
        }
      }
      .pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
          zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.2f)
          panOffset += pan
        }
      }
      .testTag("safe_route_map_canvas")
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val cx = (w / 2f) + panOffset.x
      val cy = (h / 2f) + panOffset.y

      // Dynamic coordinate normalization mapper anchored directly to active GPS and destination
      fun geoToScreen(lat: Double, lng: Double): Offset {
        val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
        val normY = 1f - ((lat - minLat) / (maxLat - minLat)).toFloat()

        val baseX = (normX - 0.5f) * w * 1.35f * zoomScale + cx
        val baseY = (normY - 0.5f) * h * 1.35f * zoomScale + cy
        return Offset(baseX, baseY)
      }

      // 1. Draw Street Grid Network (Dark asphalt corridors with outlines)
      drawStreetGrid(cx, cy, w, h, zoomScale, minLat, maxLat, minLng, maxLng, ::geoToScreen)

      // 2. Draw Street Illumination Glows (Warm amber pools on well-lit streets)
      if (showLighting) {
        drawLightingCones(minLat, maxLat, minLng, maxLng, ::geoToScreen, zoomScale)
      }

      // 3. Draw High-Crime / Hazard Danger Zones (Red pulsating threat bubbles)
      if (showCrimeZones) {
        hazardZones.forEach { zone ->
          val center = geoToScreen(zone.lat, zone.lng)
          val r = (zone.radiusMeters * 0.45f * zoomScale) * pulseRadiusFraction

          // Soft threat aura
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(
                AlertCrimson.copy(alpha = pulseAlpha * 0.7f),
                AlertCrimson.copy(alpha = 0.08f),
                Color.Transparent
              ),
              center = center,
              radius = r
            ),
            center = center,
            radius = r
          )

          // Hazard border
          drawCircle(
            color = AlertCrimson.copy(alpha = 0.65f),
            center = center,
            radius = r * 0.85f,
            style = Stroke(
              width = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
            )
          )

          // Label
          drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
              color = android.graphics.Color.parseColor("#FCA5A5")
              textSize = 28f * zoomScale.coerceIn(0.8f, 1.3f)
              isFakeBoldText = true
              textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("⚠️ ${zone.name}", center.x, center.y - 12f, paint)
          }
        }
      }

      // 4. Draw Route Paths (Selected route prominently highlighted)
      selectedRoute?.let { route ->
        val path = Path()
        val points = route.pathPoints
        if (points.isNotEmpty()) {
          val firstPos = geoToScreen(points.first().lat, points.first().lng)
          path.moveTo(firstPos.x, firstPos.y)
          for (i in 1 until points.size) {
            val p = geoToScreen(points[i].lat, points[i].lng)
            path.lineTo(p.x, p.y)
          }

          if (route.type == RouteType.SAFEST) {
            // Radiant Safe Corridor glow
            drawPath(
              path = path,
              color = SafetyTeal.copy(alpha = 0.28f),
              style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
              path = path,
              color = SafetyTeal,
              style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
          } else if (route.type == RouteType.WELL_LIT) {
            drawPath(
              path = path,
              color = AmberGlow.copy(alpha = 0.32f),
              style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
              path = path,
              color = AmberGlow,
              style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
          } else {
            // Direct / Dangerous route - Red dashed
            drawPath(
              path = path,
              color = AlertCrimson,
              style = Stroke(
                width = 5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 20f))
              )
            )
          }

          // Draw checkpoints along the route
          points.forEachIndexed { idx, pt ->
            val p = geoToScreen(pt.lat, pt.lng)
            val ptColor = if (pt.isHazardous) AlertCrimson else if (route.type == RouteType.SAFEST) SafetyTeal else AmberGlow
            drawCircle(color = MidnightBlack, center = p, radius = 6.dp.toPx())
            drawCircle(color = ptColor, center = p, radius = 4.dp.toPx())
          }
        }
      }

      // 5. Draw 24/7 Safe Havens (Green shield spots & route path highlights)
      if (showSafeHavens) {
        val routePoints = selectedRoute?.pathPoints ?: emptyList()

        safeHavens.forEach { haven ->
          val pos = geoToScreen(haven.lat, haven.lng)
          val distToRoute = if (routePoints.isNotEmpty()) {
            RealtimeAlertMonitor.calculateDistanceToRoute(haven.lat, haven.lng, routePoints)
          } else {
            Double.MAX_VALUE
          }
          val isOnWalkingPath = distToRoute <= 200.0

          if (isOnWalkingPath && routePoints.isNotEmpty()) {
            // Draw illuminated connection line between the walking path and the Safe Haven
            val closestRoutePt = routePoints.minByOrNull {
              RealtimeAlertMonitor.calculateDistanceMeters(it.lat, it.lng, haven.lat, haven.lng)
            }
            if (closestRoutePt != null) {
              val routePos = geoToScreen(closestRoutePt.lat, closestRoutePt.lng)
              drawLine(
                color = SafeHavenGreen.copy(alpha = 0.85f),
                start = routePos,
                end = pos,
                strokeWidth = 3.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
              )
            }

            // Glowing pulsating aura along route
            drawCircle(
              color = SafeHavenGreen.copy(alpha = pulseAlpha * 0.75f),
              center = pos,
              radius = 34.dp.toPx() * zoomScale.coerceIn(0.8f, 1.4f) * pulseRadiusFraction
            )
            drawCircle(
              color = SafeHavenGreen.copy(alpha = 0.35f),
              center = pos,
              radius = 22.dp.toPx() * zoomScale.coerceIn(0.8f, 1.4f)
            )
            drawCircle(
              color = SafeHavenGreen,
              center = pos,
              radius = 13.dp.toPx()
            )
            drawCircle(
              color = Color.White,
              center = pos,
              radius = 6.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.apply {
              val titlePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#34D399")
                textSize = 27f * zoomScale.coerceIn(0.8f, 1.2f)
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
              }
              drawText("🛡️ ${haven.name}", pos.x, pos.y + 36f, titlePaint)

              val badgePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#A7F3D0")
                textSize = 20f * zoomScale.coerceIn(0.8f, 1.1f)
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
              }
              drawText("★ SAFE HAVEN (${distToRoute.toInt()}m)", pos.x, pos.y + 58f, badgePaint)
            }
          } else {
            // Standard Safe Haven off current path
            drawCircle(
              color = SafeHavenGreen.copy(alpha = 0.22f),
              center = pos,
              radius = 20.dp.toPx() * zoomScale.coerceIn(0.8f, 1.4f)
            )
            drawCircle(
              color = SafeHavenGreen,
              center = pos,
              radius = 11.dp.toPx()
            )
            drawCircle(
              color = Color.White,
              center = pos,
              radius = 4.dp.toPx()
            )

            drawContext.canvas.nativeCanvas.apply {
              val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#6EE7B7")
                textSize = 24f * zoomScale.coerceIn(0.8f, 1.2f)
                isFakeBoldText = true
                textAlign = android.graphics.Paint.Align.CENTER
              }
              drawText("🛡️ ${haven.name}", pos.x, pos.y + 34f, paint)
            }
          }
        }
      }

      // 6. Draw Crowd-Sourced Safety Hazard Markers
      if (showCrowdReports) {
        crowdReports.forEach { report ->
          val pos = geoToScreen(report.latitude, report.longitude)
          val markerColor = when {
            report.isLightingRelated -> AmberGlow
            report.severity.name == "HIGH" -> AlertCrimson
            else -> Color(0xFFFBBF24)
          }

          drawCircle(
            color = markerColor.copy(alpha = 0.3f),
            center = pos,
            radius = 16.dp.toPx()
          )
          drawCircle(
            color = markerColor,
            center = pos,
            radius = 8.dp.toPx()
          )
          drawCircle(
            color = MidnightBlack,
            center = pos,
            radius = 3.dp.toPx()
          )

          drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
              color = android.graphics.Color.parseColor("#E2E8F0")
              textSize = 22f * zoomScale.coerceIn(0.7f, 1.1f)
              textAlign = android.graphics.Paint.Align.CENTER
            }
            val symbol = if (report.isLightingRelated) "💡 Outage" else "⚠️ Report"
            drawText(symbol, pos.x, pos.y - 14f, paint)
          }
        }
      }

      // 6.5 Draw Realtime Active Alerts (Police Activity, Accidents, Blackouts)
      if (showRealtimeAlerts) {
        realtimeAlerts.forEach { alert ->
          val pos = geoToScreen(alert.latitude, alert.longitude)
          val alertRadius = (alert.radiusMeters * 0.4f * zoomScale).toFloat() * pulseRadiusFraction

          drawCircle(
            color = AlertCrimson.copy(alpha = 0.28f),
            center = pos,
            radius = alertRadius
          )
          drawCircle(
            color = AlertCrimson,
            center = pos,
            radius = 11.dp.toPx()
          )
          drawCircle(
            color = Color.White,
            center = pos,
            radius = 4.dp.toPx()
          )

          drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
              color = android.graphics.Color.parseColor("#F87171")
              textSize = 26f * zoomScale.coerceIn(0.8f, 1.2f)
              isFakeBoldText = true
              textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("🚨 ${alert.title}", pos.x, pos.y - 20f, paint)
          }
        }
      }

      // 7. Draw Destination Pin
      val destPos = geoToScreen(destination.lat, destination.lng)
      drawCircle(
        color = Color(0xFFF59E0B).copy(alpha = 0.35f),
        center = destPos,
        radius = 28.dp.toPx()
      )
      drawCircle(
        color = Color(0xFFF59E0B),
        center = destPos,
        radius = 14.dp.toPx()
      )
      drawCircle(
        color = Color.White,
        center = destPos,
        radius = 6.dp.toPx()
      )
      drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
          color = android.graphics.Color.parseColor("#FDE68A")
          textSize = 30f * zoomScale.coerceIn(0.8f, 1.3f)
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText("📍 ${destination.name}", destPos.x, destPos.y - 24f, paint)
      }

      // 8. Draw Live User Location Dot (Pulsing Electric Blue Beacon)
      val userPos = geoToScreen(userLat, userLng)
      drawCircle(
        color = ElectricBlue.copy(alpha = pulseAlpha),
        center = userPos,
        radius = 26.dp.toPx() * pulseRadiusFraction
      )
      drawCircle(
        color = ElectricBlue,
        center = userPos,
        radius = 11.dp.toPx()
      )
      drawCircle(
        color = Color.White,
        center = userPos,
        radius = 5.dp.toPx()
      )
      drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
          color = android.graphics.Color.parseColor("#38BDF8")
          textSize = 26f
          isFakeBoldText = true
          textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText("You (Live)", userPos.x, userPos.y + 32f, paint)
      }
    }

    // Recenter Floating Button
    FloatingActionButton(
      onClick = {
        panOffset = Offset.Zero
        zoomScale = 1f
      },
      containerColor = MidnightCard,
      contentColor = ElectricBlue,
      elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 16.dp, end = 16.dp)
        .size(48.dp)
        .testTag("recenter_map_button")
    ) {
      Icon(
        imageVector = Icons.Default.MyLocation,
        contentDescription = "Recenter Map",
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

// Draws the dynamic street network grid and street labels
private fun DrawScope.drawStreetGrid(
  cx: Float,
  cy: Float,
  w: Float,
  h: Float,
  zoom: Float,
  minLat: Double,
  maxLat: Double,
  minLng: Double,
  maxLng: Double,
  toScreen: (Double, Double) -> Offset
) {
  val roadColor = Color(0xFF131C2E)
  val roadBorderColor = MidnightBorder.copy(alpha = 0.35f)

  // 1. Draw dynamic background urban roadway grid for any geographic coordinates
  val gridDivisions = 5
  val latStep = (maxLat - minLat) / gridDivisions
  val lngStep = (maxLng - minLng) / gridDivisions

  for (i in 0..gridDivisions) {
    val lat = minLat + i * latStep
    val p1 = toScreen(lat, minLng)
    val p2 = toScreen(lat, maxLng)
    drawLine(color = roadBorderColor, start = p1, end = p2, strokeWidth = 16.dp.toPx() * zoom.coerceIn(0.7f, 1.3f), cap = StrokeCap.Square)
    drawLine(color = roadColor, start = p1, end = p2, strokeWidth = 11.dp.toPx() * zoom.coerceIn(0.7f, 1.3f), cap = StrokeCap.Square)
  }

  for (j in 0..gridDivisions) {
    val lng = minLng + j * lngStep
    val p1 = toScreen(minLat, lng)
    val p2 = toScreen(maxLat, lng)
    drawLine(color = roadBorderColor, start = p1, end = p2, strokeWidth = 18.dp.toPx() * zoom.coerceIn(0.7f, 1.3f), cap = StrokeCap.Square)
    drawLine(color = roadColor, start = p1, end = p2, strokeWidth = 13.dp.toPx() * zoom.coerceIn(0.7f, 1.3f), cap = StrokeCap.Square)
  }

  // 2. Draw prominent arterial street labels
  for (i in 1 until gridDivisions) {
    val lat = minLat + i * latStep
    val mid = toScreen(lat, (minLng + maxLng) / 2.0)
    drawContext.canvas.nativeCanvas.apply {
      val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#475569")
        textSize = 20f * zoom.coerceIn(0.7f, 1.2f)
        textAlign = android.graphics.Paint.Align.CENTER
      }
      drawText("Safe Corridor ${i * 100} Ave", mid.x, mid.y - 6f, paint)
    }
  }
}

// Draws warm illumination pools for well-lit streets
private fun DrawScope.drawLightingCones(
  minLat: Double,
  maxLat: Double,
  minLng: Double,
  maxLng: Double,
  toScreen: (Double, Double) -> Offset,
  zoom: Float
) {
  val gridDivisions = 4
  val latStep = (maxLat - minLat) / gridDivisions
  val lngStep = (maxLng - minLng) / gridDivisions

  for (i in 0..gridDivisions) {
    for (j in 0..gridDivisions step 2) {
      val lat = minLat + i * latStep
      val lng = minLng + j * lngStep
      val p = toScreen(lat, lng)
      val r = 28.dp.toPx() * zoom.coerceIn(0.7f, 1.4f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            AmberGlow.copy(alpha = 0.28f),
            AmberGlow.copy(alpha = 0.08f),
            Color.Transparent
          ),
          center = p,
          radius = r
        ),
        center = p,
        radius = r
      )
      drawCircle(
        color = Color(0xFFFEF3C7),
        center = p,
        radius = 3.dp.toPx()
      )
    }
  }
}
