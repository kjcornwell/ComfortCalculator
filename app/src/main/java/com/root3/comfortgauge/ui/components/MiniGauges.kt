package com.root3.comfortgauge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.Cause
import com.root3.comfortgauge.model.ComfortModel
import com.root3.comfortgauge.model.GaugeConfig
import com.root3.comfortgauge.model.Gauges
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.model.Units
import com.root3.comfortgauge.model.Verdict
import com.root3.comfortgauge.ui.theme.CardBg
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.TextMain
import com.root3.comfortgauge.ui.theme.toColor

/** Heat Index, Apparent, Humidex and Dew Point, each with its zone label. */
@Composable
fun MiniGauges(v: Verdict, unit: TempUnit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val items = listOf(
            Triple("Heat Index", Gauges.FEELS, v.heatIndex),
            Triple("Apparent", Gauges.FEELS, v.apparent),
            Triple("Humidex", Gauges.FEELS, v.humidex),
            Triple("Dew Point", Gauges.DEW, v.dewPoint),
        )
        for ((title, cfg, value) in items) {
            MiniGauge(title, cfg, value, unit, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniGauge(title: String, cfg: GaugeConfig, value: Double, unit: TempUnit, modifier: Modifier) {
    val zone = cfg.zoneOf(value)
    Column(
        modifier
            .background(CardBg, CardShape)
            .padding(start = 4.dp, end = 4.dp, top = 5.dp, bottom = 4.dp)
            .clearAndSetSemantics { contentDescription = "$title ${Units.format(value, unit)}, ${zone.label}" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(bottom = 1.dp),
        )
        ArcGauge(cfg, value, unit, big = false, modifier = Modifier.fillMaxWidth().aspectRatio(200f / 112f))
        Text(
            zone.label, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.SemiBold,
            color = zone.color.toColor(), maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 1.dp),
        )
    }
}

/** Ideal, Good, then each cause with its 3-step ramp. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Legend(modifier: Modifier = Modifier) {
    FlowRow(
        modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem("Ideal", listOf(ComfortModel.OK[0]), swatchFirst = true)
        LegendItem("Good", listOf(ComfortModel.OK[1]), swatchFirst = true)
        for ((name, cause) in listOf("Cold" to Cause.COLD, "Hot" to Cause.HOT, "Dry" to Cause.DRY, "Humid" to Cause.HUMID)) {
            LegendItem(name, ComfortModel.ramp(cause), swatchFirst = false)
        }
        Text("lighter → darker = marginal → poor → bad", fontSize = 9.sp, lineHeight = 11.sp, color = Muted)
    }
}

@Composable
private fun LegendItem(label: String, colors: List<Long>, swatchFirst: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (!swatchFirst) LegendLabel(label)
        for (c in colors) {
            Box(Modifier.padding(end = 1.dp).size(8.dp).background(c.toColor(), RoundedCornerShape(2.dp)))
        }
        if (swatchFirst) LegendLabel(label)
    }
}

@Composable
private fun LegendLabel(label: String) {
    Text(label, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMain, modifier = Modifier.padding(end = 3.dp))
}
