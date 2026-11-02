package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

// ============================================================================
// SMARTLAB Material 3 Color Schemes (Medical Blue & Clean White)
// ============================================================================

private val LightColorScheme = lightColorScheme(
    primary = LabBluePrimary,
    onPrimary = LabPureWhite,
    primaryContainer = LabBlueContainerLight,
    onPrimaryContainer = LabOnBlueContainerLight,
    secondary = LabCyanAccent,
    onSecondary = LabPureWhite,
    secondaryContainer = LabCyanContainer,
    onSecondaryContainer = LabOnCyanContainer,
    tertiary = LabTealTertiary,
    onTertiary = LabPureWhite,
    tertiaryContainer = LabTealContainer,
    onTertiaryContainer = LabOnTealContainer,
    background = LabSurfaceLight,
    onBackground = LabTextPrimary,
    surface = LabPureWhite,
    onSurface = LabTextPrimary,
    surfaceVariant = LabSurfaceVariantLight,
    onSurfaceVariant = LabTextSecondary,
    surfaceContainerLowest = LabPureWhite,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = LabBorderLight,
    outlineVariant = LabOutlineVariantLight,
    error = StatusDanger,
    onError = LabPureWhite,
    errorContainer = StatusDangerBg,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = LabBluePrimaryDark,
    onPrimary = LabBlueDark,
    primaryContainer = LabBlueContainerDark,
    onPrimaryContainer = LabOnBlueContainerDark,
    secondary = LabCyanSecondaryDark,
    onSecondary = Color(0xFF002F35),
    secondaryContainer = LabCyanContainerDark,
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = Color(0xFF4DB6AC),
    onTertiary = Color(0xFF00332C),
    tertiaryContainer = Color(0xFF004D40),
    onTertiaryContainer = Color(0xFFB2DFDB),
    background = LabDarkBg,
    onBackground = LabDarkTextPrimary,
    surface = LabDarkSurface,
    onSurface = LabDarkTextPrimary,
    surfaceVariant = LabDarkCard,
    onSurfaceVariant = LabDarkTextSecondary,
    surfaceContainerLowest = Color(0xFF070D17),
    surfaceContainerLow = LabDarkBg,
    surfaceContainer = LabDarkSurface,
    surfaceContainerHigh = LabDarkCard,
    surfaceContainerHighest = Color(0xFF1E324F),
    outline = LabDarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

/**
 * الثيم الرسمي لنظام SMARTLAB Management
 * يطبق الهوية البصرية (الأزرق الطبي والأبيض النقي)، الخطوط العربية (Cairo & Tajawal)،
 * ويدعم اتجاه الواجهة العربية من اليمين إلى اليسار (RTL) والوضعين النهاري والليلي.
 */
@Composable
fun SmartLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    enforceRtl: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val layoutDirection = if (enforceRtl) LayoutDirection.Rtl else LocalLayoutDirection.current

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = SmartLabShapes,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    SmartLabTheme(darkTheme = darkTheme, content = content)
}
