package com.vishaltelangre.nerdcalci.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MidnightDarkColorScheme = darkColorScheme(
    primary = MidPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = MidPrimaryContainerDark,
    onPrimaryContainer = MidPrimaryDark,
    secondary = MidSecondaryDark,
    secondaryContainer = MidSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = MidSecondaryDark,
    tertiary = MidTertiaryDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1B1B1B),
    surfaceVariant = Color(0xFF252525),
    surfaceContainer = Color(0xFF2B2B2B),
    surfaceContainerHigh = Color(0xFF353535),
    surfaceContainerHighest = Color(0xFF404040),
    surfaceContainerLow = Color(0xFF181818),
    surfaceContainerLowest = Color(0xFF0F0F0F),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = MidOutlineDark,
    outlineVariant = MidOutlineVariantDark
)

private val MidnightLightColorScheme = lightColorScheme(
    primary = MidPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = MidPrimaryContainerLight,
    onPrimaryContainer = MidPrimaryLight,
    secondary = MidSecondaryLight,
    secondaryContainer = MidSecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = MidSecondaryLight,
    tertiary = MidTertiaryLight,
    background = Color(0xFFFFFBFE),
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    surfaceContainer = Color(0xFFF0F0F0),
    surfaceContainerHigh = Color(0xFFEBEBEB),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    surfaceContainerLow = Color(0xFFF7F7F7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    outline = MidOutlineLight,
    outlineVariant = MidOutlineVariantLight
)

private val SolarFlareDarkColorScheme = darkColorScheme(
    primary = SolarPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = SolarPrimaryContainerDark,
    onPrimaryContainer = SolarPrimaryDark,
    secondary = SolarSecondaryDark,
    secondaryContainer = SolarSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = SolarSecondaryDark,
    tertiary = SolarTertiaryDark,
    background = Color(0xFF1A1614),
    surface = Color(0xFF241E1B),
    surfaceVariant = Color(0xFF2D2622),
    surfaceContainer = Color(0xFF252525),
    surfaceContainerHigh = Color(0xFF2D2D2D),
    surfaceContainerHighest = Color(0xFF352D28),
    surfaceContainerLow = Color(0xFF1F1A17),
    surfaceContainerLowest = Color(0xFF15110F),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = SolarOutlineDark,
    outlineVariant = SolarOutlineVariantDark
)

private val SolarFlareLightColorScheme = lightColorScheme(
    primary = SolarPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SolarPrimaryContainerLight,
    onPrimaryContainer = SolarPrimaryLight,
    secondary = SolarSecondaryLight,
    secondaryContainer = SolarSecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = SolarSecondaryLight,
    tertiary = SolarTertiaryLight,
    background = Color(0xFFFFF9F2),
    surface = Color(0xFFFFFAF5),
    surfaceVariant = Color(0xFFFDF2E8),
    surfaceContainer = Color(0xFFFFF1E6),
    surfaceContainerHigh = Color(0xFFFFE6D1),
    surfaceContainerHighest = Color(0xFFF5E1D0),
    surfaceContainerLow = Color(0xFFFFF7EF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    outline = SolarOutlineLight,
    outlineVariant = SolarOutlineVariantLight
)

private val ArcticFrostDarkColorScheme = darkColorScheme(
    primary = ArcticPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = ArcticPrimaryContainerDark,
    onPrimaryContainer = ArcticPrimaryDark,
    secondary = ArcticSecondaryDark,
    secondaryContainer = ArcticSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = ArcticSecondaryDark,
    tertiary = ArcticTertiaryDark,
    background = Color(0xFF0E1415),
    surface = Color(0xFF151D1F),
    surfaceVariant = Color(0xFF1C2729),
    surfaceContainer = Color(0xFF1B2628),
    surfaceContainerHigh = Color(0xFF233134),
    surfaceContainerHighest = Color(0xFF2C3E42),
    surfaceContainerLow = Color(0xFF111719),
    surfaceContainerLowest = Color(0xFF0C1011),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = ArcticOutlineDark,
    outlineVariant = ArcticOutlineVariantDark
)

private val ArcticFrostLightColorScheme = lightColorScheme(
    primary = ArcticPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = ArcticPrimaryContainerLight,
    onPrimaryContainer = ArcticPrimaryLight,
    secondary = ArcticSecondaryLight,
    secondaryContainer = ArcticSecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = ArcticSecondaryLight,
    tertiary = ArcticTertiaryLight,
    background = Color(0xFFF4FBFC),
    surface = Color(0xFFF9FDFF),
    surfaceVariant = Color(0xFFEBF7F9),
    surfaceContainer = Color(0xFFE0F7FA),
    surfaceContainerHigh = Color(0xFFB2EBF2),
    surfaceContainerHighest = Color(0xFFD1F2F7),
    surfaceContainerLow = Color(0xFFF2FBFC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    outline = ArcticOutlineLight,
    outlineVariant = ArcticOutlineVariantLight
)

private val NaturesBreathDarkColorScheme = darkColorScheme(
    primary = NaturePrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = NaturePrimaryContainerDark,
    onPrimaryContainer = NaturePrimaryDark,
    secondary = NatureSecondaryDark,
    secondaryContainer = NatureSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = NatureSecondaryDark,
    tertiary = NatureTertiaryDark,
    background = Color(0xFF0E150E),
    surface = Color(0xFF151F15),
    surfaceVariant = Color(0xFF1C271C),
    surfaceContainer = Color(0xFF1D291D),
    surfaceContainerHigh = Color(0xFF263526),
    surfaceContainerHighest = Color(0xFF2F402F),
    surfaceContainerLow = Color(0xFF121B12),
    surfaceContainerLowest = Color(0xFF0C140C),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = NatureOutlineDark,
    outlineVariant = NatureOutlineVariantDark
)

private val NaturesBreathLightColorScheme = lightColorScheme(
    primary = NaturePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = NaturePrimaryContainerLight,
    onPrimaryContainer = NaturePrimaryLight,
    secondary = NatureSecondaryLight,
    secondaryContainer = NatureSecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = NatureSecondaryLight,
    tertiary = NatureTertiaryLight,
    background = Color(0xFFF5F9F5),
    surface = Color(0xFFFAFCFA),
    surfaceVariant = Color(0xFFECF3EC),
    surfaceContainer = Color(0xFFF1F8E9),
    surfaceContainerHigh = Color(0xFFDCEDC8),
    surfaceContainerHighest = Color(0xFFC5E1A5),
    surfaceContainerLow = Color(0xFFF7FBEF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    outline = NatureOutlineLight,
    outlineVariant = NatureOutlineVariantLight
)

private val RoyalVelvetDarkColorScheme = darkColorScheme(
    primary = RoyalPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = RoyalPrimaryContainerDark,
    onPrimaryContainer = RoyalPrimaryDark,
    secondary = RoyalSecondaryDark,
    secondaryContainer = RoyalSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = RoyalSecondaryDark,
    tertiary = RoyalTertiaryDark,
    background = Color(0xFF150E0F),
    surface = Color(0xFF1F1517),
    surfaceVariant = Color(0xFF291C1E),
    surfaceContainer = Color(0xFF2D1F1F),
    surfaceContainerHigh = Color(0xFF3D2525),
    surfaceContainerHighest = Color(0xFF4D2F2F),
    surfaceContainerLow = Color(0xFF1A1213),
    surfaceContainerLowest = Color(0xFF120C0D),
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = RoyalOutlineDark,
    outlineVariant = RoyalOutlineVariantDark
)

private val RoyalVelvetLightColorScheme = lightColorScheme(
    primary = RoyalPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = RoyalPrimaryContainerLight,
    onPrimaryContainer = RoyalPrimaryLight,
    secondary = RoyalSecondaryLight,
    secondaryContainer = RoyalSecondaryLight.copy(alpha = 0.1f),
    onSecondaryContainer = RoyalSecondaryLight,
    tertiary = RoyalTertiaryLight,
    background = Color(0xFFFFF5F7),
    surface = Color(0xFFFFFAFB),
    surfaceVariant = Color(0xFFFBECEF),
    surfaceContainer = Color(0xFFFFF0F3),
    surfaceContainerHigh = Color(0xFFFFE1E6),
    surfaceContainerHighest = Color(0xFFFFD1D9),
    surfaceContainerLow = Color(0xFFFFF2F4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    outline = RoyalOutlineLight,
    outlineVariant = RoyalOutlineVariantLight
)

@Composable
fun NerdCalciTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPalette: String = "midnight",
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        when (colorPalette) {
            "solar" -> SolarFlareDarkColorScheme
            "arctic" -> ArcticFrostDarkColorScheme
            "nature" -> NaturesBreathDarkColorScheme
            "royal" -> RoyalVelvetDarkColorScheme
            else -> MidnightDarkColorScheme
        }
    } else {
        when (colorPalette) {
            "solar" -> SolarFlareLightColorScheme
            "arctic" -> ArcticFrostLightColorScheme
            "nature" -> NaturesBreathLightColorScheme
            "royal" -> RoyalVelvetLightColorScheme
            else -> MidnightLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
