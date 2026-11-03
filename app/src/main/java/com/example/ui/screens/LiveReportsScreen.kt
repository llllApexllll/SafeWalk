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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.data.model.ReportCategory
import com.example.data.model.SafetyReportEntity
import com.example.data.model.SeverityLevel
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
  val reports by viewModel.filteredReports.collectAsState()
  val selectedFilter by viewModel.reportFilterCategory.collectAsState()

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
          text = "Community Safety Network",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Real-time crowdsourced reports by local pedestrians and verified safe haven coordinators.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFF94A3B8),
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Summary Statistics Card
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
              Text(text = "6 Active", fontWeight = FontWeight.Bold, color = SafetyTeal, fontSize = 15.sp)
              Text(text = "Nearby Alerts", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Box(modifier = Modifier.size(1.dp, 30.dp).background(MidnightBorder))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "3 Outages", fontWeight = FontWeight.Bold, color = AmberGlow, fontSize = 15.sp)
              Text(text = "Unlit Spots", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            Box(modifier = Modifier.size(1.dp, 30.dp).background(MidnightBorder))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "4 Havens", fontWeight = FontWeight.Bold, color = SafeHavenGreen, fontSize = 15.sp)
              Text(text = "Open 24/7", color = Color(0xFF94A3B8), fontSize = 11.sp)
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

      // Reports List
      if (reports.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(24.dp)
            ) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text("No reports found for this filter", fontWeight = FontWeight.Bold, color = Color.White)
              Spacer(modifier = Modifier.height(4.dp))
              Text("This area is reported peaceful with normal lighting.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
          }
        }
      } else {
        items(reports, key = { it.id }) { report ->
          SafetyReportItemCard(
            report = report,
            onUpvote = { viewModel.upvoteReport(report.id) }
          )
        }
      }

      // Bottom padding for FAB and navbar
      item {
        Spacer(modifier = Modifier.height(88.dp))
      }
    }

    // Floating Action Button to File Report
    ExtendedFloatingActionButton(
      onClick = onOpenReportDialog,
      containerColor = AmberGlow,
      contentColor = MidnightBlack,
      shape = RoundedCornerShape(18.dp),
      icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
      text = { Text("Report Hazard", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 20.dp, end = 20.dp)
        .testTag("report_hazard_fab")
    )
  }
}

@Composable
fun SafetyReportItemCard(
  report: SafetyReportEntity,
  onUpvote: () -> Unit,
  modifier: Modifier = Modifier
) {
  val iconVector = when (report.category) {
    ReportCategory.BROKEN_LIGHTING -> Icons.Default.Lightbulb
    ReportCategory.DARK_ALLEY -> Icons.Default.FlashlightOn
    ReportCategory.SUSPICIOUS_ACTIVITY -> Icons.Default.Warning
    ReportCategory.HARASSMENT -> Icons.Default.Warning
    ReportCategory.HIGH_CRIME_SPOT -> Icons.Default.Warning
    ReportCategory.POLICE_PATROL -> Icons.Default.Shield
    ReportCategory.SAFE_HAVEN_24_7 -> Icons.Default.Shield
    ReportCategory.WELL_LIT_CORRIDOR -> Icons.Default.Lightbulb
  }

  val accentColor = when {
    report.category == ReportCategory.SAFE_HAVEN_24_7 || report.category == ReportCategory.POLICE_PATROL -> SafeHavenGreen
    report.isLightingRelated -> AmberGlow
    report.severity == SeverityLevel.HIGH -> AlertCrimson
    else -> WarningOrange
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = MidnightCard),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
    modifier = modifier
      .fillMaxWidth()
      .animateContentSize()
      .testTag("report_card_${report.id}")
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
              imageVector = iconVector,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = report.category.displayName,
              color = accentColor,
              fontWeight = FontWeight.Bold,
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

        // Severity / Time tag
        Surface(
          color = MidnightSurface,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = report.timestampFormatted,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
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

        // Upvote / Confirm Button
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
          Text(text = "Confirmed (${report.upvotes})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
