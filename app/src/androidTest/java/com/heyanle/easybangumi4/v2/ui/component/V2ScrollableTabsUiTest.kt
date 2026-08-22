package com.heyanle.easybangumi4.v2.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V2ScrollableTabsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun secondaryTabSelection_keepsEveryClickableBoundsStable() {
        val labels = listOf("今天", "昨天", "前天")
        composeRule.setContent {
            MaterialTheme {
                var selectedIndex by remember { mutableIntStateOf(0) }
                V2ScrollableTabs(
                    labels = labels,
                    selectedIndex = selectedIndex,
                    onSelected = { selectedIndex = it },
                    style = V2TabStyle.SecondaryDot,
                )
            }
        }

        val before = labels.associateWith(::clickableBounds)
        composeRule.onNode(hasText("昨天") and hasClickAction()).performClick()
        composeRule.mainClock.advanceTimeBy(220)
        composeRule.waitForIdle()
        val after = labels.associateWith(::clickableBounds)

        labels.forEach { label -> assertRectEquals(before.getValue(label), after.getValue(label)) }
    }

    private fun clickableBounds(label: String): Rect = composeRule
        .onNode(hasText(label) and hasClickAction())
        .fetchSemanticsNode()
        .boundsInRoot

    private fun assertRectEquals(expected: Rect, actual: Rect) {
        assertEquals(expected.left, actual.left, 0.01f)
        assertEquals(expected.top, actual.top, 0.01f)
        assertEquals(expected.right, actual.right, 0.01f)
        assertEquals(expected.bottom, actual.bottom, 0.01f)
    }
}
