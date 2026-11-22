package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BrandOceanBlue,
    onPrimary = Color.White,
    primaryContainer = BrandOceanBlueDark,
    onPrimaryContainer = BrandOceanContainer,
    secondary = BrandEmerald,
    onSecondary = Color(0xFF042105),
    secondaryContainer = BrandEmeraldDark,
    onSecondaryContainer = BrandEmeraldContainer,
    tertiary = BrandSunsetOrange,
    onTertiary = Color.White,
    tertiaryContainer = BrandSunsetOrangeDark,
    onTertiaryContainer = BrandSunsetContainer,
    background = DeepTealNight,
    onBackground = Color(0xFFEAF4F7),
    surface = DeepTealSurface,
    onSurface = Color(0xFFEAF4F7),
    surfaceVariant = DeepTealCard,
    onSurfaceVariant = Color(0xFFB5CDD4),
    outline = Color(0xFF2B5765)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOceanBlue,
    onPrimary = Color.White,
    primaryContainer = BrandOceanContainer,
    onPrimaryContainer = DeepTealNight,
    secondary = BrandEmerald,
    onSecondary = Color.White,
    secondaryContainer = BrandEmeraldContainer,
    onSecondaryContainer = Color(0xFF0A380C),
    tertiary = BrandSunsetOrange,
    onTertiary = Color.White,
    tertiaryContainer = BrandSunsetContainer,
    onTertiaryContainer = Color(0xFF4A1E04),
    background = AlabasterBg,
    onBackground = InkSlate,
    surface = PureWhite,
    onSurface = InkSlate,
    surfaceVariant = SoftMist,
    onSurfaceVariant = MutedSlate,
    outline = SubtleOutline
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TourExpertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
