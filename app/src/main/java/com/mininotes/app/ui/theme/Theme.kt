package com.mininotes.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    Dracula, Grey, Beige, Mauve, Peach, Light, MaterialYou
}

private val DraculaScheme = darkColorScheme(
    primary = DraculaPurple,
    secondary = DraculaPink,
    background = DraculaBackground,
    surface = DraculaBackground,
    surfaceVariant = DraculaCurrentLine,
    onBackground = DraculaForeground,
    onSurface = DraculaForeground,
    onSurfaceVariant = DraculaForeground,
    outline = DraculaComment
)

private val GreyScheme = darkColorScheme(
    primary = GreyPrimary,
    secondary = GreyAccent,
    background = GreyBackground,
    surface = GreyBackground,
    surfaceVariant = GreySurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.LightGray
)

private val BeigeScheme = lightColorScheme(
    primary = BeigePrimary,
    secondary = BeigeAccent,
    background = BeigeBackground,
    surface = BeigeBackground,
    surfaceVariant = BeigeSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.DarkGray
)

private val MauveScheme = lightColorScheme(
    primary = MauvePrimary,
    secondary = MauveAccent,
    background = MauveBackground,
    surface = MauveBackground,
    surfaceVariant = MauveSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black
)

private val PeachScheme = lightColorScheme(
    primary = PeachPrimary,
    secondary = PeachAccent,
    background = PeachBackground,
    surface = PeachBackground,
    surfaceVariant = PeachSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.DarkGray
)

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightAccent,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurface,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.DarkGray
)

@Composable
fun MiniNotesTheme(
    appTheme: AppTheme = AppTheme.Dracula,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = when (appTheme) {
        AppTheme.Dracula -> DraculaScheme
        AppTheme.Grey -> GreyScheme
        AppTheme.Beige -> BeigeScheme
        AppTheme.Mauve -> MauveScheme
        AppTheme.Peach -> PeachScheme
        AppTheme.Light -> LightScheme
        AppTheme.MaterialYou -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isSystemDark) GreyScheme else LightScheme
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
            
            val isDark = when (appTheme) {
                AppTheme.Dracula, AppTheme.Grey -> true
                AppTheme.MaterialYou -> isSystemDark
                else -> false // Beige, Mauve, Peach, Light are light themes
            }
            
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
