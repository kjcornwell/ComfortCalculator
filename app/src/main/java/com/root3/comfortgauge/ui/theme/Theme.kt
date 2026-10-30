package com.root3.comfortgauge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val Scheme = darkColorScheme(
    primary = TextMain,
    onPrimary = Bg,
    background = Bg,
    onBackground = TextMain,
    surface = CardBg,
    onSurface = TextMain,
    surfaceVariant = TabOn,
    onSurfaceVariant = Muted,
    outline = Muted,
)

/** Tabular numerals for every readout. */
const val TNUM = "tnum"

val NumStyle = TextStyle(fontFeatureSettings = TNUM)

/** Dark only; dynamic color is deliberately not used. */
@Composable
fun ComfortTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
