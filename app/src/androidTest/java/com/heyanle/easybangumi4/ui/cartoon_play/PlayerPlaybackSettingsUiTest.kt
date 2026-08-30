package com.heyanle.easybangumi4.ui.cartoon_play

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.easybangumi4.danmaku.DanmakuBinding
import com.heyanle.easybangumi4.danmaku.DanmakuMatchOrigin
import com.heyanle.easybangumi4.danmaku.DanmakuBangumi
import com.heyanle.easybangumi4.danmaku.DanmakuComment
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayMode
import com.heyanle.easybangumi4.danmaku.DanmakuEpisode
import com.heyanle.easybangumi4.danmaku.DanmakuManualMatchState
import com.heyanle.easybangumi4.danmaku.DanmakuMatchPage
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackKey
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackStatus
import com.heyanle.easybangumi4.danmaku.DanmakuSourceMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerPlaybackSettingsUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun playbackDetail_exposesDanmakuDisplaySettingsEntry() {
        var openCount = 0
        setMaterialContent {
            DanmakuSection(
                state = DanmakuPlaybackState(),
                onManualMatch = {},
                onRetry = {},
                onOpenDisplaySettings = { openCount++ },
            )
        }

        composeRule.onNodeWithContentDescription("弹幕显示设置")
            .assertExists()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, openCount) }
    }

    @Test
    fun matchedDanmakuCard_usesWholeCardAsSingleChangeMatchAction() {
        var openCount = 0
        setMaterialContent {
            DanmakuSection(
                state = DanmakuPlaybackState(
                    source = DanmakuSourceMetadata(
                        id = "dandanplay",
                        displayName = "弹弹play",
                        attribution = "测试",
                        website = "https://example.com",
                    ),
                    status = DanmakuPlaybackStatus.Matched(
                        binding = testBinding(),
                        comments = List(3) { index -> testComment(index.toLong()) },
                        fromCache = true,
                    ),
                ),
                onManualMatch = { openCount++ },
                onRetry = {},
                onOpenDisplaySettings = {},
            )
        }

        composeRule.onNodeWithText("选择弹幕").assertDoesNotExist()
        composeRule.onNodeWithText("重新匹配").assertDoesNotExist()
        composeRule.onNodeWithText("弹弹play · 第 1 集 · 已缓存").assertExists()
        composeRule.onNodeWithText("3 条弹幕").assertExists()
        val summaryBounds = composeRule
            .onNodeWithTag(
                DanmakuComponentTestTags.MATCH_SUMMARY,
                useUnmergedTree = true,
            )
            .fetchSemanticsNode()
            .boundsInRoot
        val commentCountBounds = composeRule
            .onNodeWithTag(
                DanmakuComponentTestTags.MATCH_COMMENT_COUNT,
                useUnmergedTree = true,
            )
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(commentCountBounds.top >= summaryBounds.bottom)
        composeRule.onNodeWithTag(DanmakuComponentTestTags.MATCH_CARD)
            .assert(
                SemanticsMatcher("has choose danmaku click label") {
                    runCatching {
                        it.config[SemanticsActions.OnClick].label
                    }.getOrNull() == "选择弹幕"
                },
            )
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle { assertEquals(1, openCount) }
    }

    @Test
    fun episodeMatchStep_showsSelectedWorkAndExplicitChangeAction() {
        var changeBangumiCount = 0
        val bangumi = DanmakuBangumi(
            remoteAnimeId = 10L,
            remoteBangumiId = "10",
            title = "测试番剧",
            typeDescription = "TV动画",
        )
        val episode = DanmakuEpisode(
            remoteEpisodeId = 101L,
            remoteAnimeId = bangumi.remoteAnimeId,
            remoteBangumiId = bangumi.remoteBangumiId,
            bangumiTitle = bangumi.title,
            episodeTitle = "第1话",
            episodeNumber = "1",
        )
        setMaterialContent {
            DanmakuMatchBottomSheet(
                state = DanmakuManualMatchState(
                    sourceId = "dandanplay",
                    query = bangumi.title,
                    page = DanmakuMatchPage.EPISODE,
                    hasSearched = true,
                    candidates = listOf(bangumi),
                    selectedBangumi = bangumi,
                    episodes = listOf(episode),
                    selectedEpisode = episode,
                ),
                onQueryChange = {},
                onSearch = {},
                onBangumiSelect = {},
                onEpisodeSelect = {},
                onBackToBangumiSelection = { changeBangumiCount++ },
                onDismiss = {},
                isVisible = true,
            )
        }

        composeRule.onNodeWithText("匹配弹幕").assertExists()
        composeRule.onNodeWithText("1  选择番剧").assertExists()
        composeRule.onNodeWithText("2  选择选集").assertExists()
        composeRule.onNodeWithText("测试番剧").assertExists()
        composeRule.onNodeWithTag(DanmakuComponentTestTags.BANGUMI_STEP)
            .assertIsNotSelected()
        composeRule.onNodeWithTag(DanmakuComponentTestTags.EPISODE_STEP)
            .assertIsSelected()
        composeRule.onNodeWithTag(DanmakuComponentTestTags.CHANGE_BANGUMI)
            .assertHasClickAction()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, changeBangumiCount) }
    }

    @Test
    fun danmakuDensityHelp_opensCompactExplanationDialog() {
        setMaterialContent {
            DanmakuDisplaySettingsContent(
                config = DanmakuDisplayConfig.DEFAULT,
                onConfigChange = {},
                onReset = {},
            )
        }

        composeRule.onNodeWithContentDescription("弹幕数量 说明")
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeRule.onNodeWithText("控制实际显示的弹幕比例。降低比例会均匀减少同一时间段内的弹幕，适合弹幕过密或性能有限的设备。")
            .assertExists()
    }

    @Test
    fun settingsPanel_preservesSelectedTabWhenReopened() {
        var visible by mutableStateOf(true)
        var section by mutableStateOf(PlayerSettingsSection.Danmaku)
        setMaterialContent {
            AdaptivePlayerSettingsPanel(
                visible = visible,
                selectedSection = section,
                onSectionSelected = { section = it },
                onDismiss = { visible = false },
                danmakuConfig = DanmakuDisplayConfig.DEFAULT,
                danmakuSummary = null,
                onDanmakuConfigChange = {},
                onResetDanmaku = {},
                videoScaleType = 0,
                videoScaleOptions = listOf(
                    0 to com.heyanle.easy_i18n.R.string.video_scale_type_default,
                ),
                onVideoScaleSelected = {},
            )
        }

        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .performClick()
        composeRule.runOnIdle {
            assertEquals(PlayerSettingsSection.Video, section)
        }
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .assertIsSelected()

        composeRule.onNodeWithContentDescription("关闭播放设置").performClick()
        composeRule.runOnIdle { visible = true }
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .assertIsSelected()
    }

    @Test
    fun landscapeSettingsPanel_remainsComposedDuringSlideOut() {
        composeRule.mainClock.autoAdvance = false
        var visible by mutableStateOf(true)
        val landscapeConfiguration = Configuration().apply {
            orientation = Configuration.ORIENTATION_LANDSCAPE
            screenWidthDp = 800
            screenHeightDp = 400
        }
        setMaterialContent {
            CompositionLocalProvider(LocalConfiguration provides landscapeConfiguration) {
                AdaptivePlayerSettingsPanel(
                    visible = visible,
                    selectedSection = PlayerSettingsSection.Video,
                    onSectionSelected = {},
                    onDismiss = { visible = false },
                    danmakuConfig = DanmakuDisplayConfig.DEFAULT,
                    danmakuSummary = null,
                    onDanmakuConfigChange = {},
                    onResetDanmaku = {},
                    videoScaleType = 0,
                    videoScaleOptions = listOf(
                        0 to com.heyanle.easy_i18n.R.string.video_scale_type_default,
                    ),
                    onVideoScaleSelected = {},
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(300)
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL)
            .assertExists()

        composeRule.runOnIdle { visible = false }
        composeRule.mainClock.advanceTimeBy(120)
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL)
            .assertExists()

        composeRule.mainClock.advanceTimeBy(180)
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL)
            .assertDoesNotExist()
    }

    @Test
    fun optionalToggle_keepsLegacyControlFreeOfDanmakuEntry() {
        var state by mutableStateOf<PlayerDanmakuControlState?>(null)
        setMaterialContent { OptionalPlayerDanmakuToggle(state) }

        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.DANMAKU_TOGGLE)
            .assertDoesNotExist()

        state = PlayerDanmakuControlState(
            visualState = PlayerDanmakuControlState.VisualState.Available,
            displayEnabled = true,
            contentDescription = "关闭弹幕",
            onClick = {},
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.DANMAKU_TOGGLE)
            .assertExists()
    }

    @Test
    fun quickToggle_exposesEnabledLoadingAndUnavailableSemantics() {
        var clickCount = 0
        var longClickCount = 0
        var state by mutableStateOf(
            PlayerDanmakuControlState(
                visualState = PlayerDanmakuControlState.VisualState.Available,
                displayEnabled = true,
                contentDescription = "关闭弹幕",
                onClick = { clickCount++ },
                onLongClick = { longClickCount++ },
            ),
        )
        setMaterialContent { PlayerDanmakuToggle(state) }

        composeRule.onNodeWithContentDescription("关闭弹幕")
            .assertIsEnabled()
            .assert(stateDescription("已开启"))
            .performClick()
        composeRule.onNodeWithContentDescription("关闭弹幕")
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.runOnIdle { assertEquals(1, clickCount) }
        composeRule.runOnIdle { assertEquals(1, longClickCount) }

        state = state.copy(
            visualState = PlayerDanmakuControlState.VisualState.Loading,
            contentDescription = "弹幕加载中",
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("弹幕加载中")
            .assertIsEnabled()
            .assert(stateDescription("加载中"))
            .performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.runOnIdle { assertEquals(2, longClickCount) }

        state = state.copy(
            visualState = PlayerDanmakuControlState.VisualState.Unavailable,
            contentDescription = "尚未匹配弹幕，点击匹配番剧和选集",
        )
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("尚未匹配弹幕，点击匹配番剧和选集")
            .assertIsEnabled()
            .assert(stateDescription("不可用"))
            .performClick()
        composeRule.runOnIdle { assertEquals(2, clickCount) }
    }

    @Test
    fun controlMapping_togglesVisibilityWithoutDispatchingMatchOrRetry() {
        var toggledTo: Boolean? = null
        var manualMatchCount = 0
        var retryCount = 0
        var sourceSettingsCount = 0
        val matched = DanmakuPlaybackState(
            status = DanmakuPlaybackStatus.Matched(
                binding = testBinding(),
                comments = emptyList(),
                fromCache = true,
            ),
        )

        val available = matched.toPlayerDanmakuControlState(
            displayEnabled = true,
            onToggleDisplay = { toggledTo = it },
            onManualMatch = { manualMatchCount++ },
            onRetry = { retryCount++ },
            onOpenSourceSettings = { sourceSettingsCount++ },
        )
        available.onClick()

        assertEquals(false, toggledTo)
        assertEquals(0, manualMatchCount)
        assertEquals(0, retryCount)
        assertEquals(0, sourceSettingsCount)

        val unmatched = DanmakuPlaybackState(
            status = DanmakuPlaybackStatus.Unmatched("未匹配"),
        ).toPlayerDanmakuControlState(
            displayEnabled = true,
            onToggleDisplay = { toggledTo = it },
            onManualMatch = { manualMatchCount++ },
            onRetry = { retryCount++ },
            onOpenSourceSettings = { sourceSettingsCount++ },
        )
        unmatched.onClick()
        assertEquals(1, manualMatchCount)

        val sourceUnavailable = DanmakuPlaybackState(
            status = DanmakuPlaybackStatus.Unavailable("弹幕源尚未配置"),
        ).toPlayerDanmakuControlState(
            displayEnabled = true,
            onToggleDisplay = { toggledTo = it },
            onManualMatch = { manualMatchCount++ },
            onRetry = { retryCount++ },
            onOpenSourceSettings = { sourceSettingsCount++ },
        )
        sourceUnavailable.onClick()
        assertEquals(1, sourceSettingsCount)

        val transientFailure = DanmakuPlaybackState(
            status = DanmakuPlaybackStatus.Unavailable("网络错误"),
        ).toPlayerDanmakuControlState(
            displayEnabled = true,
            onToggleDisplay = { toggledTo = it },
            onManualMatch = { manualMatchCount++ },
            onRetry = { retryCount++ },
            onOpenSourceSettings = { sourceSettingsCount++ },
        )
        transientFailure.onClick()
        assertEquals(1, retryCount)
    }

    @Test
    fun settingsPanel_switchesSectionsAdjustsRangesAndConfirmsReset() {
        var visible by mutableStateOf(true)
        var section by mutableStateOf(PlayerSettingsSection.Danmaku)
        var config by mutableStateOf(DanmakuDisplayConfig.DEFAULT)
        var selectedScale = 0
        var resetCount = 0
        val scaleOptions = listOf(
            0 to com.heyanle.easy_i18n.R.string.video_scale_type_default,
            1 to com.heyanle.easy_i18n.R.string.video_scale_type_16_9,
            2 to com.heyanle.easy_i18n.R.string.video_scale_type_4_3,
            3 to com.heyanle.easy_i18n.R.string.video_scale_type_match_parent,
            4 to com.heyanle.easy_i18n.R.string.video_scale_type_original,
            5 to com.heyanle.easy_i18n.R.string.video_scale_type_center_crop,
            6 to com.heyanle.easy_i18n.R.string.video_scale_type_adapt,
        )
        setMaterialContent {
            AdaptivePlayerSettingsPanel(
                visible = visible,
                selectedSection = section,
                onSectionSelected = { section = it },
                onDismiss = { visible = false },
                danmakuConfig = config,
                danmakuSummary = "弹弹play · 40 条",
                onDanmakuConfigChange = { config = it },
                onResetDanmaku = {
                    resetCount++
                    config = DanmakuDisplayConfig.DEFAULT
                },
                videoScaleType = selectedScale,
                videoScaleOptions = scaleOptions,
                onVideoScaleSelected = { selectedScale = it },
            )
        }

        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.SETTINGS_PANEL).assertExists()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.DANMAKU_SECTION_TAB)
            .assertIsSelected()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .assertIsNotSelected()
        composeRule.onNodeWithText("弹弹play · 40 条").assertExists()
        composeRule.onNodeWithTag("${PlayerPlaybackSettingsTestTags.FONT_SIZE}_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                assertTrue(setProgress(36f))
            }
        composeRule.onNodeWithTag("${PlayerPlaybackSettingsTestTags.LINE_HEIGHT}_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                assertTrue(setProgress(2f))
            }
        composeRule.onNodeWithTag("${PlayerPlaybackSettingsTestTags.SCROLL_SPEED}_slider")
            .performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                assertTrue(setProgress(0.5f))
            }
        composeRule.runOnIdle {
            assertEquals(36f, config.fontSizeSp)
            assertEquals(2f, config.lineHeightFactor)
            assertEquals(0.5f, config.scrollSpeed)
        }

        composeRule.onNodeWithText("+0.5s").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(500L, config.timeOffsetMillis) }

        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.DANMAKU_SECTION_TAB)
            .assertIsNotSelected()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .assertIsSelected()
        scaleOptions.forEach { (value, _) ->
            composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.videoScale(value))
                .assertExists()
        }
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.videoScale(1))
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle { assertEquals(1, selectedScale) }

        composeRule.onNodeWithContentDescription("关闭播放设置").performClick()
        composeRule.runOnIdle {
            assertFalse(visible)
            visible = true
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.VIDEO_SECTION_TAB)
            .assertIsSelected()
        composeRule.onNodeWithTag(
            PlayerPlaybackSettingsTestTags.VIDEO_SECTION,
            useUnmergedTree = true,
        )
            .assertExists()

        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.DANMAKU_SECTION_TAB)
            .performClick()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.RESET)
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(PlayerPlaybackSettingsTestTags.RESET_CONFIRM).performClick()
        composeRule.runOnIdle {
            assertEquals(1, resetCount)
            assertEquals(DanmakuDisplayConfig.DEFAULT, config)
        }

        composeRule.onNodeWithContentDescription("关闭播放设置").performClick()
        composeRule.runOnIdle { assertFalse(visible) }
    }

    private fun stateDescription(value: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.StateDescription,
            value,
        )

    private fun testBinding() = DanmakuBinding(
        playbackKey = DanmakuPlaybackKey("cartoon", "source", "line", "episode"),
        sourceId = "dandanplay",
        remoteEpisodeId = 1L,
        remoteAnimeId = 2L,
        remoteBangumiId = "3",
        bangumiTitle = "番剧",
        episodeTitle = "第 1 集",
        timeOffsetMillis = 0L,
        origin = DanmakuMatchOrigin.AUTOMATIC,
        createdAtMillis = 0L,
        updatedAtMillis = 0L,
    )

    private fun testComment(id: Long) = DanmakuComment(
        id = id,
        timeMillis = id * 1_000L,
        mode = DanmakuDisplayMode.SCROLL,
        colorArgb = 0xFFFFFF,
        userId = null,
        text = "测试弹幕 $id",
    )

    private fun setMaterialContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            MaterialTheme {
                Surface { content() }
            }
        }
    }
}
