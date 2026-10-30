package com.root3.comfortgauge.ui.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.root3.comfortgauge.model.TempUnit
import com.root3.comfortgauge.ui.components.CardShape
import com.root3.comfortgauge.ui.theme.Bg
import com.root3.comfortgauge.ui.theme.CardBg
import com.root3.comfortgauge.ui.theme.DocText
import com.root3.comfortgauge.ui.theme.Muted
import com.root3.comfortgauge.ui.theme.TextMain

/** Full-screen "About" page; the title bar stays put while the body scrolls. */
@Composable
fun InfoScreen(unit: TempUnit, onDone: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .pointerInput(Unit) { } // a hit target, so touches never reach the main screen underneath
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.widthIn(max = 520.dp).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().background(Bg).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "About Comfort Gauge", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextMain,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    "Done", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, Muted, RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onDone)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
            ) {
                InfoContent(unit)
            }
        }
    }
}

@Composable
internal fun H2(text: String) {
    Text(
        text, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
        modifier = Modifier.padding(top = 22.dp, bottom = 6.dp).semantics { heading() },
    )
}

@Composable
internal fun P(text: AnnotatedString, color: Color = DocText, size: TextUnit = 14.sp) {
    Text(text, fontSize = size, lineHeight = size * 1.5f, color = color, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
internal fun Bullets(items: List<AnnotatedString>, color: Color = DocText, size: TextUnit = 14.sp) {
    Column(Modifier.padding(start = 6.dp, bottom = 8.dp)) {
        for (item in items) {
            Row(Modifier.padding(bottom = 3.dp)) {
                Text("•", fontSize = size, lineHeight = size * 1.5f, color = color, modifier = Modifier.widthIn(min = 12.dp))
                Text(item, fontSize = size, lineHeight = size * 1.5f, color = color)
            }
        }
    }
}

@Composable
internal fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(CardBg, CardShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            title.uppercase(), fontSize = 12.sp, lineHeight = 16.sp, color = Muted, letterSpacing = 0.06.em,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp),
        )
        content()
    }
}
