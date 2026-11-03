package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityDestination
import com.example.data.model.RouteOption
import com.example.data.model.RouteType
import com.example.data.model.SeverityLevel
import com.example.ui.components.SafeRouteMapCanvas
import com.example.ui.components.SosActiveOverlay
import com.example.ui.components.SosCountdownDialog
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonBg
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.SafeWalkViewModel

@Composable
fun SafeMapScreen(
  viewModel: SafeWalkViewModel,
  onOpenReportDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedDestination by viewModel.selectedDestination.collectAsState()
  val routeOptions by viewModel.routeOptions.collectAsState()
  val selectedRoute by viewModel.selectedRoute.collectAsState()
  val isNavigating by viewModel.isNavigating.collectAsState()
  val currentStepIdx by viewModel.currentStepIndex.collectAsState()
  val userLat by viewModel.userLat.collectAsState()
  val userLng by viewModel.userLng.collectAsState()
  val mapLayers by viewModel.mapLayers.collectAsState()
  val localReports by viewModel.localReports.collectAsState()
  val realtimeAlerts by viewModel.realtimeAlerts.collectAsState()
  val topRouteAlert by viewModel.topRouteAlert.collectAsState()
  val contacts by viewModel.localContacts.collectAsState()

  val isSosActive by viewModel.isSosActive.collectAsState()
  val isSosCountingDown by viewModel.isSosCountingDown.collectAsState()
  val sosCountdown by viewModel.sosCountdown.collectAsState()
  val isSirenActive by viewModel.toolkit.isSirenActive.collectAsState()
  val isStrobeActive by viewModel.toolkit.isStrobeActive.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .testTag("safe_map_screen")
  ) {
    // 1. Vector Interactive Map with Route & Hazards
    SafeRouteMapCanvas(
      selectedRoute = selectedRoute,
      destination = selectedDestination,
      userLat = userLat,
      userLng = userLng,
      hazardZones = viewModel.hazardZones,
      safeHavens = viewModel.safeHavens,
      crowdReports = localReports,
      realtimeAlerts = realtimeAlerts,
      showLighting = mapLayers.showLighting,
      showCrimeZones = mapLayers.showCrimeZones,
      showSafeHavens = mapLayers.showSafeHavens,
      showCrowdReports = mapLayers.showCrowdReports,
      showRealtimeAlerts = mapLayers.showRealtimeAlerts,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Top Bar: Title, SOS Trigger & Real-time Alert Banner
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        // App identity pill
        Surface(
          color = MidnightSurface.copy(alpha = 0.94f),
          shape = RoundedCornerShape(20.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder))
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = AmberGlow,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "SafeWalk Live",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Real-Time Hazard Guardian",
                color = SafetyTeal,
                fontSize = 11.sp
              )
            }
          }
        }

        // Quick Emergency SOS Button (Hold to activate)
        Button(
          onClick = { viewModel.startSosHoldCountdown() },
          colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .height(42.dp)
            .testTag("quick_sos_trigger_button")
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "SOS",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "SOS", fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Layer Filter Chips Floating Row
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
      ) {
        FilterChip(
          selected = mapLayers.showRealtimeAlerts,
          onClick = { viewModel.toggleLayer("alerts") },
          label = { Text("🚨 Live Alerts (${realtimeAlerts.size})", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AlertCrimson,
            selectedLabelColor = Color.White,
            containerColor = MidnightSurface.copy(alpha = 0.85f),
            labelColor = Color(0xFFE2E8F0)
          )
        )
        FilterChip(
          selected = mapLayers.showLighting,
          onClick = { viewModel.toggleLayer("lighting") },
          label = { Text("💡 Streetlamps", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AmberGlow,
            selectedLabelColor = MidnightBlack,
            containerColor = MidnightSurface.copy(alpha = 0.85f),
            labelColor = Color(0xFFE2E8F0)
          )
        )
        FilterChip(
          selected = mapLayers.showSafeHavens,
          onClick = { viewModel.toggleLayer("havens") },
          label = { Text("🛡️ 24/7 Havens", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SafeHavenGreen,
            selectedLabelColor = MidnightBlack,
            containerColor = MidnightSurface.copy(alpha = 0.85f),
            labelColor = Color(0xFFE2E8F0)
          )
        )
        FilterChip(
          selected = mapLayers.showCrowdReports,
          onClick = { viewModel.toggleLayer("reports") },
          label = { Text("📍 Crowd Reports", fontSize = 12.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SafetyTeal,
            selectedLabelColor = MidnightBlack,
            containerColor = MidnightSurface.copy(alpha = 0.85f),
            labelColor = Color(0xFFE2E8F0)
          )
        )
      }

      // Prioritized Real-Time Hazard Alert Banner (Push Notification card)
      topRouteAlert?.let { alertItem ->
        Spacer(modifier = Modifier.height(8.dp))
        val isCritical = alertItem.alert.severity == SeverityLevel.CRITICAL.name
        val bannerBg = if (isCritical) AlertCrimsonBg else Color(0xFF452B00)
        val bannerBorder = if (isCritical) AlertCrimson else AmberGlow

        Card(
          colors = CardDefaults.cardColors(containerColor = bannerBg.copy(alpha = 0.95f)),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(bannerBorder)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("top_route_alert_banner")
        ) {
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(
              imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = if (isCritical) AlertCrimson else AmberGlow,
              modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = alertItem.alert.title,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  fontSize = 13.sp
                )
                Surface(
                  color = if (isCritical) AlertCrimson else AmberGlow,
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = alertItem.proximityLabel,
                    color = MidnightBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(3.dp))

              Text(
                text = alertItem.alert.message,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                lineHeight = 15.sp
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "👉 ${alertItem.recommendedAction}",
                color = if (isCritical) Color(0xFFFCA5A5) else Color(0xFFFEF3C7),
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      // Active SOS Warning Overlay
      if (isSosActive) {
        SosActiveOverlay(
          isSirenActive = isSirenActive,
          isStrobeActive = isStrobeActive,
          contacts = contacts,
          onToggleSiren = { viewModel.toolkit.toggleSiren() },
          onToggleStrobe = { viewModel.toolkit.toggleStrobe() },
          onCall911 = { viewModel.toolkit.dialEmergencyNumber("911") },
          onDispatchSms = { viewModel.activateSosProtocol() },
          onDismiss = { viewModel.dismissSos() }
        )
      }
    }

    // 3. Bottom Panel: Navigation Controls or Route Selector
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = 12.dp)
    ) {
      if (isNavigating && selectedRoute != null) {
        // Active Turn-by-Turn Safe Guidance View
        val route = selectedRoute!!
        val steps = route.navigationSteps
        val step = steps.getOrNull(currentStepIdx) ?: steps.first()

        Card(
          colors = CardDefaults.cardColors(containerColor = MidnightSurface.copy(alpha = 0.96f)),
          shape = RoundedCornerShape(24.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafetyTeal)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("active_navigation_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SafetyTeal)
                ) {
                  Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = MidnightBlack,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "SAFE REAL-TIME NAVIGATION ACTIVE",
                    color = SafetyTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                  Text(
                    text = "${route.title} • ${route.estimatedMinutes} min",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                }
              }

              IconButton(
                onClick = { viewModel.stopNavigation() },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("stop_navigation_button")
              ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit", tint = Color(0xFF94A3B8))
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Instruction
            Text(
              text = step.instruction,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Lighting: ${step.lightingLevel}",
                color = AmberGlow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
              if (step.nearbySafeHaven != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = SafeHavenGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Haven: ${step.nearbySafeHaven}",
                  color = SafeHavenGreen,
                  fontSize = 12.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            if (step.warningNote != null) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "⚠️ ${step.warningNote}",
                color = AlertCrimson,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Navigation Controls
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedButton(
                onClick = { viewModel.previousStep() },
                enabled = currentStepIdx > 0,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("prev_step_button")
              ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Prev", fontSize = 12.sp)
              }

              Text(
                text = "Step ${currentStepIdx + 1} of ${steps.size}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
              )

              if (currentStepIdx < steps.size - 1) {
                Button(
                  onClick = { viewModel.nextStep() },
                  colors = ButtonDefaults.buttonColors(containerColor = SafetyTeal),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("next_step_button")
                ) {
                  Text("Next Step", color = MidnightBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(16.dp))
                }
              } else {
                Button(
                  onClick = { viewModel.stopNavigation() },
                  colors = ButtonDefaults.buttonColors(containerColor = SafeHavenGreen),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("arrived_safely_button")
                ) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Arrived Safely", color = MidnightBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        // Route Selection & Comparison Bottom Sheet
        Card(
          colors = CardDefaults.cardColors(containerColor = MidnightSurface.copy(alpha = 0.96f)),
          shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("route_selection_panel")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp)
          ) {
            // Destination Selector
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = "SAFE DESTINATION",
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = selectedDestination.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }

              // Report hazard quick button
              OutlinedButton(
                onClick = onOpenReportDialog,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGlow),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AmberGlow)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .height(38.dp)
                  .testTag("report_hazard_map_button")
              ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Report Hazard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Popular destinations chips
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
              viewModel.popularDestinations.forEach { dest ->
                val isSelected = dest.id == selectedDestination.id
                Surface(
                  color = if (isSelected) SafetyTeal else MidnightCard,
                  shape = RoundedCornerShape(14.dp),
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) SafetyTeal else MidnightBorder)
                  ),
                  modifier = Modifier
                    .clickable { viewModel.selectDestination(dest) }
                    .testTag("dest_chip_${dest.id}")
                ) {
                  Text(
                    text = dest.name,
                    color = if (isSelected) MidnightBlack else Color(0xFFCBD5E1),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route Options Cards (Safest vs Well-Lit vs Direct)
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
            ) {
              routeOptions.forEach { route ->
                val isChosen = route.id == selectedRoute?.id
                val borderColor = when (route.type) {
                  RouteType.SAFEST -> SafetyTeal
                  RouteType.WELL_LIT -> AmberGlow
                  RouteType.DIRECT -> AlertCrimson
                }

                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = if (isChosen) MidnightCard else MidnightBlack.copy(alpha = 0.8f)
                  ),
                  shape = RoundedCornerShape(16.dp),
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isChosen) borderColor else MidnightBorder)
                  ),
                  modifier = Modifier
                    .width(220.dp)
                    .clickable { viewModel.selectRoute(route) }
                    .testTag("route_option_card_${route.id}")
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Surface(
                        color = when (route.type) {
                          RouteType.SAFEST -> Color(0xFF0F3D39)
                          RouteType.WELL_LIT -> Color(0xFF452B00)
                          RouteType.DIRECT -> Color(0xFF450A0A)
                        },
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Text(
                          text = route.type.badge,
                          color = borderColor,
                          fontSize = 10.sp,
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                      }

                      Text(
                        text = "${route.estimatedMinutes} min",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                      text = route.title,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = "Safety Score: ",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                      )
                      Text(
                        text = "${route.safetyScore}/100",
                        color = if (route.safetyScore >= 80) SafetyTeal else AlertCrimson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = "Illumination: ",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                      )
                      Text(
                        text = "${route.lightingPercentage}% lit",
                        color = AmberGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                      text = route.summaryNote,
                      color = Color(0xFF94A3B8),
                      fontSize = 10.sp,
                      lineHeight = 13.sp,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start Safe Navigation CTA Button
            selectedRoute?.let { route ->
              Button(
                onClick = { viewModel.startNavigation() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (route.type == RouteType.DIRECT) WarningOrange else SafetyTeal
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("start_safe_walk_button")
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsWalk,
                  contentDescription = null,
                  tint = MidnightBlack,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Start Safe Walk (${route.estimatedMinutes} min)",
                  color = MidnightBlack,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }
            }
          }
        }
      }
    }

    // SOS Countdown Dialog if initiated
    if (isSosCountingDown) {
      SosCountdownDialog(
        countdown = sosCountdown,
        onCancel = { viewModel.cancelSosCountdown() }
      )
    }
  }
}
