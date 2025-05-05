package com.example.projectwork.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FuturisticDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = DeepSpace,
    primaryContainer = NeonBlue.copy(alpha = 0.7f),
    onPrimaryContainer = StarWhite,
    
    secondary = CosmicPurple,
    onSecondary = StarWhite,
    secondaryContainer = CosmicPurple.copy(alpha = 0.7f),
    onSecondaryContainer = StarWhite,
    
    tertiary = NeonPink,
    onTertiary = StarWhite,
    tertiaryContainer = NeonPink.copy(alpha = 0.7f),
    onTertiaryContainer = StarWhite,
    
    background = DeepSpace,
    onBackground = StarWhite,
    
    surface = DarkMatter,
    onSurface = StarWhite,
    surfaceVariant = DarkMatter.copy(alpha = 0.7f),
    onSurfaceVariant = StarWhite.copy(alpha = 0.7f),
    
    error = NeonPink,
    onError = StarWhite,
    errorContainer = NeonPink.copy(alpha = 0.7f),
    onErrorContainer = StarWhite,
    
    outline = GlowingTeal
)

private val FuturisticLightColorScheme = lightColorScheme(
    primary = LightBlue,
    onPrimary = PaleWhite,
    primaryContainer = LightBlue.copy(alpha = 0.7f),
    onPrimaryContainer = DarkIndigo,
    
    secondary = TechPurple,
    onSecondary = PaleWhite,
    secondaryContainer = TechPurple.copy(alpha = 0.7f),
    onSecondaryContainer = DarkIndigo,
    
    tertiary = ElectricPink,
    onTertiary = PaleWhite,
    tertiaryContainer = ElectricPink.copy(alpha = 0.7f),
    onTertiaryContainer = DarkIndigo,
    
    background = SilverGray,
    onBackground = DarkIndigo,
    
    surface = PaleWhite,
    onSurface = DarkIndigo,
    surfaceVariant = PaleWhite.copy(alpha = 0.7f),
    onSurfaceVariant = DarkIndigo.copy(alpha = 0.7f),
    
    error = ElectricPink,
    onError = PaleWhite,
    errorContainer = ElectricPink.copy(alpha = 0.7f),
    onErrorContainer = DarkIndigo,
    
    outline = EnergyCyan
)

@Composable
fun ProjectWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to force our futuristic theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FuturisticDarkColorScheme
        else -> FuturisticLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}