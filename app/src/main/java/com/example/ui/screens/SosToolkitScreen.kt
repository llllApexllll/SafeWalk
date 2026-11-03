package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonBg
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal
import com.example.ui.viewmodel.SafeWalkViewModel

@Composable
fun SosToolkitScreen(
  viewModel: SafeWalkViewModel,
  modifier: Modifier = Modifier
) {
  val isSirenActive by viewModel.toolkit.isSirenActive.collectAsState()
  val isStrobeActive by viewModel.toolkit.isStrobeActive.collectAsState()
  val isArrivalTimerActive by viewModel.isArrivalTimerActive.collectAsState()
  val arrivalSecondsRemaining by viewModel.arrivalTimerSecondsRemaining.collectAsState()
  val contacts by viewModel.allContacts.collectAsState()

  // Ambient pulse on SOS beacon
  val infiniteTransition = rememberInfiniteTransition(label = "sos_button_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "sos_scale"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .padding(horizontal = 16.dp)
      .testTag("sos_toolkit_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Guardian Emergency Hub",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = "Immediate security countermeasures specifically engineered for walking alone after dark.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(top = 4.dp)
      )
    }

    // 1. Central Hero SOS Button Card
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = AlertCrimsonBg),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AlertCrimson)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sos_hero_card")
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Text(
            text = "ONE-TOUCH EMERGENCY SOS",
            color = Color(0xFFFCA5A5),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Big Pulsing Red SOS Button
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(130.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(AlertCrimson, Color(0xFFB91C1C))
                )
              )
              .border(4.dp, Color(0xFFFCA5A5).copy(alpha = 0.8f), CircleShape)
              .clickable { viewModel.startSosHoldCountdown() }
              .testTag("central_sos_button")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
              )
              Text(
                text = "SOS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Tap to begin 3-second countdown. Sounds alarm, strobes flashlight, and broadcasts GPS coordinates to your ${contacts.size} guardians.",
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    // 2. Immediate Deterrence Countermeasures (Fake Call, Siren, Strobe)
    item {
      Text(
        text = "Immediate Deterrence Tools",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    // Fake Call Deterrence
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.triggerFakeCall("Mom (Mobile)") }
          .testTag("trigger_fake_call_card")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(16.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(SafeHavenGreen.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = Icons.Default.PhoneInTalk,
              contentDescription = null,
              tint = SafeHavenGreen,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Simulate Fake Incoming Call",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
            Text(
              text = "Deter people following you with a realistic phone call and simulated speaker cues.",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          Button(
            onClick = { viewModel.triggerFakeCall("Mom (Mobile)") },
            colors = ButtonDefaults.buttonColors(containerColor = SafeHavenGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("call_me_now_button")
          ) {
            Text("Call Me", color = MidnightBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // Loud Siren Alarm Toggle
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AlertCrimson.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = AlertCrimson,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Loud Siren Alarm (Piercing)",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
              )
              Text(
                text = "High-decibel alternating frequency to alert surrounding people.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
              )
            }
          }

          Switch(
            checked = isSirenActive,
            onCheckedChange = { viewModel.toolkit.toggleSiren() },
            colors = SwitchDefaults.colors(checkedThumbColor = AlertCrimson, checkedTrackColor = Color(0xFF7F1D1D)),
            modifier = Modifier.testTag("siren_switch")
          )
        }
      }
    }

    // Strobe Flashlight Toggle
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.padding(16.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AmberGlow.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = AmberGlow,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Disorientation Strobe Light",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
              )
              Text(
                text = "Rapid torch strobing (11 Hz) to disorient or attract attention.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
              )
            }
          }

          Switch(
            checked = isStrobeActive,
            onCheckedChange = { viewModel.toolkit.toggleStrobe() },
            colors = SwitchDefaults.colors(checkedThumbColor = AmberGlow, checkedTrackColor = Color(0xFF78350F)),
            modifier = Modifier.testTag("strobe_switch")
          )
        }
      }
    }

    // 3. Safe Arrival Walk Timer
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SafetyTeal.copy(alpha = 0.2f))
            ) {
              Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = null,
                tint = SafetyTeal,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Safe Arrival Walk Timer",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
              )
              Text(
                text = if (isArrivalTimerActive) {
                  val mins = arrivalSecondsRemaining / 60
                  val secs = arrivalSecondsRemaining % 60
                  "Active: ${String.format("%02d:%02d", mins, secs)} remaining"
                } else {
                  "Automatically alerts guardians if you do not check in."
                },
                color = if (isArrivalTimerActive) SafetyTeal else Color(0xFF94A3B8),
                fontWeight = if (isArrivalTimerActive) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (isArrivalTimerActive) {
            Button(
              onClick = { viewModel.stopArrivalTimer() },
              colors = ButtonDefaults.buttonColors(containerColor = SafeHavenGreen),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("check_in_safe_button")
            ) {
              Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("I Checked In / Arrived Safely", color = MidnightBlack, fontWeight = FontWeight.Bold)
            }
          } else {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Button(
                onClick = { viewModel.startArrivalTimer(10) },
                colors = ButtonDefaults.buttonColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("timer_10m_btn")
              ) {
                Text("10 Min", fontSize = 12.sp, color = Color.White)
              }
              Button(
                onClick = { viewModel.startArrivalTimer(15) },
                colors = ButtonDefaults.buttonColors(containerColor = SafetyTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("timer_15m_btn")
              ) {
                Text("15 Min", fontSize = 12.sp, color = MidnightBlack, fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = { viewModel.startArrivalTimer(25) },
                colors = ButtonDefaults.buttonColors(containerColor = MidnightSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("timer_25m_btn")
              ) {
                Text("25 Min", fontSize = 12.sp, color = Color.White)
              }
            }
          }
        }
      }
    }

    // Direct 911 / Police Contact Card
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Button(
          onClick = { viewModel.toolkit.dialEmergencyNumber("911") },
          colors = ButtonDefaults.buttonColors(containerColor = AlertCrimson),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("direct_dial_911_button")
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Call 911 Direct", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Button(
          onClick = { viewModel.activateSosProtocol() },
          colors = ButtonDefaults.buttonColors(containerColor = SafetyTeal),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("broadcast_sos_sms_button")
        ) {
          Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Alert Guardians", color = MidnightBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
