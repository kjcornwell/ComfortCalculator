package com.root3.comfortgauge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import com.root3.comfortgauge.model.GaugeConfig
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.TNUM
import com.root3.comfortgauge.ui.theme.TextMain
import com.root3.comfortgauge.ui.theme.toColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val CX = 100f
private const val CY = 100f
private const val R = 76f

/** Half-circle gauge drawn in the prototype's SVG viewBox (200×118 big, 200×112 mini). */
@Composable
fun ArcGauge(
    cfg: GaugeConfig,
    valueF: Double,
    unit: TempUnit,
    big: Boolean,
    modifier: Modifier = Modifier,
    emoji: String? = null,
) {
    val measurer = rememberTextMeasurer()
    val frac by animateFloatAsState(cfg.frac(valueF).toFloat(), tween(250), label = "marker")
    val dotColor = cfg.zoneOf(valueF).color.toColor()
    val valueText = Units.format(valueF, unit)
    val ticks = if (big) cfg.boundaryTicks else listOf(cfg.min, cfg.max)
    val vbH = if (big) 118f else 112f
    val sw = if (big) 18f else 16f

    Canvas(modifier) {
        val s = min(size.width / 200f, size.height / vbH)
        val ox = (size.width - 200f * s) / 2
        val oy = (size.height - vbH * s) / 2
        fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)
        fun at(f: Float, r: Float): Offset {
            val a = PI * (1 + f)
            return p(CX + r * cos(a).toFloat(), CY + r * sin(a).toFloat())
        }
        fun units(v: Float): TextUnit = (v * s).toSp()

        var lo = cfg.min
        for (z in cfg.zones) {
            val hi = min(z.to, cfg.max)
            if (hi <= lo) continue
            val f0 = cfg.frac(lo).toFloat()
            val f1 = cfg.frac(hi).toFloat()
            drawArc(
                color = z.color.toColor(),
                startAngle = 180f + f0 * 180f,
                sweepAngle = (f1 - f0) * 180f,
                useCenter = false,
                topLeft = p(CX - R, CY - R),
                size = Size(2 * R * s, 2 * R * s),
                style = Stroke(sw * s),
            )
            lo = hi
        }
        val tickStyle = TextStyle(color = Muted, fontSize = units(if (big) 7f else 8f), fontFeatureSettings = TNUM)
        for (t in ticks) {
            val c = at(cfg.frac(t).toFloat(), R + sw / 2 + 7)
            drawCentered(measurer, Units.format(t, unit, bare = true), c, tickStyle)
        }
        val dot = at(frac, R)
        val dr = (if (big) 9f else 7f) * s
        drawCircle(dotColor, dr, dot)
        drawCircle(Bg, dr, dot, style = Stroke(2.5f * s))
        if (big) {
            emoji?.let { drawBaseline(measurer, it, p(CX, 62f), TextStyle(fontSize = units(30f))) }
            drawBaseline(measurer, "FEELS LIKE", p(CX, 113f), TextStyle(color = Muted, fontSize = units(9f)))
        }
        drawBaseline(
            measurer, valueText, p(CX, 100f),
            TextStyle(color = TextMain, fontSize = units(if (big) 26f else 17f), fontWeight = FontWeight.Bold, fontFeatureSettings = TNUM),
        )
    }
}

/** SVG text-anchor="middle" dominant-baseline="middle". */
internal fun DrawScope.drawCentered(m: TextMeasurer, text: String, c: Offset, style: TextStyle) {
    val l = m.measure(text, style)
    drawText(l, topLeft = Offset(c.x - l.size.width / 2f, c.y - l.size.height / 2f))
}

/** SVG text-anchor="middle" with the default alphabetic baseline at [c].y. */
internal fun DrawScope.drawBaseline(m: TextMeasurer, text: String, c: Offset, style: TextStyle) {
    val l = m.measure(text, style)
    drawText(l, topLeft = Offset(c.x - l.size.width / 2f, c.y - l.firstBaseline))
}
