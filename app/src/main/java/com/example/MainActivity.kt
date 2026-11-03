package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddContactBottomSheet
import com.example.ui.components.FakeCallDialog
import com.example.ui.components.ReportHazardBottomSheet
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.LiveReportsScreen
import com.example.ui.screens.SafeMapScreen
import com.example.ui.screens.SosToolkitScreen
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MidnightBorder
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SafetyTeal
import com.example.ui.viewmodel.SafeWalkViewModel

enum class SafeWalkTab(val label: String) {
  MAP("Safe Map"),
  REPORTS("Live Alerts"),
  SOS("SOS Toolkit"),
  GUARDIANS("Guardians")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        SafeWalkApp()
      }
    }
  }
}

@Composable
fun SafeWalkApp(viewModel: SafeWalkViewModel = viewModel()) {
  val context = LocalContext.current
  var currentTab by remember { mutableStateOf(SafeWalkTab.MAP) }

  var showReportHazardSheet by remember { mutableStateOf(false) }
  var showAddContactSheet by remember { mutableStateOf(false) }

  val isFakeCallActive by viewModel.isFakeCallActive.collectAsState()
  val fakeCallerName by viewModel.fakeCallerName.collectAsState()

  // Gracefully request Camera permission for flashlight strobe mode and location
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { /* Handled gracefully */ }

  LaunchedEffect(Unit) {
    val neededPermissions = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      neededPermissions.add(Manifest.permission.CAMERA)
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (neededPermissions.isNotEmpty()) {
      permissionLauncher.launch(neededPermissions.toTypedArray())
    }
  }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MidnightSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
          .navigationBarsPadding()
          .testTag("main_bottom_nav_bar")
      ) {
        // Tab 1: Safe Route Map
        NavigationBarItem(
          selected = currentTab == SafeWalkTab.MAP,
          onClick = { currentTab = SafeWalkTab.MAP },
          icon = {
            Icon(
              imageVector = if (currentTab == SafeWalkTab.MAP) Icons.Filled.Map else Icons.Outlined.Map,
              contentDescription = "Safe Map",
              modifier = Modifier.size(24.dp)
            )
          },
          label = { Text("Safe Map", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MidnightBlack,
            selectedTextColor = SafetyTeal,
            indicatorColor = SafetyTeal,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          ),
          modifier = Modifier.testTag("tab_safe_map")
        )

        // Tab 2: Live Reports Feed
        NavigationBarItem(
          selected = currentTab == SafeWalkTab.REPORTS,
          onClick = { currentTab = SafeWalkTab.REPORTS },
          icon = {
            Icon(
              imageVector = if (currentTab == SafeWalkTab.REPORTS) Icons.Filled.Shield else Icons.Outlined.Shield,
              contentDescription = "Live Alerts",
              modifier = Modifier.size(24.dp)
            )
          },
          label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MidnightBlack,
            selectedTextColor = AmberGlow,
            indicatorColor = AmberGlow,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          ),
          modifier = Modifier.testTag("tab_live_alerts")
        )

        // Tab 3: SOS Hub
        NavigationBarItem(
          selected = currentTab == SafeWalkTab.SOS,
          onClick = { currentTab = SafeWalkTab.SOS },
          icon = {
            Icon(
              imageVector = if (currentTab == SafeWalkTab.SOS) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsActive,
              contentDescription = "SOS Hub",
              modifier = Modifier.size(24.dp)
            )
          },
          label = { Text("SOS Toolkit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = AlertCrimson,
            indicatorColor = AlertCrimson,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          ),
          modifier = Modifier.testTag("tab_sos_toolkit")
        )

        // Tab 4: Guardians & Contacts
        NavigationBarItem(
          selected = currentTab == SafeWalkTab.GUARDIANS,
          onClick = { currentTab = SafeWalkTab.GUARDIANS },
          icon = {
            Icon(
              imageVector = if (currentTab == SafeWalkTab.GUARDIANS) Icons.Filled.Group else Icons.Outlined.Group,
              contentDescription = "Guardians",
              modifier = Modifier.size(24.dp)
            )
          },
          label = { Text("Guardians", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MidnightBlack,
            selectedTextColor = SafetyTeal,
            indicatorColor = SafetyTeal,
            unselectedIconColor = Color(0xFF94A3B8),
            unselectedTextColor = Color(0xFF94A3B8)
          ),
          modifier = Modifier.testTag("tab_guardians")
        )
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MidnightBlack)
    ) {
      when (currentTab) {
        SafeWalkTab.MAP -> {
          SafeMapScreen(
            viewModel = viewModel,
            onOpenReportDialog = { showReportHazardSheet = true }
          )
        }
        SafeWalkTab.REPORTS -> {
          LiveReportsScreen(
            viewModel = viewModel,
            onOpenReportDialog = { showReportHazardSheet = true }
          )
        }
        SafeWalkTab.SOS -> {
          SosToolkitScreen(
            viewModel = viewModel
          )
        }
        SafeWalkTab.GUARDIANS -> {
          ContactsScreen(
            viewModel = viewModel,
            onOpenAddContactDialog = { showAddContactSheet = true }
          )
        }
      }

      // Modals & Sheets
      if (showReportHazardSheet) {
        ReportHazardBottomSheet(
          onDismiss = { showReportHazardSheet = false },
          onSubmit = { title, desc, cat, sev, isLit ->
            viewModel.submitReport(title, desc, cat, sev, isLit)
          }
        )
      }

      if (showAddContactSheet) {
        AddContactBottomSheet(
          onDismiss = { showAddContactSheet = false },
          onSave = { name, phone, rel, isPri ->
            viewModel.addContact(name, phone, rel, isPri)
          }
        )
      }

      // Fake Call Deterrence Screen Overlay
      if (isFakeCallActive) {
        FakeCallDialog(
          callerName = fakeCallerName,
          onDismiss = { viewModel.dismissFakeCall() }
        )
      }
    }
  }
}
