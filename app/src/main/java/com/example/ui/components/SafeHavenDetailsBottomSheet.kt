package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SafeHaven
import com.example.data.model.SafeHavenType
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeHavenDetailsBottomSheet(
  haven: SafeHaven,
  isOnActiveRoute: Boolean,
  onDismiss: () -> Unit,
  onCallHaven: (String) -> Unit,
  onNavigateToHaven: (SafeHaven) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val icon = when (haven.type) {
    SafeHavenType.POLICE_STATION -> Icons.Default.Shield
    SafeHavenType.PHARMACY_24_7 -> Icons.Default.LocalPharmacy
    SafeHavenType.CONVENIENCE_STORE_24_7 -> Icons.Default.Storefront
    SafeHavenType.CAMPUS_SECURITY -> Icons.Default.Security
    SafeHavenType.HOSPITAL_ER -> Icons.Default.LocalHospital
    SafeHavenType.FIRE_STATION -> Icons.Default.Shield
  }

  val typeLabel = when (haven.type) {
    SafeHavenType.POLICE_STATION -> "Police Station / Precinct"
    SafeHavenType.PHARMACY_24_7 -> "24/7 Pharmacy & Guard"
    SafeHavenType.CONVENIENCE_STORE_24_7 -> "24/7 Convenience Store"
    SafeHavenType.CAMPUS_SECURITY -> "Campus Security / Blue Light"
    SafeHavenType.HOSPITAL_ER -> "Hospital 24/7 Emergency Room"
    SafeHavenType.FIRE_STATION -> "Fire Station"
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    scrimColor = Color.Black.copy(alpha = 0.65f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("safe_haven_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(SafeHavenGreen.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = SafeHavenGreen,
              modifier = Modifier.size(26.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = typeLabel,
              color = SafeHavenGreen,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = haven.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Surface(
          color = SafeHavenGreen.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "Open 24/7",
            color = SafeHavenGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = haven.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Route proximity status
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(12.dp)
        ) {
          Icon(
            imageVector = if (isOnActiveRoute) Icons.Default.CheckCircle else Icons.Default.DirectionsWalk,
            contentDescription = null,
            tint = if (isOnActiveRoute) SafeHavenGreen else SafetyTeal,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isOnActiveRoute) {
              "Directly Along Your Calculated Walking Path (0 min detour). Ideal safe stopover."
            } else {
              "Nearby Safe Haven: 1-2 min detour from current walking route."
            },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            lineHeight = 17.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Security & Protection Amenities",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      HavenAmenityRow(icon = Icons.Default.Security, text = "Staff & Security Guard on Duty")
      HavenAmenityRow(icon = Icons.Default.Lightbulb, text = "Continuous Exterior & Interior High-Illumination")
      HavenAmenityRow(icon = Icons.Default.Videocam, text = "Monitored CCTV Surveillance")
      HavenAmenityRow(icon = Icons.Default.Call, text = "Emergency Assistance & Phone Call Safe Zone")

      Spacer(modifier = Modifier.height(18.dp))

      // Action Buttons
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (haven.phone.isNotBlank()) {
          OutlinedButton(
            onClick = { onCallHaven(haven.phone) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("call_safe_haven_button")
          ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call Haven", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }

        Button(
          onClick = {
            onNavigateToHaven(haven)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = SafeHavenGreen),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("route_to_safe_haven_button")
        ) {
          Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Walk Here", color = MidnightBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun HavenAmenityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(vertical = 3.dp)
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = SafeHavenGreen, modifier = Modifier.size(15.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
  }
}
