package com.example.financemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Light theme color scheme
private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF006A6B),
    secondary = Color(0xFF4A6572),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
)

// Dark theme color scheme - More distinct borders
private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF4DB8B9),
    secondary = Color(0xFFA7C0CD),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF003737),
    onSecondary = Color(0xFF1E2930),
    onTertiary = Color(0xFF492532),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF2D2D2D),
    outline = Color(0xFF404040),
)

// Theme state management
object ThemeManager {
    var isDarkTheme by mutableStateOf(false)

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }
}

@Composable
fun FinanceManagerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (ThemeManager.isDarkTheme) DarkColorScheme else LightColorScheme

    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}