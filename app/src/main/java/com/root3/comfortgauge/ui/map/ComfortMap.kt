package com.root3.comfortgauge.ui.map

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.Crosshair
import com.root3.comfortgauge.ui.theme.GridLine
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.TNUM
import kotlin.math.roundToInt

private val PAD_L = 30.dp
private val PAD_R = 16.dp
private val PAD_T = 8.dp
private val PAD_B = 18.dp

private data class Tick(val f: Double, val label: String)

private fun xTicks(unit: TempUnit): List<Tick> =
    if (unit == TempUnit.F) (50..100 step 10).map { Tick(it.toDouble(), "$it°") }
    else (10..38 step 5).map { Tick(Units.c2f(it.toDouble()), "$it°") }

/**
 * 2-D comfort map: temperature 50–100°F on x, humidity 0–100% on y. Dragging anywhere sets both.
 * [description] is the accessible summary of the current verdict.
 */
@Composable
fun ComfortMap(
    tempF: Double,
    rh: Int,
    markerColor: Color,
    unit: TempUnit,
    description: String,
    onPick: (tempF: Double, rh: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { MapBitmap.ensureBuilt() }
    val grid by MapBitmap.bitmap.collectAsState()
    val measurer = rememberTextMeasurer()
    val pick by rememberUpdatedState(onPick)
    val ticks = xTicks(unit)
    val gridPaint = remember { Paint().apply { isFilterBitmap = true; alpha = (0.88f * 255).roundToInt() } }

    Canvas(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .testTag("comfortMap")
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                fun handle(p: Offset) {
                    val pw = size.width - PAD_L.toPx() - PAD_R.toPx()
                    val ph = size.height - PAD_T.toPx() - PAD_B.toPx()
                    val t = MAP_XMIN + (p.x - PAD_L.toPx()).toDouble() / pw * (MAP_XMAX - MAP_XMIN)
                    val h = (1 - (p.y - PAD_T.toPx()).toDouble() / ph) * 100
                    pick(t.coerceIn(MAP_XMIN, MAP_XMAX), Units.jsRound(h.coerceIn(0.0, 100.0)))
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    handle(down.position)
                    drag(down.id) { c -> c.consume(); handle(c.position) }
                }
            },
    ) {
        val l = PAD_L.toPx(); val t = PAD_T.toPx()
        val pw = size.width - l - PAD_R.toPx()
        val ph = size.height - t - PAD_B.toPx()
        if (pw <= 0 || ph <= 0) return@Canvas
        fun px(f: Double) = (l + pw * (f - MAP_XMIN) / (MAP_XMAX - MAP_XMIN)).toFloat()
        fun py(r: Double) = (t + ph * (1 - r / 100)).toFloat()

        // Bilinear upscaling of the 0.25°F × 0.5% grid gives the smooth zone edges.
        grid?.let { bmp ->
            drawIntoCanvas { c ->
                c.nativeCanvas.drawBitmap(bmp, null, RectF(l, t, l + pw, t + ph), gridPaint)
            }
        }
        val hair = 1.dp.toPx()
        for (tk in ticks) {
            if (tk.f <= MAP_XMIN || tk.f >= MAP_XMAX) continue
            drawLine(GridLine, Offset(px(tk.f), t), Offset(px(tk.f), t + ph), hair)
        }
        for (r in listOf(25.0, 50.0, 75.0)) drawLine(GridLine, Offset(l, py(r)), Offset(l + pw, py(r)), hair)

        val style = TextStyle(color = Muted, fontSize = 9.sp, fontFeatureSettings = TNUM)
        for (tk in ticks) {
            val m = measurer.measure(tk.label, style)
            drawText(m, topLeft = Offset(px(tk.f) - m.size.width / 2f, t + ph + 4.dp.toPx()))
        }
        for (r in listOf(0, 50, 100)) {
            val m = measurer.measure("$r%", style)
            drawText(m, topLeft = Offset(l - 4.dp.toPx() - m.size.width, py(r.toDouble()) - m.size.height / 2f))
        }

        val edge = if (tempF < MAP_XMIN) -1 else if (tempF > MAP_XMAX) 1 else 0
        val x = px(tempF.coerceIn(MAP_XMIN, MAP_XMAX))
        val y = py(rh.toDouble())
        val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
        drawLine(Crosshair, Offset(x, t), Offset(x, t + ph), hair, pathEffect = dash)
        drawLine(Crosshair, Offset(l, y), Offset(l + pw, y), hair, pathEffect = dash)
        if (edge != 0) {
            val d = 1.dp.toPx()
            drawPath(
                Path().apply {
                    moveTo(x + edge * 13 * d, y)
                    lineTo(x + edge * 21 * d, y - 6 * d)
                    lineTo(x + edge * 21 * d, y + 6 * d)
                    close()
                },
                Color.White,
            )
        }
        val r9 = 9.dp.toPx()
        drawCircle(Color.White, r9, Offset(x, y))
        drawCircle(Bg, r9, Offset(x, y), style = Stroke(3.dp.toPx()))
        drawCircle(markerColor, 3.5.dp.toPx(), Offset(x, y))
    }
}
