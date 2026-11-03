package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// For a night-time walking safety app, high-contrast dark theme is default and optimal
private val NightSafeColorScheme = darkColorScheme(
  primary = AmberGlow,
  onPrimary = Color(0xFF1E1500),
  primaryContainer = AmberGlowContainer,
  onPrimaryContainer = Color(0xFFFFE299),

  secondary = SafetyTeal,
  onSecondary = Color(0xFF003833),
  secondaryContainer = SafetyTealContainer,
  onSecondaryContainer = Color(0xFF99F8EF),

  tertiary = ElectricBlue,
  onTertiary = Color(0xFF00354E),
  tertiaryContainer = Color(0xFF074360),
  onTertiaryContainer = Color(0xFFBAE6FD),

  error = AlertCrimson,
  onError = Color.White,
  errorContainer = AlertCrimsonBg,
  onErrorContainer = Color(0xFFFFB4AB),

  background = MidnightBlack,
  onBackground = TextPrimary,
  surface = MidnightSurface,
  onSurface = TextPrimary,
  surfaceVariant = MidnightSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = MidnightBorder,
  outlineVariant = Color(0xFF1E293B)
)

private val LightFallbackColorScheme = lightColorScheme(
  primary = Color(0xFFB45309),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFEF3C7),
  onPrimaryContainer = Color(0xFF78350F),

  secondary = Color(0xFF0D9488),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCCFBF1),
  onSecondaryContainer = Color(0xFF115E59),

  background = Color(0xFFF8FAFC),
  surface = Color.White,
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to night theme for night walking safety
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = if (darkTheme) NightSafeColorScheme else LightFallbackColorScheme,
    typography = Typography,
    content = content
  )
}
