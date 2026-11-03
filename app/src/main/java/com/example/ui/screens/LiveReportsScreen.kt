package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FirestoreCommunityReport
import com.example.data.model.ReportCategory
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel
import com.example.data.model.VerificationStatus
import com.example.ui.theme.AlertCrimson
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
fun LiveReportsScreen(
  viewModel: SafeWalkViewModel,
  onOpenReportDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cloudReports by viewModel.cloudReports.collectAsState()
  val localReports by viewModel.localReports.collectAsState()
  val selectedFilter by viewModel.reportFilterCategory.collectAsState()

  // Prioritize cloud reports if present, fallback to local reports
  val displayCloudReports = if (selectedFilter == null) {
    cloudReports
  } else {
    cloudReports.filter { it.category == selectedFilter?.name }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .testTag("live_reports_screen")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Header item
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Crowdsourced Safety Network",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Anonymous, geo-tagged hazard reports reviewed for accuracy before updating algorithmic safe routes.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8),
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Accuracy Review Pipeline Card
        Card(
          colors = CardDefaults.cardColors(containerColor = MidnightSurface),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Cloud Sync", fontWeight = FontWeight.Bold, color = SafetyTeal, fontSize = 14.sp)
              Text(text = "Firestore Live", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Box(modifier = Modifier.size(1.dp, 30.dp).background(MidnightBorder))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Accuracy Review", fontWeight = FontWeight.Bold, color = AmberGlow, fontSize = 14.sp)
              Text(text = "3+ Confirmations", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Box(modifier = Modifier.size(1.dp, 30.dp).background(MidnightBorder))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Route Impact", fontWeight = FontWeight.Bold, color = SafeHavenGreen, fontSize = 14.sp)
              Text(text = "Auto-Detour", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
          FilterChip(
            selected = selectedFilter == null,
            onClick = { viewModel.setFilterCategory(null) },
            label = { Text("All Reports", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = SafetyTeal,
              selectedLabelColor = MidnightBlack,
              containerColor = MidnightCard,
              labelColor = Color(0xFFCBD5E1)
            )
          )
          ReportCategory.entries.forEach { cat ->
            FilterChip(
              selected = selectedFilter == cat,
              onClick = { viewModel.setFilterCategory(cat) },
              label = { Text(cat.displayName, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = AmberGlow,
                selectedLabelColor = MidnightBlack,
                containerColor = MidnightCard,
                labelColor = Color(0xFFCBD5E1)
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
      }

      // Display Cloud Reports if available, else local
      if (displayCloudReports.isNotEmpty()) {
        items(displayCloudReports, key = { it.id }) { report ->
          CloudSafetyReportCard(
            report = report,
            onUpvote = { viewModel.upvoteReport(report) }
          )
        }
      } else {
        items(localReports, key = { it.id }) { localReport ->
          LocalSafetyReportCard(
            report = localReport,
            onUpvote = { viewModel.upvoteLocalReport(localReport.id) }
          )
        }
      }

      // Bottom padding for FAB and navbar
      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    // Floating Action Button to File Anonymous Report
    ExtendedFloatingActionButton(
      onClick = onOpenReportDialog,
      containerColor = AmberGlow,
      contentColor = MidnightBlack,
      shape = RoundedCornerShape(18.dp),
      icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
      text = { Text("Report Safety Hazard", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 20.dp, end = 20.dp)
        .testTag("report_hazard_fab")
    )
  }
}

@Composable
fun CloudSafetyReportCard(
  report: FirestoreCommunityReport,
  onUpvote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLighting = report.category.contains("LIGHTING") || report.category.contains("DARK")
  val isCritical = report.severity == SeverityLevel.CRITICAL.name || report.severity == SeverityLevel.HIGH.name

  val accentColor = when {
    report.category.contains("SAFE_HAVEN") -> SafeHavenGreen
    isLighting -> AmberGlow
    isCritical -> AlertCrimson
    else -> WarningOrange
  }

  val verificationLabel = when (report.verificationStatus) {
    VerificationStatus.VERIFIED.name -> "Verified Accuracy"
    VerificationStatus.COMMUNITY_CONFIRMED.name -> "Community Confirmed"
    else -> "Pending Review"
  }

  val verificationColor = when (report.verificationStatus) {
    VerificationStatus.VERIFIED.name -> SafeHavenGreen
    VerificationStatus.COMMUNITY_CONFIRMED.name -> SafetyTeal
    else -> AmberGlow
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MidnightCard),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize()
      .testTag("cloud_report_card_${report.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
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
              .background(accentColor.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = if (isLighting) Icons.Default.Lightbulb else Icons.Default.Warning,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (report.isAnonymous) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = report.authorName,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            }
            Text(
              text = report.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        // Verification Status Badge
        Surface(
          color = verificationColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = verificationLabel,
            color = verificationColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = report.description,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFE2E8F0),
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = report.addressName,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
          )
        }

        // Upvote / Accuracy Confirmation Button
        OutlinedButton(
          onClick = onUpvote,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SafetyTeal,
            containerColor = MidnightSurface
          ),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("upvote_button_${report.id}")
        ) {
          Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Confirm (+${report.upvotes})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun LocalSafetyReportCard(
  report: SafetyReportEntity,
  onUpvote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = when {
    report.category == ReportCategory.SAFE_HAVEN_24_7 -> SafeHavenGreen
    report.isLightingRelated -> AmberGlow
    report.severity == SeverityLevel.HIGH || report.severity == SeverityLevel.CRITICAL -> AlertCrimson
    else -> WarningOrange
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MidnightCard),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
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
              .background(accentColor.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = if (report.isLightingRelated) Icons.Default.Lightbulb else Icons.Default.Warning,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = if (report.isAnonymous) "Anonymous Pedestrian" else "Community Member",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
            Text(
              text = report.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Surface(
          color = SafetyTeal.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Reviewed",
            color = SafetyTeal,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = report.description,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFFE2E8F0),
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = report.addressName, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onUpvote,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = SafetyTeal, containerColor = MidnightSurface),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Confirm (${report.upvotes})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
