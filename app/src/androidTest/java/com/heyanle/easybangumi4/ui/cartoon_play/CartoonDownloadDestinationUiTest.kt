package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.easybangumi4.cartoon.entity.DownloadDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartoonDownloadDestinationUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun destinationControlsBehaveAsARealTwoWayToggle() {
        val selectedDestinations = mutableListOf<Int>()
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    DownloadDestinationToggle(
                        isFlat = true,
                        onDestinationChange = selectedDestinations::add,
                    )
                }
            }
        }

        composeRule.onNodeWithTag(CartoonDownloadTestTags.DESTINATION_LOCAL_CACHE)
            .assertIsSelected()
        composeRule.onNodeWithTag(CartoonDownloadTestTags.DESTINATION_LOCAL_STORY)
            .assertIsNotSelected()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(DownloadDestination.LOCAL_STORY), selectedDestinations)
        }
    }
}
