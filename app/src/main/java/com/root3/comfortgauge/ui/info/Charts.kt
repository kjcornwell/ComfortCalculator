package com.root3.comfortgauge.ui.info

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.ChartColors
import com.root3.comfortgauge.ui.theme.DocText
import com.root3.comfortgauge.ui.theme.Muted

data class Series(val name: String, val values: List<Double>)

private const val W = 360f
private const val H = 200f
private const val L = 34f
private const val R = 10f
private const val T = 8f
private const val B = 40f
private val ASHRAE = Color(0xFF16A34A)

/** Port of the prototype's `lineChart`: % dissatisfied (0–100) against evenly spaced x labels. */
@Composable
fun LineChart(xLabels: List<String>, series: List<Series>, xTitle: String, description: String) {
    val measurer = rememberTextMeasurer()
    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(W / H)
            .semantics { contentDescription = description },
    ) {
        val s = size.width / W
        val pw = W - L - R
        val ph = H - T - B
        fun sx(i: Int) = (L + pw * i / (xLabels.size - 1)) * s
        fun sy(v: Double) = (T + ph * (1 - v / 100)).toFloat() * s
        fun style(px: Float, c: Color) = TextStyle(color = c, fontSize = (px * s).toSp())

        for (v in listOf(0.0, 25.0, 50.0, 75.0, 100.0)) {
            drawLine(Color.White.copy(alpha = 0.1f), Offset(L * s, sy(v)), Offset((L + pw) * s, sy(v)), s)
            val m = measurer.measure("${v.toInt()}%", style(9f, Muted))
            drawText(m, topLeft = Offset((L - 5) * s - m.size.width, sy(v) + 3 * s - m.firstBaseline))
        }
        drawLine(
            ASHRAE, Offset(L * s, sy(10.0)), Offset((L + pw) * s, sy(10.0)), s,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3 * s, 3 * s)),
        )
        val lim = measurer.measure("ASHRAE limit 10%", style(8f, ASHRAE))
        drawText(lim, topLeft = Offset((L + pw) * s - lim.size.width, sy(10.0) - 3 * s - lim.firstBaseline))

        xLabels.forEachIndexed { i, x ->
            if (x.isEmpty()) return@forEachIndexed
            val m = measurer.measure(x, style(9f, Muted))
            drawText(m, topLeft = Offset(sx(i) - m.size.width / 2f, (T + ph + 12) * s - m.firstBaseline))
        }
        val title = measurer.measure(xTitle, style(9f, Muted))
        drawText(title, topLeft = Offset((L + pw / 2) * s - title.size.width / 2f, (H - 18) * s - title.firstBaseline))

        series.forEachIndexed { k, sr ->
            val path = Path()
            sr.values.forEachIndexed { i, v ->
                if (i == 0) path.moveTo(sx(i), sy(v)) else path.lineTo(sx(i), sy(v))
            }
            drawPath(path, ChartColors[k], style = Stroke(width = 2 * s, join = StrokeJoin.Round))
        }

        var lx = L * s
        series.forEachIndexed { k, sr ->
            drawRect(ChartColors[k], Offset(lx, (H - 10) * s), Size(10 * s, 3 * s))
            val m = measurer.measure(sr.name, style(9f, DocText))
            drawText(m, topLeft = Offset(lx + 13 * s, (H - 6) * s - m.firstBaseline))
            lx += 13 * s + m.size.width + 10 * s
        }
    }
}

/** One colored segment of a band bar; [weight] is its CSS flex value. */
data class BandSeg(val weight: Float, val color: Long, val label: String = "", val whiteText: Boolean = false)

@Composable
fun BandBar(segments: List<BandSeg>, scale: List<String>) {
    Row(
        Modifier
            .padding(top = 4.dp, bottom = 2.dp)
            .fillMaxWidth()
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp)),
    ) {
        for (seg in segments) {
            Box(
                Modifier.weight(seg.weight).fillMaxHeight().clipToBounds().background(Color(seg.color)),
                contentAlignment = Alignment.Center,
            ) {
                if (seg.label.isNotEmpty()) {
                    Text(
                        seg.label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Visible, softWrap = false,
                        color = if (seg.whiteText) Color.White else Bg,
                        // Overflowing labels clip on both sides, like the prototype's centered flex spans.
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                    )
                }
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        for (s in scale) Text(s, fontSize = 9.sp, color = Muted)
    }
}
