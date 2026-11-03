package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.EmergencyContactEntity
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonBg
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.SafetyTeal

@Composable
fun SosCountdownDialog(
  countdown: Int,
  onCancel: () -> Unit
) {
  Dialog(
    onDismissRequest = onCancel,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = AlertCrimsonBg),
      shape = RoundedCornerShape(28.dp),
      border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AlertCrimson)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("sos_countdown_dialog")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = "Alert",
          tint = AlertCrimson,
          modifier = Modifier.size(54.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "ACTIVATING EMERGENCY SOS",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFCA5A5),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Broadcasting GPS coordinates to trusted guardians, sounding alarm, and activating strobe.",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFE2E8F0),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(AlertCrimson)
        ) {
          Text(
            text = "$countdown",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onCancel,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("cancel_sos_button")
        ) {
          Text(
            text = "CANCEL SOS (I AM SAFE)",
            color = AlertCrimson,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    }
  }
}

@Composable
fun SosActiveOverlay(
  isSirenActive: Boolean,
  isStrobeActive: Boolean,
  contacts: List<EmergencyContactEntity>,
  onToggleSiren: () -> Unit,
  onToggleStrobe: () -> Unit,
  onCall911: () -> Unit,
  onDispatchSms: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = AlertCrimsonBg),
    shape = RoundedCornerShape(20.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AlertCrimson)),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("sos_active_overlay")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(12.dp)
              .clip(CircleShape)
              .background(AlertCrimson)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "EMERGENCY PROTOCOL ACTIVE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }

        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Dismiss SOS",
          tint = Color(0xFFFCA5A5),
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable { onDismiss() }
            .padding(4.dp)
            .testTag("dismiss_sos_button")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Siren Toggle
        OutlinedButton(
          onClick = onToggleSiren,
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSirenActive) AlertCrimson else MidnightCard,
            contentColor = Color.White
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSirenActive) Color.White else AlertCrimson)
          ),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("toggle_siren_button")
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = if (isSirenActive) "Siren ON" else "Siren OFF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Strobe Toggle
        OutlinedButton(
          onClick = onToggleStrobe,
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isStrobeActive) AmberGlow else MidnightCard,
            contentColor = if (isStrobeActive) MidnightBlack else Color.White
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AmberGlow)
          ),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("toggle_strobe_button")
        ) {
          Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = if (isStrobeActive) "Strobe ON" else "Strobe OFF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Button(
          onClick = onCall911,
          colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("call_911_direct_button")
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Call 911", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onDispatchSms,
          colors = ButtonDefaults.buttonColors(containerColor = SafetyTeal),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("dispatch_sms_button")
        ) {
          Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Alert Guardians", color = MidnightBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
