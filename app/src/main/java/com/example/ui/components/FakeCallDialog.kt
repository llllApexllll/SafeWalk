package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import kotlinx.coroutines.delay

@Composable
fun FakeCallDialog(
  callerName: String,
  onDismiss: () -> Unit
) {
  var isCallAnswered by remember { mutableStateOf(false) }
  var callDurationSeconds by remember { mutableIntStateOf(0) }

  LaunchedEffect(isCallAnswered) {
    if (isCallAnswered) {
      while (true) {
        delay(1000)
        callDurationSeconds++
      }
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MidnightBlack)
        .testTag("fake_call_screen")
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxSize()
          .padding(vertical = 54.dp, horizontal = 24.dp)
      ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(96.dp)
              .clip(CircleShape)
              .background(MidnightSurface)
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(54.dp)
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = callerName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = if (isCallAnswered) {
              val mins = callDurationSeconds / 60
              val secs = callDurationSeconds % 60
              String.format("%02d:%02d", mins, secs)
            } else {
              "Incoming Call..."
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (isCallAnswered) SafeHavenGreen else Color(0xFF94A3B8)
          )
        }

        // Active Speaking Script Prompt (Gives user natural phrases to repeat out loud!)
        if (isCallAnswered) {
          Card(
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "💬 Caller Audio Simulation",
                fontWeight = FontWeight.Bold,
                color = SafeHavenGreen,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "\"Hey! I'm standing right outside Beacon Blvd near the 24/7 store waiting for you! I see you walking this way, meet you in 30 seconds!\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                lineHeight = 22.sp
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Tip: Speak loudly so anyone following hears that someone is waiting right around the corner.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
              )
            }
          }
        } else {
          Text(
            text = "Deter pursuers by answering. The screen displays helpful audio cues for you to speak aloud.",
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
          )
        }

        // Actions
        if (!isCallAnswered) {
          Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
          ) {
            // Decline Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              FloatingActionButton(
                onClick = onDismiss,
                containerColor = AlertCrimson,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                  .size(72.dp)
                  .testTag("decline_fake_call_button")
              ) {
                Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Decline", modifier = Modifier.size(32.dp))
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = "Decline", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }

            // Accept Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              FloatingActionButton(
                onClick = { isCallAnswered = true },
                containerColor = SafeHavenGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                  .size(72.dp)
                  .testTag("accept_fake_call_button")
              ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = "Accept", modifier = Modifier.size(32.dp))
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = "Answer", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
          }
        } else {
          // In-call End Button
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingActionButton(
              onClick = onDismiss,
              containerColor = AlertCrimson,
              contentColor = Color.White,
              shape = CircleShape,
              modifier = Modifier
                .size(72.dp)
                .testTag("end_fake_call_button")
            ) {
              Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "End Call", color = Color(0xFF94A3B8), fontSize = 14.sp)
          }
        }
      }
    }
  }
}
