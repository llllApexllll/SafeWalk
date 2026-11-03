package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafetyTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactBottomSheet(
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var relationship by remember { mutableStateOf("") }
  var isPrimary by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MidnightSurface,
    scrimColor = Color.Black.copy(alpha = 0.65f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("add_contact_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Add Emergency Guardian",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Text(
        text = "This trusted person will receive instant SMS alerts with your real-time GPS location if SOS is triggered.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
      )

      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Full Name") },
        placeholder = { Text("e.g., Maya Miller") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("contact_name_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = SafetyTeal,
          unfocusedBorderColor = MidnightBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Phone Number") },
        placeholder = { Text("e.g., +1 (555) 012-3456") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("contact_phone_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = SafetyTeal,
          unfocusedBorderColor = MidnightBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedTextField(
        value = relationship,
        onValueChange = { relationship = it },
        label = { Text("Relationship") },
        placeholder = { Text("e.g., Sister, Roommate, Partner") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("contact_relation_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = SafetyTeal,
          unfocusedBorderColor = MidnightBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .background(MidnightCard, RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SafetyTeal, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Set as Primary Guardian", fontSize = 13.sp, color = Color.White)
        }
        Switch(
          checked = isPrimary,
          onCheckedChange = { isPrimary = it },
          colors = SwitchDefaults.colors(checkedThumbColor = SafetyTeal, checkedTrackColor = Color(0xFF0F3D39))
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            onSave(name, phone, relationship.ifBlank { "Guardian" }, isPrimary)
            onDismiss()
          }
        },
        enabled = name.isNotBlank() && phone.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = SafetyTeal,
          contentColor = MidnightBlack,
          disabledContainerColor = Color(0xFF334155),
          disabledContentColor = Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("save_contact_button")
      ) {
        Text("Save Guardian", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    }
  }
}
