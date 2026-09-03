package com.vibe.pdfscan.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.vibe.pdfscan.data.ThemeManager

@Composable
fun VibePDFScanTheme(
    themeManager: ThemeManager? = null,
    preset: ThemeColorPreset = ThemeColorPreset.BLUE_INDIGO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val activePreset = themeManager?.selectedPreset ?: preset
    val useDynamicColor = themeManager?.isDynamicColorEnabled ?: dynamicColor

    val isDark = if (themeManager != null) {
        when (themeManager.displayMode) {
            ThemeDisplayMode.SYSTEM -> isSystemInDarkTheme()
            ThemeDisplayMode.LIGHT -> false
            ThemeDisplayMode.DARK -> true
        }
    } else {
        darkTheme
    }

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> createDarkColorScheme(activePreset)
        else -> createLightColorScheme(activePreset)
    }

    val appGradient = AppGradient(
        preset = activePreset,
        brush = activePreset.createBrush(),
        isGradientActive = activePreset.isGradient
    )

    CompositionLocalProvider(
        LocalAppGradient provides appGradient
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
