package com.root3.comfortgauge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.TextMain

/**
 * Title, °F/°C toggle and ⓘ. The controls keep the prototype's compact look; Compose's
 * minimum-touch-target hit testing expands each of them to 48dp.
 */
@Composable
fun Header(unit: TempUnit, onUnit: (TempUnit) -> Unit, onInfo: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Comfort Gauge", fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitToggle(unit, onUnit)
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Muted, CircleShape)
                    .clickable(role = Role.Button, onClick = onInfo)
                    .semantics { contentDescription = "About this tool" },
                contentAlignment = Alignment.Center,
            ) {
                Text("i", fontSize = 14.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif, color = TextMain)
            }
        }
    }
}

@Composable
private fun UnitToggle(unit: TempUnit, onUnit: (TempUnit) -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Row(Modifier.clip(shape).border(1.5.dp, Muted, shape)) {
        for (u in TempUnit.entries) {
            val on = u == unit
            Text(
                "°${u.name}",
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (on) Bg else Muted,
                modifier = Modifier
                    .background(if (on) TextMain else Bg.copy(alpha = 0f))
                    .clickable(role = Role.Tab) { onUnit(u) }
                    .semantics { selected = on; contentDescription = if (u == TempUnit.F) "Fahrenheit" else "Celsius" }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
