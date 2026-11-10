package dev.neardrop.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Palette = darkColorScheme(
    primary = Color(0xFF0A84FF), onPrimary = Color.White,
    secondary = Color(0xFF64D2FF), background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E), surfaceVariant = Color(0xFF2C2C2E),
    onBackground = Color(0xFFF5F5F7), onSurface = Color(0xFFF5F5F7),
    onSurfaceVariant = Color(0xFF98989F), tertiary = Color(0xFFFF9F0A),
    outline = Color(0xFF38383D), error = Color(0xFFFF453A)
)

@Composable fun NearDropTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = Palette, content = content) }
