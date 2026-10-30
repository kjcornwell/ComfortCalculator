package com.root3.comfortgauge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Verdict
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.CardBg
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.NumStyle
import com.root3.comfortgauge.ui.theme.TextMain
import com.root3.comfortgauge.ui.theme.toColor

val CardShape = RoundedCornerShape(12.dp)

/** Slider card: uppercase label, large readout, gradient slider. */
@Composable
fun ControlCard(
    label: String,
    valueText: String,
    valueTag: String,
    value: Int,
    range: IntRange,
    stops: List<Pair<Float, Color>>,
    onChange: (Int) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(CardBg, CardShape)
            .padding(start = 12.dp, end = 12.dp, top = 7.dp, bottom = 9.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                label.uppercase(), fontSize = 11.sp, color = Muted, fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.06.em, modifier = Modifier.clearAndSetSemantics { },
            )
            Text(
                valueText, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = TextMain,
                style = NumStyle, modifier = Modifier.testTag(valueTag),
            )
        }
        GradientSlider(value, range, stops, label, valueText, onChange)
    }
}

/** Emoji, colored level word, "thermal · humidity" and the meta line on a tinted card. */
@Composable
fun VerdictCard(v: Verdict, unit: TempUnit) {
    // CSS: linear-gradient(90deg, color@0x33, card 70%) over the page background.
    val start by animateColorAsState(lerp(Bg, v.assessment.color.toColor(), 0x33 / 255f), tween(300), label = "tint")
    val levelColor by animateColorAsState(v.levelColor.toColor(), tween(300), label = "level")
    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(0f to start, 0.7f to CardBg), CardShape)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .clearAndSetSemantics { contentDescription = "${v.levelWord}. ${v.why}. ${v.metaLine(unit)}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(v.emoji, fontSize = 30.sp, lineHeight = 30.sp)
        Column {
            Text(v.levelWord, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = levelColor)
            Text(v.why, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
            Text(
                v.metaLine(unit), fontSize = 10.sp, lineHeight = 13.sp, color = Muted, style = NumStyle,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
