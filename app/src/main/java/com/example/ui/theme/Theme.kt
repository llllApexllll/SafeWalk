package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode
import java.util.Calendar

// SafeWalk Night-Theme (Default & High Contrast for Night Walking)
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

// Light Daylight Theme
private val LightSafeColorScheme = lightColorScheme(
  primary = Color(0xFFD97706),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFEF3C7),
  onPrimaryContainer = Color(0xFF78350F),

  secondary = Color(0xFF0D9488),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCCFBF1),
  onSecondaryContainer = Color(0xFF115E59),

  tertiary = Color(0xFF0284C7),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFE0F2FE),
  onTertiaryContainer = Color(0xFF0369A1),

  error = Color(0xFFDC2626),
  onError = Color.White,
  errorContainer = Color(0xFFFEE2E2),
  onErrorContainer = Color(0xFF991B1B),

  background = Color(0xFFF8FAFC),
  surface = Color.White,
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.DARK,
  content: @Composable () -> Unit,
) {
  val systemIsDark = isSystemInDarkTheme()

  val isDark = when (themeMode) {
    AppThemeMode.DARK -> true
    AppThemeMode.LIGHT -> false
    AppThemeMode.SYSTEM -> systemIsDark
    AppThemeMode.AUTO -> {
      val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
      // Automatically dark after 18:00 (sunset) or before 06:00 (sunrise)
      currentHour >= 18 || currentHour < 6
    }
  }

  MaterialTheme(
    colorScheme = if (isDark) NightSafeColorScheme else LightSafeColorScheme,
    typography = Typography,
    content = content
  )
}
