package com.root3.comfortgauge.ui.theme

import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF0B1220)
val CardBg = Color(0xFF131C2E)
val TextMain = Color(0xFFE5E7EB)
val Muted = Color(0xFF8B97AD)
val TabOn = Color(0xFF1F2A40)
val DocText = Color(0xFFCBD5E1)
val LinkBlue = Color(0xFF93C5FD)
val GridLine = Color(0x24FFFFFF)
val Crosshair = Color(0x8CFFFFFF)

/** Info-chart series colors (prototype `CAT`). */
val ChartColors = listOf(Color(0xFF60A5FA), Color(0xFFF472B6), Color(0xFFFBBF24))

fun Long.toColor(): Color = Color(this)

val TempTrack = listOf(
    0f to Color(0xFF1D4ED8), 0.30f to Color(0xFF3B82F6), 0.48f to Color(0xFF93C5FD),
    0.58f to Color(0xFF16A34A), 0.66f to Color(0xFFFDE047), 0.76f to Color(0xFFF97316),
    0.88f to Color(0xFFDC2626), 1f to Color(0xFF7F1D1D),
)

val RhTrack = listOf(
    0f to Color(0xFF7A3D10), 0.22f to Color(0xFFD9A066), 0.42f to Color(0xFF16A34A),
    0.58f to Color(0xFF16A34A), 0.72f to Color(0xFFC4B5FD), 1f to Color(0xFF5B21B6),
)
