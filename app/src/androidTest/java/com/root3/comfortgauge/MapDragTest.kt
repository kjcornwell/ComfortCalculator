package com.root3.comfortgauge

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapDragTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun text(tag: String): String =
        rule.onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }

    @Test
    fun draggingOnTheMapSetsBothSliders() {
        rule.waitUntil(5_000) { rule.onAllNodes(hasTestTag("tempValue")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Map").performClick()
        rule.waitForIdle()
        val temp0 = text("tempValue")
        val rh0 = text("rhValue")

        rule.onNodeWithTag("comfortMap").performTouchInput {
            down(percentOffset(0.35f, 0.75f))
            moveTo(percentOffset(0.55f, 0.5f))
            moveTo(percentOffset(0.85f, 0.2f))
            up()
        }
        rule.waitForIdle()

        assertNotEquals("temperature readout should change", temp0, text("tempValue"))
        assertNotEquals("humidity readout should change", rh0, text("rhValue"))
    }

}
