package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.AppThemeMode
import com.example.data.model.FirestoreFavoriteRoute
import com.example.data.model.FirestoreUserProfile
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.SafeHavenGreen
import com.example.ui.theme.SafetyTeal
import com.example.ui.viewmodel.SafeWalkViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun ProfileScreen(
  viewModel: SafeWalkViewModel,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val userProfile by viewModel.userProfile.collectAsState()
  val currentThemeMode by viewModel.themeMode.collectAsState()
  val favoriteRoutes by viewModel.favoriteRoutes.collectAsState()
  val myReportedAlerts by viewModel.myReportedAlerts.collectAsState()

  val currentUser = Firebase.auth.currentUser

  var displayName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: (currentUser?.displayName ?: "SafeWalk User")) }
  var phoneNumber by remember(userProfile) { mutableStateOf(userProfile?.phoneNumber ?: "") }
  var bloodType by remember(userProfile) { mutableStateOf(userProfile?.bloodType ?: "O+") }
  var physicalCapacity by remember(userProfile) { mutableStateOf(userProfile?.physicalCapacity ?: "Level 1 (Full Mobility)") }
  var chronicIllnesses by remember(userProfile) { mutableStateOf(userProfile?.chronicIllnesses ?: "None declared") }
  var medications by remember(userProfile) { mutableStateOf(userProfile?.medications ?: "None declared") }
  var allergies by remember(userProfile) { mutableStateOf(userProfile?.allergies ?: "No known allergies") }
  var emergencyNotes by remember(userProfile) { mutableStateOf(userProfile?.emergencyNotes ?: "") }

  var isEditingMedical by remember { mutableStateOf(false) }
  var showSaveSuccess by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp)
      .testTag("profile_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Google Account Header Card
    item {
      Spacer(modifier = Modifier.height(14.dp))
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
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
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(30.dp)
                )
              }

              Spacer(modifier = Modifier.width(14.dp))

              Column {
                Text(
                  text = displayName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = currentUser?.email ?: "Google Account Connected",
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 12.sp
                )
                Surface(
                  color = SafeHavenGreen.copy(alpha = 0.15f),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.padding(top = 4.dp)
                ) {
                  Text(
                    text = "Google Account Active",
                    color = SafeHavenGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            IconButton(
              onClick = {
                signOutUser(
                  context = context,
                  credentialManager = credentialManager,
                  onSignOutComplete = onSignOut,
                  scope = coroutineScope
                )
              },
              modifier = Modifier.testTag("sign_out_button")
            ) {
              Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Sign Out",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    }

    // 2. App Theme Selector (Light, Dark, System, Auto)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "App Theme Mode",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Text(
            text = "Choose your preferred viewing comfort for nighttime and daylight navigation.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          AppThemeMode.entries.forEach { mode ->
            val isSelected = currentThemeMode == mode
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.setThemeMode(mode) }
                .padding(vertical = 6.dp, horizontal = 4.dp)
                .testTag("theme_radio_${mode.name}")
            ) {
              RadioButton(
                selected = isSelected,
                onClick = { viewModel.setThemeMode(mode) },
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = mode.displayName,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 14.sp
                )
                Text(
                  text = mode.subtitle,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
              }
            }
          }
        }
      }
    }

    // 3. Customizable Medical Information & Physical Capacity (Phcap)
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                tint = AlertCrimson,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Emergency Medical Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            OutlinedButton(
              onClick = {
                if (isEditingMedical) {
                  viewModel.saveProfile(
                    displayName = displayName,
                    phoneNumber = phoneNumber,
                    bloodType = bloodType,
                    physicalCapacity = physicalCapacity,
                    chronicIllnesses = chronicIllnesses,
                    medications = medications,
                    allergies = allergies,
                    emergencyNotes = emergencyNotes
                  )
                  showSaveSuccess = true
                }
                isEditingMedical = !isEditingMedical
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("toggle_edit_medical_button")
            ) {
              Icon(
                imageVector = if (isEditingMedical) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (isEditingMedical) "Save" else "Edit", fontSize = 12.sp)
            }
          }

          Text(
            text = "Stored securely in your cloud profile and attached to emergency SOS broadcasts for paramedics and guardians.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          if (isEditingMedical) {
            // Edit Fields
            OutlinedTextField(
              value = physicalCapacity,
              onValueChange = { physicalCapacity = it },
              label = { Text("Physical Capacity (Phcap) / Mobility") },
              placeholder = { Text("e.g., Level 1 (Full Mobility), Uses Cane, Reduced Pace") },
              modifier = Modifier.fillMaxWidth().testTag("input_phcap"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = bloodType,
              onValueChange = { bloodType = it },
              label = { Text("Blood Type") },
              placeholder = { Text("e.g., O+, A-, B+") },
              modifier = Modifier.fillMaxWidth().testTag("input_blood_type"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = chronicIllnesses,
              onValueChange = { chronicIllnesses = it },
              label = { Text("Chronic Illnesses & Conditions") },
              placeholder = { Text("e.g., Asthma, Type 1 Diabetes, Heart Condition") },
              modifier = Modifier.fillMaxWidth().testTag("input_illnesses"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = medications,
              onValueChange = { medications = it },
              label = { Text("Active Medications") },
              placeholder = { Text("e.g., Albuterol Inhaler (carry in pocket), Insulin") },
              modifier = Modifier.fillMaxWidth().testTag("input_medications"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = allergies,
              onValueChange = { allergies = it },
              label = { Text("Known Allergies") },
              placeholder = { Text("e.g., Penicillin, Latex, Severe Bee Venom") },
              modifier = Modifier.fillMaxWidth().testTag("input_allergies"),
              singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = emergencyNotes,
              onValueChange = { emergencyNotes = it },
              label = { Text("Emergency Responder Instructions") },
              placeholder = { Text("e.g., Inhaler is in outer backpack pocket") },
              modifier = Modifier.fillMaxWidth().testTag("input_emergency_notes"),
              minLines = 2
            )
          } else {
            // Read-Only Medical Overview Cards
            MedicalDataRow(label = "Physical Capacity (Phcap)", value = physicalCapacity)
            MedicalDataRow(label = "Blood Type", value = bloodType)
            MedicalDataRow(label = "Illnesses / Conditions", value = chronicIllnesses)
            MedicalDataRow(label = "Medications", value = medications)
            MedicalDataRow(label = "Allergies", value = allergies)
            if (emergencyNotes.isNotBlank()) {
              MedicalDataRow(label = "Emergency Instructions", value = emergencyNotes)
            }
          }

          if (showSaveSuccess) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "✓ Medical profile synchronized with cloud database.",
              color = SafeHavenGreen,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 4. Favorite Routes Section
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = AmberGlow,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Favorite Safe Routes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            OutlinedButton(
              onClick = { viewModel.saveCurrentRouteAsFavorite("Saved Safe Corridor") },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("save_favorite_route_button")
            ) {
              Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save Current", fontSize = 11.sp)
            }
          }

          Text(
            text = "Quickly navigate along your vetted, high-illumination routes.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          if (favoriteRoutes.isEmpty()) {
            Text(
              text = "No saved favorite routes yet. Tap 'Save Current' to pin your favorite corridor.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          } else {
            favoriteRoutes.forEach { favRoute ->
              FavoriteRouteItemCard(
                favRoute = favRoute,
                onDelete = { viewModel.deleteFavoriteRoute(favRoute.id) }
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }

    // 5. Previously Reported Alerts History
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = SafetyTeal,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Previously Reported Alerts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Text(
            text = "Track the community accuracy review status of your past safety reports.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          if (myReportedAlerts.isEmpty()) {
            Text(
              text = "You haven't filed any safety reports yet. When you report a hazard, its verification progress appears here.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          } else {
            myReportedAlerts.forEach { rep ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                  .padding(10.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = rep.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${rep.category} • ${rep.addressName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                  )
                }

                Surface(
                  color = if (rep.isVerified) SafeHavenGreen.copy(alpha = 0.2f) else AmberGlow.copy(alpha = 0.2f),
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(
                    text = rep.verificationStatus,
                    color = if (rep.isVerified) SafeHavenGreen else AmberGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(36.dp))
    }
  }
}

@Composable
fun MedicalDataRow(label: String, value: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp)
  ) {
    Text(
      text = label,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
    Text(
      text = value,
      color = MaterialTheme.colorScheme.onSurface,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun FavoriteRouteItemCard(
  favRoute: FirestoreFavoriteRoute,
  onDelete: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = favRoute.title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "To: ${favRoute.destinationName} • Safety: ${favRoute.safetyScore}/100",
        color = SafetyTeal,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
    }

    IconButton(
      onClick = onDelete,
      modifier = Modifier.size(32.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete Route",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
