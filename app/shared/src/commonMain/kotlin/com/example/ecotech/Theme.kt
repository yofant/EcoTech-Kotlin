package com.example.ecotech

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EcoColors = darkColorScheme(
    primary = Color(0xFF2EE68A),
    onPrimary = Color(0xFF040706),
    primaryContainer = Color(0xFF123021),
    onPrimaryContainer = Color(0xFF7EF9C0),
    secondary = Color(0xFF12C46E),
    onSecondary = Color(0xFF040706),
    secondaryContainer = Color(0xFF123021),
    onSecondaryContainer = Color(0xFF7EF9C0),
    tertiary = Color(0xFF7EF9C0),
    onTertiary = Color(0xFF040706),
    background = Color(0xFF070B0A),
    onBackground = Color(0xFFE9F3EF),
    surface = Color(0xFF101C16),
    onSurface = Color(0xFFE9F3EF),
    surfaceVariant = Color(0xFF16251D),
    onSurfaceVariant = Color(0xFF9DB0AA),
    outline = Color(0xFF315342),
    outlineVariant = Color(0xFF233A2D),
    error = Color(0xFFFF7D7D),
    onError = Color(0xFF240707),
)

private val EcoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.7).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
)

private val EcoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun EcoTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EcoColors,
        typography = EcoTypography,
        shapes = EcoShapes,
        content = content,
    )
}

// Deep Black to Neon Emerald Glow Gradient
fun ecoGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070B0A),
            Color(0xFF0B1711),
            Color(0xFF09110D),
            Color(0xFF070B0A),
        ),
    )
}

fun ecoCardGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(0xF016251D),
            Color(0xE6101C16),
        ),
    )
}
