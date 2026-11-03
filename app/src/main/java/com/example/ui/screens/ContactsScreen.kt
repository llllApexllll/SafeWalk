package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyContactEntity
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal
import com.example.ui.viewmodel.SafeWalkViewModel

@Composable
fun ContactsScreen(
  viewModel: SafeWalkViewModel,
  onOpenAddContactDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val contacts by viewModel.allContacts.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MidnightBlack)
      .padding(horizontal = 16.dp)
      .testTag("contacts_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Text(
            text = "Emergency Guardians",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Text(
            text = "Trusted contacts alerted when you trigger SOS or miss your check-in.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onOpenAddContactDialog,
        colors = ButtonDefaults.buttonColors(containerColor = SafetyTeal),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("add_guardian_button")
      ) {
        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = MidnightBlack, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add New Guardian", color = MidnightBlack, fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }

    if (contacts.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MidnightCard),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Guardians Added Yet", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Add family, friends, or roommates so they receive your live location during emergencies.", color = Color(0xFF94A3B8), fontSize = 12.sp)
          }
        }
      }
    } else {
      items(contacts, key = { it.id }) { contact ->
        ContactItemCard(
          contact = contact,
          onDelete = { viewModel.deleteContact(contact) },
          onTestCall = { viewModel.toolkit.dialEmergencyNumber(contact.phoneNumber) },
          onTestSms = {
            viewModel.toolkit.sendSosSmsIntent(
              phoneNumbers = listOf(contact.phoneNumber),
              currentAddress = "Beacon Blvd near Grand Ave",
              lat = viewModel.userLat.value,
              lng = viewModel.userLng.value
            )
          }
        )
      }
    }

    // Night Walking Safety Checklist Card
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Essential Night Safety Tips",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = MidnightCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          SafetyTipRow(
            icon = Icons.Default.Lightbulb,
            color = AmberGlow,
            title = "Stick to Illuminated Corridors",
            desc = "Always choose the Safest or Well-Lit route over dark shortcuts, even if it adds 2 to 3 minutes."
          )
          SafetyTipRow(
            icon = Icons.Default.Security,
            color = SafeHavenGreen,
            title = "Locate 24/7 Safe Havens",
            desc = "If you feel followed, step immediately inside an open 24/7 pharmacy, store, or police booth."
          )
          SafetyTipRow(
            icon = Icons.Default.Call,
            color = SafetyTeal,
            title = "Use Fake Call Deterrence",
            desc = "Simulate an incoming phone call and speak loudly to let anyone nearby know someone expects you in seconds."
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(36.dp))
    }
  }
}

@Composable
fun ContactItemCard(
  contact: EmergencyContactEntity,
  onDelete: () -> Unit,
  onTestCall: () -> Unit,
  onTestSms: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MidnightCard),
    shape = RoundedCornerShape(16.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("contact_card_${contact.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(if (contact.isPrimary) AmberGlow.copy(alpha = 0.2f) else SafetyTeal.copy(alpha = 0.2f))
          ) {
            Icon(
              imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.Person,
              contentDescription = null,
              tint = if (contact.isPrimary) AmberGlow else SafetyTeal,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (contact.isPrimary) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  color = AmberGlow.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = "PRIMARY",
                    color = AmberGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            Text(
              text = "${contact.relationship} • ${contact.phoneNumber}",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp
            )
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.testTag("delete_contact_${contact.id}")
        ) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF64748B))
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedButton(
          onClick = onTestCall,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).testTag("call_guardian_${contact.id}")
        ) {
          Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Call", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = onTestSms,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MidnightBorder)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).testTag("sms_guardian_${contact.id}")
        ) {
          Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Test Alert", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun SafetyTipRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  color: Color,
  title: String,
  desc: String
) {
  Row(verticalAlignment = Alignment.Top) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.15f))
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
      Text(text = desc, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 16.sp)
    }
  }
}
