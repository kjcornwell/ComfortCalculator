package com.root3.comfortgauge.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.root3.comfortgauge.ui.theme.Bg
import kotlin.math.roundToInt

private val THUMB = 30.dp
private val TRACK = 10.dp
private val BORDER = 3.dp

/**
 * Range input matching the prototype: a full-width 10dp gradient track and a 30dp white thumb
 * whose centre travels between the two thumb radii. Whole steps over [range].
 */
@Composable
fun GradientSlider(
    value: Int,
    range: IntRange,
    stops: List<Pair<Float, Color>>,
    label: String,
    valueText: String,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentRange by rememberUpdatedState(range)
    val currentOnChange by rememberUpdatedState(onChange)
    val span = (range.last - range.first).coerceAtLeast(1)
    val brush = Brush.horizontalGradient(*stops.toTypedArray())

    Canvas(
        modifier
            .fillMaxWidth()
            .height(THUMB)
            .semantics {
                contentDescription = label
                stateDescription = valueText
                progressBarRangeInfo = ProgressBarRangeInfo(
                    value.toFloat(),
                    range.first.toFloat()..range.last.toFloat(),
                    steps = (span - 1).coerceAtLeast(0),
                )
                setProgress { target ->
                    currentOnChange(target.roundToInt().coerceIn(currentRange))
                    true
                }
            }
            .focusable()
            .pointerInput(Unit) {
                val r = THUMB.toPx() / 2
                fun valueAt(x: Float): Int {
                    val rg = currentRange
                    val usable = (size.width - 2 * r).coerceAtLeast(1f)
                    val f = ((x - r) / usable).coerceIn(0f, 1f)
                    return (rg.first + f * (rg.last - rg.first)).roundToInt()
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    currentOnChange(valueAt(down.position.x))
                    drag(down.id) { change ->
                        change.consume()
                        currentOnChange(valueAt(change.position.x))
                    }
                }
            },
    ) {
        val r = THUMB.toPx() / 2
        val th = TRACK.toPx()
        val cy = size.height / 2
        drawRoundRect(
            brush = brush,
            topLeft = Offset(0f, cy - th / 2),
            size = Size(size.width, th),
            cornerRadius = CornerRadius(th / 2),
        )
        val f = (value - range.first).toFloat() / span
        val cx = r + f.coerceIn(0f, 1f) * (size.width - 2 * r)
        val c = Offset(cx, cy)
        // box-shadow: 0 2px 6px rgba(0,0,0,.5)
        for (i in 4 downTo 1) {
            drawCircle(Color.Black.copy(alpha = 0.09f), radius = r + i * 1.dp.toPx(), center = c + Offset(0f, 2.dp.toPx()))
        }
        drawCircle(Color.White, radius = r, center = c)
        val b = BORDER.toPx()
        drawCircle(Bg, radius = r - b / 2, center = c, style = Stroke(b))
    }
}
