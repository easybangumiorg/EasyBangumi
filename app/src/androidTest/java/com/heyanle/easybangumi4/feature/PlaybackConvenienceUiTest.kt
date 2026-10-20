package com.heyanle.easybangumi4.feature

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.sourcelab.foregroundSourceLabForDeviceTests
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerActionNotice
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerSleepTimerOption
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerSleepTimerSettingsContent
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerSleepTimerState
import com.heyanle.easybangumi4.v2.ui.home.ContinueWatchingBar
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

/**
 * Runs real controls with in-memory fixtures in the isolated source acceptance package.
 * Captures are component crops, not screenshots of the complete application or a real playback.
 */
@RunWith(AndroidJUnit4::class)
class PlaybackConvenienceUiTest {
    private val composeRule = createComposeRule()

    @get:Rule
    val rules: TestRule = RuleChain.outerRule(object : ExternalResource() {
        override fun before() {
            foregroundSourceLabForDeviceTests()
        }
    }).around(composeRule)

    @Test
    fun continueWatching_resumesFromButtonAndRow_andClosesWithoutResuming() {
        var resumeCount = 0
        var dismissCount = 0
        var visible by mutableStateOf(true)
        setContent {
            if (visible) {
                ContinueWatchingBar(
                    item = cartoon(),
                    onResume = { resumeCount++ },
                    onDismiss = { dismissCount++; visible = false },
                )
            }
        }

        composeRule.onNodeWithText("继续第 3 集 · 01:35").assertIsDisplayed()
        listOf("continue-watching-play", "continue-watching-close").forEach { tag ->
            composeRule.onNodeWithTag(tag)
                .assertIsDisplayed()
                .assertWidthIsAtLeast(44.dp)
                .assertHeightIsAtLeast(44.dp)
        }
        captureComponent("continue-watching", "continue-watching-component.png")
        composeRule.onNodeWithTag("continue-watching-play").performClick()
        composeRule.runOnIdle { assertEquals(1, resumeCount) }
        composeRule.onNodeWithText("本地测试番剧").performClick()
        composeRule.runOnIdle { assertEquals(2, resumeCount) }
        composeRule.onNodeWithTag("continue-watching-close").performClick()
        composeRule.runOnIdle {
            assertEquals(1, dismissCount)
            assertEquals("Closing the bar must not also resume playback", 2, resumeCount)
        }
        composeRule.onNodeWithTag("continue-watching").assertDoesNotExist()
    }

    @Test
    fun continueWatching_withoutEpisodeOrDuration_hasReadableFallback() {
        setContent {
            ContinueWatchingBar(
                item = cartoon().copy(lastEpisodeLabel = "", lastProcessTime = 0, lastTotalTile = 0),
                onResume = {},
                onDismiss = {},
            )
        }

        composeRule.onNodeWithText("继续观看 · 00:00").assertIsDisplayed()
        composeRule.onNodeWithTag("continue-watching-play").assertIsDisplayed()
        composeRule.onNodeWithTag("continue-watching-close").assertIsDisplayed()
    }

    @Test
    fun historyJumpNotice_showsPosition_andCancelInvokesActionOnce() {
        var cancelCount = 0
        var visible by mutableStateOf(true)
        setContent {
            if (visible) {
                PlayerActionNotice(
                    message = "已跳转至 01:35",
                    actionLabel = "取消跳转",
                    onAction = { cancelCount++; visible = false },
                )
            }
        }

        composeRule.onNodeWithText("已跳转至 01:35").assertIsDisplayed()
        composeRule.onNodeWithText("取消跳转")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(44.dp)
        captureComponent("player-action-notice", "history-jump-notice-component.png")
        composeRule.onNodeWithText("取消跳转").performClick()
        composeRule.runOnIdle { assertEquals(1, cancelCount) }
        composeRule.onNodeWithTag("player-action-notice").assertDoesNotExist()
    }

    @Test
    fun sleepTimer_eachOptionCallsSelection_andOnlyCurrentOptionIsSelected() {
        val selectedOptions = mutableListOf<PlayerSleepTimerOption>()
        var timerState by mutableStateOf(PlayerSleepTimerState())
        setContent {
            PlayerSleepTimerSettingsContent(
                state = timerState,
                onOptionSelected = { option ->
                    selectedOptions += option
                    timerState = PlayerSleepTimerState(option, option.minutes?.times(60_000L) ?: 0L)
                },
                modifier = Modifier.testTag("sleep-timer-component"),
            )
        }

        composeRule.onNodeWithTag("player_sleep_timer_status").assertTextEquals("未开启")
        PlayerSleepTimerOption.entries.forEach { option ->
            composeRule.onNodeWithTag(optionTag(option)).assertIsDisplayed().performClick()
            PlayerSleepTimerOption.entries.forEach { other ->
                val node = composeRule.onNodeWithTag(optionTag(other))
                if (other == option) node.assertIsSelected() else node.assertIsNotSelected()
            }
        }
        composeRule.runOnIdle { assertEquals(PlayerSleepTimerOption.entries.toList(), selectedOptions) }
        composeRule.onNodeWithTag("player_sleep_timer_status")
            .assertTextEquals("将在本集结束后停止")
        composeRule.onNodeWithText("播完当前集后停止，不自动播放下一集").assertIsDisplayed()
        captureComponent("sleep-timer-component", "sleep-timer-end-of-episode-component.png")
    }

    @Test
    fun sleepTimer_remainingAndCompletedStatesUpdateInPlace() {
        var timerState by mutableStateOf(
            PlayerSleepTimerState(PlayerSleepTimerOption.FIFTEEN_MINUTES, remainingMs = 61_001L),
        )
        setContent {
            PlayerSleepTimerSettingsContent(
                state = timerState,
                onOptionSelected = {},
                modifier = Modifier.testTag("sleep-timer-component"),
            )
        }

        composeRule.onNodeWithTag("player_sleep_timer_status").assertTextEquals("1:02 后暂停播放")
        composeRule.onNodeWithTag(optionTag(PlayerSleepTimerOption.FIFTEEN_MINUTES)).assertIsSelected()
        captureComponent("sleep-timer-component", "sleep-timer-countdown-component.png")
        composeRule.runOnIdle { timerState = timerState.copy(remainingMs = 1_000L) }
        composeRule.onNodeWithTag("player_sleep_timer_status").assertTextEquals("0:01 后暂停播放")
        composeRule.runOnIdle { timerState = PlayerSleepTimerState(stoppedByTimer = true) }
        composeRule.onNodeWithTag("player_sleep_timer_status").assertTextEquals("已按睡眠定时暂停播放")
        composeRule.onNodeWithTag(optionTag(PlayerSleepTimerOption.OFF)).assertIsSelected()
    }

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxWidth(), content = content)
            }
        }
    }

    private fun captureComponent(tag: String, name: String) {
        val bitmap = composeRule.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.cacheDir,
            "feature-acceptance",
        ).apply { mkdirs() }
        File(directory, name).outputStream().use { output ->
            assertTrue("Component screenshot must be saved", bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    private fun optionTag(option: PlayerSleepTimerOption) = "player_sleep_timer_${option.name.lowercase()}"

    private fun cartoon() = CartoonInfo(
        id = "component-fixture",
        source = "local-component-test",
        name = "本地测试番剧",
        coverUrl = "",
        intro = "",
        url = "",
        lastEpisodeLabel = "第 3 集",
        lastProcessTime = 95_000L,
        lastTotalTile = 1_440_000L,
        createTime = 0L,
    )
}
