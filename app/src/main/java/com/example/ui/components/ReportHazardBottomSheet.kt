package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportCategory
import com.example.data.model.SeverityLevel
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafetyTeal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportHazardBottomSheet(
  onDismiss: () -> Unit,
  onSubmit: (title: String, description: String, category: ReportCategory, severity: SeverityLevel, isLighting: Boolean) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedCategory by remember { mutableStateOf(ReportCategory.BROKEN_LIGHTING) }
  var selectedSeverity by remember { mutableStateOf(SeverityLevel.HIGH) }
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var isLightingRelated by remember { mutableStateOf(true) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MidnightSurface,
    scrimColor = Color.Black.copy(alpha = 0.65f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("report_hazard_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Report Community Safety Hazard",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }

      Text(
        text = "Your real-time report will immediately alert other pedestrians and update safe route calculations.",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
      )

      // Category Selection Chips
      Text(text = "Category", style = MaterialTheme.typography.labelMedium, color = Color(0xFFCBD5E1))
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        ReportCategory.entries.forEach { category ->
          FilterChip(
            selected = selectedCategory == category,
            onClick = {
              selectedCategory = category
              if (category == ReportCategory.BROKEN_LIGHTING || category == ReportCategory.DARK_ALLEY) {
                isLightingRelated = true
              }
            },
            label = { Text(category.displayName, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AmberGlow,
              selectedLabelColor = MidnightBlack,
              containerColor = MidnightCard,
              labelColor = Color(0xFFE2E8F0)
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Streetlamp Outage Toggle
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .background(MidnightCard, RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AmberGlow, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Streetlight / Illumination Issue", fontSize = 13.sp, color = Color.White)
        }
        Switch(
          checked = isLightingRelated,
          onCheckedChange = { isLightingRelated = it },
          colors = SwitchDefaults.colors(checkedThumbColor = AmberGlow, checkedTrackColor = Color(0xFF78350F))
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Title Input
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title") },
        placeholder = { Text("e.g., Pitch black streetlamps on Elm St") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("report_title_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AmberGlow,
          unfocusedBorderColor = MidnightBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Description Input
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Details & Guidance") },
        placeholder = { Text("Explain exactly what you saw so others can stay safe...") },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("report_desc_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AmberGlow,
          unfocusedBorderColor = MidnightBorder,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        minLines = 2,
        maxLines = 4
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Submit Button
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSubmit(
              title,
              description.ifBlank { "Reported by nearby pedestrian." },
              selectedCategory,
              selectedSeverity,
              isLightingRelated
            )
            onDismiss()
          }
        },
        enabled = title.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
          containerColor = AmberGlow,
          contentColor = MidnightBlack,
          disabledContainerColor = Color(0xFF334155),
          disabledContentColor = Color(0xFF64748B)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("submit_report_button")
      ) {
        Text("Publish Community Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    }
  }
}
