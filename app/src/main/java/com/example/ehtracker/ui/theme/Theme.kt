package com.example.ehtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val GreenDarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkAccentMuted,
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val GreenLightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccentMuted,
    secondary = LightOnSurfaceVariant,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val TealDarkColorScheme = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val TealLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TealOnPrimary,
    primaryContainer = TealPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val AmberDarkColorScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = AmberOnPrimaryDark,
    primaryContainer = AmberPrimaryContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val AmberLightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = AmberOnPrimary,
    primaryContainer = AmberPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val IndigoDarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    secondary = IndigoSecondaryDark,
    onSecondary = IndigoOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val IndigoLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = IndigoOnPrimary,
    primaryContainer = IndigoPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = IndigoOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val RoseDarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = RoseOnPrimaryDark,
    primaryContainer = RosePrimaryContainerDark,
    secondary = RoseSecondaryDark,
    onSecondary = RoseOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val RoseLightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = RoseOnPrimary,
    primaryContainer = RosePrimaryContainer,
    secondary = RoseSecondary,
    onSecondary = RoseOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDark,
    onPrimary = PurpleOnPrimaryDark,
    primaryContainer = PurplePrimaryContainerDark,
    secondary = PurpleSecondaryDark,
    onSecondary = PurpleOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurplePrimaryContainer,
    secondary = PurpleSecondary,
    onSecondary = PurpleOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = CoralOnPrimaryDark,
    primaryContainer = CoralPrimaryContainerDark,
    secondary = CoralSecondaryDark,
    onSecondary = CoralOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = CoralOnPrimary,
    primaryContainer = CoralPrimaryContainer,
    secondary = CoralSecondary,
    onSecondary = CoralOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val SlateDarkColorScheme = darkColorScheme(
    primary = SlatePrimaryDark,
    onPrimary = SlateOnPrimaryDark,
    primaryContainer = SlatePrimaryContainerDark,
    secondary = SlateSecondaryDark,
    onSecondary = SlateOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val SlateLightColorScheme = lightColorScheme(
    primary = SlatePrimary,
    onPrimary = SlateOnPrimary,
    primaryContainer = SlatePrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = SlateOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = BlueOnPrimaryDark,
    primaryContainer = BluePrimaryContainerDark,
    secondary = BlueSecondaryDark,
    onSecondary = BlueOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val BlueLightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    secondary = BlueSecondary,
    onSecondary = BlueOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val CyanDarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = CyanOnPrimaryDark,
    primaryContainer = CyanPrimaryContainerDark,
    secondary = CyanSecondaryDark,
    onSecondary = CyanOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val CyanLightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = CyanOnPrimary,
    primaryContainer = CyanPrimaryContainer,
    secondary = CyanSecondary,
    onSecondary = CyanOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val RedDarkColorScheme = darkColorScheme(
    primary = RedPrimaryDark,
    onPrimary = RedOnPrimaryDark,
    primaryContainer = RedPrimaryContainerDark,
    secondary = RedSecondaryDark,
    onSecondary = RedOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val RedLightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = RedPrimaryContainer,
    secondary = RedSecondary,
    onSecondary = RedOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val BrownDarkColorScheme = darkColorScheme(
    primary = BrownPrimaryDark,
    onPrimary = BrownOnPrimaryDark,
    primaryContainer = BrownPrimaryContainerDark,
    secondary = BrownSecondaryDark,
    onSecondary = BrownOnSecondaryDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White,
)

private val BrownLightColorScheme = lightColorScheme(
    primary = BrownPrimary,
    onPrimary = BrownOnPrimary,
    primaryContainer = BrownPrimaryContainer,
    secondary = BrownSecondary,
    onSecondary = BrownOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White,
)

private val SolarizedDarkColorScheme = darkColorScheme(
    primary = SolarizedPrimaryDark,
    onPrimary = SolarizedOnPrimaryDark,
    primaryContainer = SolarizedPrimaryContainerDark,
    secondary = SolarizedSecondaryDark,
    onSecondary = SolarizedOnSecondaryDark,
    background = SolarizedBgDark,
    onBackground = SolarizedTextDark,
    surface = SolarizedSurfaceDark,
    onSurface = SolarizedTextDark,
    surfaceVariant = SolarizedVariantDark,
    onSurfaceVariant = SolarizedMutedDark,
    outline = SolarizedBorderDark,
    outlineVariant = SolarizedBorderDark,
    error = SolarizedErrorDark,
    onError = SolarizedBgDark,
)

private val SolarizedLightColorScheme = lightColorScheme(
    primary = SolarizedPrimary,
    onPrimary = SolarizedOnPrimary,
    primaryContainer = SolarizedPrimaryContainer,
    secondary = SolarizedSecondary,
    onSecondary = SolarizedOnSecondary,
    background = SolarizedBgLight,
    onBackground = SolarizedTextLight,
    surface = SolarizedSurfaceLight,
    onSurface = SolarizedTextLight,
    surfaceVariant = SolarizedVariantLight,
    onSurfaceVariant = SolarizedMutedLight,
    outline = SolarizedBorderLight,
    outlineVariant = SolarizedBorderLight,
    error = SolarizedError,
    onError = SolarizedBgLight,
)

private val OneDarkDarkColorScheme = darkColorScheme(
    primary = OneDarkPrimaryDark,
    onPrimary = OneDarkOnPrimaryDark,
    primaryContainer = OneDarkPrimaryContainerDark,
    secondary = OneDarkSecondaryDark,
    onSecondary = OneDarkOnSecondaryDark,
    background = OneDarkBgDark,
    onBackground = OneDarkTextDark,
    surface = OneDarkSurfaceDark,
    onSurface = OneDarkTextDark,
    surfaceVariant = OneDarkVariantDark,
    onSurfaceVariant = OneDarkMutedDark,
    outline = OneDarkBorderDark,
    outlineVariant = OneDarkBorderDark,
    error = OneDarkErrorDark,
    onError = OneDarkTextDark,
)

private val OneDarkLightColorScheme = lightColorScheme(
    primary = OneDarkPrimary,
    onPrimary = OneDarkOnPrimary,
    primaryContainer = OneDarkPrimaryContainer,
    secondary = OneDarkSecondary,
    onSecondary = OneDarkOnSecondary,
    background = OneDarkBgLight,
    onBackground = OneDarkTextLight,
    surface = OneDarkSurfaceLight,
    onSurface = OneDarkTextLight,
    surfaceVariant = OneDarkVariantLight,
    onSurfaceVariant = OneDarkMutedLight,
    outline = OneDarkBorderLight,
    outlineVariant = OneDarkBorderLight,
    error = OneDarkError,
    onError = OneDarkTextLight,
)

private val NordDarkColorScheme = darkColorScheme(
    primary = NordPrimaryDark,
    onPrimary = NordOnPrimaryDark,
    primaryContainer = NordPrimaryContainerDark,
    secondary = NordSecondaryDark,
    onSecondary = NordOnSecondaryDark,
    background = NordBgDark,
    onBackground = NordTextDark,
    surface = NordSurfaceDark,
    onSurface = NordTextDark,
    surfaceVariant = NordVariantDark,
    onSurfaceVariant = NordMutedDark,
    outline = NordBorderDark,
    outlineVariant = NordBorderDark,
    error = NordErrorDark,
    onError = NordTextDark,
)

private val NordLightColorScheme = lightColorScheme(
    primary = NordPrimary,
    onPrimary = NordOnPrimary,
    primaryContainer = NordPrimaryContainer,
    secondary = NordSecondary,
    onSecondary = NordOnSecondary,
    background = NordBgLight,
    onBackground = NordTextLight,
    surface = NordSurfaceLight,
    onSurface = NordTextLight,
    surfaceVariant = NordVariantLight,
    onSurfaceVariant = NordMutedLight,
    outline = NordBorderLight,
    outlineVariant = NordBorderLight,
    error = NordError,
    onError = NordTextLight,
)

private val TokyoNightDarkColorScheme = darkColorScheme(
    primary = TokyoNightPrimaryDark,
    onPrimary = TokyoNightOnPrimaryDark,
    primaryContainer = TokyoNightPrimaryContainerDark,
    secondary = TokyoNightSecondaryDark,
    onSecondary = TokyoNightOnSecondaryDark,
    background = TokyoNightBgDark,
    onBackground = TokyoNightTextDark,
    surface = TokyoNightSurfaceDark,
    onSurface = TokyoNightTextDark,
    surfaceVariant = TokyoNightVariantDark,
    onSurfaceVariant = TokyoNightMutedDark,
    outline = TokyoNightBorderDark,
    outlineVariant = TokyoNightBorderDark,
    error = TokyoNightErrorDark,
    onError = TokyoNightTextDark,
)

private val TokyoNightLightColorScheme = lightColorScheme(
    primary = TokyoNightPrimary,
    onPrimary = TokyoNightOnPrimary,
    primaryContainer = TokyoNightPrimaryContainer,
    secondary = TokyoNightSecondary,
    onSecondary = TokyoNightOnSecondary,
    background = TokyoNightBgLight,
    onBackground = TokyoNightTextLight,
    surface = TokyoNightSurfaceLight,
    onSurface = TokyoNightTextLight,
    surfaceVariant = TokyoNightVariantLight,
    onSurfaceVariant = TokyoNightMutedLight,
    outline = TokyoNightBorderLight,
    outlineVariant = TokyoNightBorderLight,
    error = TokyoNightError,
    onError = TokyoNightTextLight,
)

private val GruvboxDarkColorScheme = darkColorScheme(
    primary = GruvboxPrimaryDark,
    onPrimary = GruvboxOnPrimaryDark,
    primaryContainer = GruvboxPrimaryContainerDark,
    secondary = GruvboxSecondaryDark,
    onSecondary = GruvboxOnSecondaryDark,
    background = GruvboxBgDark,
    onBackground = GruvboxTextDark,
    surface = GruvboxSurfaceDark,
    onSurface = GruvboxTextDark,
    surfaceVariant = GruvboxVariantDark,
    onSurfaceVariant = GruvboxMutedDark,
    outline = GruvboxBorderDark,
    outlineVariant = GruvboxBorderDark,
    error = GruvboxErrorDark,
    onError = GruvboxTextDark,
)

private val GruvboxLightColorScheme = lightColorScheme(
    primary = GruvboxPrimary,
    onPrimary = GruvboxOnPrimary,
    primaryContainer = GruvboxPrimaryContainer,
    secondary = GruvboxSecondary,
    onSecondary = GruvboxOnSecondary,
    background = GruvboxBgLight,
    onBackground = GruvboxTextLight,
    surface = GruvboxSurfaceLight,
    onSurface = GruvboxTextLight,
    surfaceVariant = GruvboxVariantLight,
    onSurfaceVariant = GruvboxMutedLight,
    outline = GruvboxBorderLight,
    outlineVariant = GruvboxBorderLight,
    error = GruvboxError,
    onError = GruvboxTextLight,
)

private fun parseTheme(themeMode: String): Palette {
    val paletteName = when {
        themeMode.startsWith("teal_") -> "teal"
        themeMode.startsWith("amber_") -> "amber"
        themeMode.startsWith("indigo_") -> "indigo"
        themeMode.startsWith("rose_") -> "rose"
        themeMode.startsWith("purple_") -> "purple"
        themeMode.startsWith("coral_") -> "coral"
        themeMode.startsWith("slate_") -> "slate"
        themeMode.startsWith("blue_") -> "blue"
        themeMode.startsWith("cyan_") -> "cyan"
        themeMode.startsWith("red_") -> "red"
        themeMode.startsWith("brown_") -> "brown"
        themeMode.startsWith("solarized_") -> "solarized"
        themeMode.startsWith("onedark_") -> "onedark"
        themeMode.startsWith("nord_") -> "nord"
        themeMode.startsWith("tokyonight_") -> "tokyonight"
        themeMode.startsWith("gruvbox_") -> "gruvbox"
        themeMode.startsWith("dynamic_") -> "dynamic"
        else -> "green"
    }
    val mode = themeMode.removePrefix("${paletteName}_")
    return Palette(paletteName, mode.ifEmpty { "system" })
}

private data class Palette(val name: String, val mode: String)

@Composable
fun EHTrackerTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val palette = parseTheme(themeMode)
    val isDark = when (palette.mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = when (palette.name) {
        "dynamic" -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) GreenDarkColorScheme else GreenLightColorScheme
            }
        }
        "teal" -> if (isDark) TealDarkColorScheme else TealLightColorScheme
        "amber" -> if (isDark) AmberDarkColorScheme else AmberLightColorScheme
        "indigo" -> if (isDark) IndigoDarkColorScheme else IndigoLightColorScheme
        "rose" -> if (isDark) RoseDarkColorScheme else RoseLightColorScheme
        "purple" -> if (isDark) PurpleDarkColorScheme else PurpleLightColorScheme
        "coral" -> if (isDark) CoralDarkColorScheme else CoralLightColorScheme
        "slate" -> if (isDark) SlateDarkColorScheme else SlateLightColorScheme
        "blue" -> if (isDark) BlueDarkColorScheme else BlueLightColorScheme
        "cyan" -> if (isDark) CyanDarkColorScheme else CyanLightColorScheme
        "red" -> if (isDark) RedDarkColorScheme else RedLightColorScheme
        "brown" -> if (isDark) BrownDarkColorScheme else BrownLightColorScheme
        "solarized" -> if (isDark) SolarizedDarkColorScheme else SolarizedLightColorScheme
        "onedark" -> if (isDark) OneDarkDarkColorScheme else OneDarkLightColorScheme
        "nord" -> if (isDark) NordDarkColorScheme else NordLightColorScheme
        "tokyonight" -> if (isDark) TokyoNightDarkColorScheme else TokyoNightLightColorScheme
        "gruvbox" -> if (isDark) GruvboxDarkColorScheme else GruvboxLightColorScheme
        else -> if (isDark) GreenDarkColorScheme else GreenLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
