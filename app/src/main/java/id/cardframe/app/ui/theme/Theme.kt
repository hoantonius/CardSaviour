package id.cardframe.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import id.cardframe.app.domain.theme.AuroraPalette
import id.cardframe.app.domain.theme.AuroraThemeEngine
import id.cardframe.app.domain.theme.AuroraThemeId

val LocalAuroraPalette = staticCompositionLocalOf { AuroraThemeEngine.palette(AuroraThemeId.Ink, dark = false) }

@Composable
fun auroraPalette(): AuroraPalette = LocalAuroraPalette.current

@Composable
fun AuroraTheme(
    theme: AuroraThemeId,
    dark: Boolean,
    content: @Composable () -> Unit,
) {
    val palette = AuroraThemeEngine.palette(theme, dark)
    val scheme = if (dark) {
        darkColorScheme(
            primary = Color(palette.primary),
            onPrimary = Color(palette.onPrimary),
            secondary = Color(palette.accent),
            onSecondary = Color(palette.onAccent),
            background = Color(palette.background),
            onBackground = Color(palette.ink),
            surface = Color(palette.surface),
            onSurface = Color(palette.ink),
            surfaceVariant = Color(palette.surfaceHigh),
            onSurfaceVariant = Color(palette.muted),
            outline = Color(palette.line),
            error = Color(palette.danger),
            onError = Color(palette.onCaution),
        )
    } else {
        lightColorScheme(
            primary = Color(palette.primary),
            onPrimary = Color(palette.onPrimary),
            secondary = Color(palette.accent),
            onSecondary = Color(palette.onAccent),
            background = Color(palette.background),
            onBackground = Color(palette.ink),
            surface = Color(palette.surface),
            onSurface = Color(palette.ink),
            surfaceVariant = Color(palette.surfaceHigh),
            onSurfaceVariant = Color(palette.muted),
            outline = Color(palette.line),
            error = Color(palette.danger),
            onError = Color(palette.surface),
        )
    }
    CompositionLocalProvider(LocalAuroraPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AuroraTypography,
            content = content,
        )
    }
}
