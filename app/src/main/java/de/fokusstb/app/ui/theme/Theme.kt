package de.fokusstb.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val AppColorScheme = lightColorScheme(
    primary = Tokens.accent,
    onPrimary = Tokens.bg,
    secondary = Tokens.accent2,
    background = Tokens.bg,
    surface = Tokens.surface,
    onBackground = Tokens.text,
    onSurface = Tokens.text,
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(fontFamily = Figtree, fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Figtree, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Figtree, fontSize = 12.sp),
)

@Composable
fun FokusStBTheme(content: @Composable () -> Unit) {
    // The prototype is a single fixed warm palette — no system dark mode variant was designed,
    // so we intentionally ignore system dark mode and always render the light palette.
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
