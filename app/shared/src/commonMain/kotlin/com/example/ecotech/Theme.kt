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

// Color Palette - Pure Black & Neon Emerald Green Theme
private val EcoColors = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF052E16),
    onPrimaryContainer = Color(0xFF00FF66),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D140E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF162218),
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0xFF1F3827),
    outlineVariant = Color(0xFF122418),
    error = Color(0xFFFF5252),
    onError = Color(0xFF000000)
)

private val EcoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
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
@Composable
fun ecoGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000),
            Color(0xFF05140B),
            Color(0xFF0B2816),
            Color(0xFF041009),
            Color(0xFF000000),
        ),
    )
}

// Glassmorphic Card Background
@Composable
fun ecoCardGradient(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D1810),
            Color(0xFF08100A),
        )
    )
}
