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

private val LightColorScheme = lightColorScheme(
    primary = LagoonTeal,
    onPrimary = Color.White,
    primaryContainer = LagoonTealContainer,
    onPrimaryContainer = OnLagoonTealContainer,
    secondary = TerracottaCoral,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = SunlitGold,
    onTertiary = Color.White,
    tertiaryContainer = SunlitGoldContainer,
    onTertiaryContainer = OnSunlitGoldContainer,
    background = WarmSandBackground,
    onBackground = TayabasEspresso,
    surface = CardWhite,
    onSurface = TayabasEspresso,
    surfaceVariant = WarmSandSurfaceVariant,
    onSurfaceVariant = CoastalSlate,
    outline = SandOutline,
    outlineVariant = Color(0xFFE6DFD5)
)

private val DarkColorScheme = darkColorScheme(
    primary = LagoonTealLight,
    onPrimary = Color(0xFF042D2D),
    primaryContainer = Color(0xFF104949),
    onPrimaryContainer = LagoonTealContainer,
    secondary = TerracottaCoralLight,
    onSecondary = Color(0xFF3E1305),
    secondaryContainer = Color(0xFF6B2B14),
    onSecondaryContainer = TerracottaContainer,
    tertiary = SunlitGold,
    onTertiary = Color(0xFF302003),
    tertiaryContainer = Color(0xFF533909),
    onTertiaryContainer = SunlitGoldContainer,
    background = DeepCoveBackground,
    onBackground = FoamWhite,
    surface = DeepCoveSurface,
    onSurface = FoamWhite,
    surfaceVariant = DeepCoveSurfaceVariant,
    onSurfaceVariant = MistGray,
    outline = DeepOutline,
    outlineVariant = Color(0xFF25373C)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BatangasConnectTheme(
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
