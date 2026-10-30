package com.root3.comfortgauge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.Gauges
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.ui.components.ArcGauge
import com.root3.comfortgauge.ui.components.ControlCard
import com.root3.comfortgauge.ui.components.Header
import com.root3.comfortgauge.ui.components.Legend
import com.root3.comfortgauge.ui.components.MiniGauges
import com.root3.comfortgauge.ui.components.VerdictCard
import com.root3.comfortgauge.ui.map.ComfortMap
import com.root3.comfortgauge.ui.theme.CardBg
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.RhTrack
import com.root3.comfortgauge.ui.theme.TabOn
import com.root3.comfortgauge.ui.theme.TempTrack
import com.root3.comfortgauge.ui.theme.TextMain
import com.root3.comfortgauge.ui.theme.toColor

class MainActions(
    val onTempSlider: (Int) -> Unit,
    val onRh: (Int) -> Unit,
    val onMapPick: (Double, Int) -> Unit,
    val onUnit: (TempUnit) -> Unit,
    val onTab: (Tab) -> Unit,
    val onInfo: () -> Unit,
)

/** One viewport, no scrolling; the hero card takes whatever height is left. */
@Composable
fun MainScreen(state: UiState, actions: MainActions, modifier: Modifier = Modifier) {
    val v = state.verdict
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Header(state.unit, actions.onUnit, actions.onInfo)
            ControlCard(
                "Temperature", state.tempLabel, "tempValue", state.sliderValue, state.sliderRange,
                TempTrack, actions.onTempSlider,
            )
            ControlCard("Humidity", "${state.rh}%", "rhValue", state.rh, 0..100, RhTrack, actions.onRh)
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = 150.dp)
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(14.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Tabs(state.tab, actions.onTab)
                val pane = Modifier.weight(1f).fillMaxWidth()
                val description = "Comfort map. ${state.tempLabel}, ${state.rh}% humidity: ${v.levelWord}, ${v.why}"
                when (state.tab) {
                    Tab.MAP -> ComfortMap(
                        state.tempF, state.rh, v.assessment.color.toColor(), state.unit, description,
                        actions.onMapPick, pane,
                    )
                    Tab.GAUGE -> ArcGauge(
                        Gauges.FEELS, v.feelsLike, state.unit, big = true, emoji = v.emoji,
                        modifier = pane.semantics { contentDescription = feelsLikeText(state) },
                    )
                }
                // visibility:hidden on the Gauge tab, so the hero layout doesn't jump.
                val legendHidden = state.tab == Tab.GAUGE
                Legend(
                    if (legendHidden) Modifier.alpha(0f).clearAndSetSemantics { } else Modifier,
                )
            }
            VerdictCard(v, state.unit)
            MiniGauges(v, state.unit)
        }
    }
}

@Composable
private fun Tabs(tab: Tab, onTab: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)) {
        for ((t, name) in listOf(Tab.MAP to "Map", Tab.GAUGE to "Gauge")) {
            val on = t == tab
            Text(
                name,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (on) TextMain else Muted,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (on) TabOn else CardBg)
                    .clickable(role = Role.Tab) { onTab(t) }
                    .semantics { selected = on }
                    .padding(horizontal = 12.dp, vertical = 3.dp),
            )
        }
    }
}

/** Accessible feels-like summary for the big gauge. */
internal fun feelsLikeText(state: UiState): String =
    "Feels like ${Units.format(state.verdict.feelsLike, state.unit)}"
