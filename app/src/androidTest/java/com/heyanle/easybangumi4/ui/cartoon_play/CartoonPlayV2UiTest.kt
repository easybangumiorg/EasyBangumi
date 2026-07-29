package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.pressBack
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartoonPlayV2UiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun episodeRailButton_shortLabel_isCompactAndKeepsAccessibleTouchHeight() {
        val episode = Episode("episode-compact", "第 1 集", 1)
        setMaterialContent {
            Column {
                EpisodeRailButton(
                    episode = episode,
                    selected = false,
                    onClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode(episode.id))
            .assertWidthIsEqualTo(72.dp)
            .assertHeightIsEqualTo(48.dp)
            .assertHasClickAction()
    }

    @Test
    fun recordingOverlayHost_handsOffSurfaceOnce_andBackDismissesOverlay() {
        var recording by mutableStateOf<String?>(null)
        val recordingModes = mutableListOf<Boolean>()

        setMaterialContent {
            RecordingOverlayHost(
                recording = recording,
                onRecordingModeChanged = recordingModes::add,
                onDismissRequest = { recording = null },
            ) { token, _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(token)
                }
            }
        }

        composeRule.onNodeWithTag(CartoonRecordedHostTestTags.OVERLAY).assertDoesNotExist()
        composeRule.runOnIdle {
            assertEquals(emptyList<Boolean>(), recordingModes)
            recording = "first-model"
        }
        composeRule.onNodeWithTag(CartoonRecordedHostTestTags.OVERLAY).assertExists()
        composeRule.onNodeWithText("first-model").assertExists()
        composeRule.runOnIdle {
            assertEquals(listOf(true), recordingModes)
            recording = "replacement-model"
        }
        composeRule.onNodeWithText("replacement-model").assertExists()
        composeRule.runOnIdle {
            assertEquals(listOf(true), recordingModes)
        }

        pressBack()

        composeRule.onNodeWithTag(CartoonRecordedHostTestTags.OVERLAY).assertDoesNotExist()
        composeRule.runOnIdle {
            assertEquals(listOf(true, false), recordingModes)
        }
    }

    @Test
    fun recordingOverlayHost_surfaceCallbackFailure_doesNotBreakLaterExitSync() {
        var recording by mutableStateOf<String?>(null)
        val recordingModes = mutableListOf<Boolean>()

        setMaterialContent {
            RecordingOverlayHost(
                recording = recording,
                onRecordingModeChanged = {
                    recordingModes += it
                    if (it) error("simulated unbind failure")
                },
                onDismissRequest = { recording = null },
            ) { _, dismiss ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("fake_recording_content"),
                    onClick = dismiss,
                ) {}
            }
        }

        composeRule.runOnIdle { recording = "recording" }
        composeRule.onNodeWithTag("fake_recording_content").assertExists().performClick()
        composeRule.onNodeWithTag(CartoonRecordedHostTestTags.OVERLAY).assertDoesNotExist()
        composeRule.runOnIdle {
            assertEquals(listOf(true, false), recordingModes)
        }
    }

    @Test
    fun mediaDetails_expandAndCollapse_switchesRenderedStateAndControl() {
        val cartoon = CartoonInfo(
            id = "cartoon-1",
            source = "source-1",
            name = "测试番剧",
            coverUrl = "",
            intro = "简介",
            url = "",
            description = "这是一段足够长的详情，用于验证播放页能够在折叠和展开状态之间切换。",
        )
        setMaterialContent { V2MediaIdentity(cartoon) }

        composeRule.onNodeWithTag(
            CartoonPlayV2TestTags.MEDIA_COLLAPSED,
            useUnmergedTree = true,
        ).assertExists().assertHeightIsEqualTo(68.dp)
        composeRule.onNodeWithContentDescription("展开详情").assertHasClickAction().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(
            CartoonPlayV2TestTags.MEDIA_EXPANDED,
            useUnmergedTree = true,
        ).assertExists().assertHeightIsAtLeast(140.dp)
        composeRule.onNodeWithTag(
            CartoonPlayV2TestTags.MEDIA_COLLAPSED,
            useUnmergedTree = true,
        ).assertDoesNotExist()
        composeRule.onNodeWithContentDescription("收起详情").assertHasClickAction().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(
            CartoonPlayV2TestTags.MEDIA_COLLAPSED,
            useUnmergedTree = true,
        ).assertExists()
        composeRule.onNodeWithTag(
            CartoonPlayV2TestTags.MEDIA_EXPANDED,
            useUnmergedTree = true,
        ).assertDoesNotExist()
    }

    @Test
    fun actionRow_exposesAllFiveActions_andDispatchesTheMatchingCallback() {
        val clicks = mutableListOf<String>()
        setMaterialContent {
            V2ActionRow(
                isStar = false,
                onStar = { clicks += "追番" },
                onSearch = { clicks += "搜索" },
                onWeb = { clicks += "网站" },
                onDownload = { clicks += "下载" },
                onExternalPlay = { clicks += "外部播放" },
            )
        }

        val labels = listOf("追番", "搜索", "网站", "下载", "外部播放")
        labels.forEach { label ->
            composeRule.onNodeWithTag(CartoonPlayV2TestTags.action(label))
                .assertExists()
                .assertHasClickAction()
                .performClick()
        }

        composeRule.runOnIdle { assertEquals(labels, clicks) }
    }

    @Test
    fun downloadSelection_startsEmpty_togglesEpisodes_andOnlySubmitsSelectedItems() {
        val episodes = listOf(
            Episode("episode-1", "第 1 集", 1),
            Episode("episode-2", "第 2 集", 2),
            Episode("episode-3", "第 3 集", 3),
        )
        var submittedEpisodeIds = emptyList<String>()
        setMaterialContent {
            DownloadSelectionHarness(
                episodes = episodes,
                onSubmit = { submittedEpisodeIds = it.map(Episode::id) },
            )
        }

        composeRule.onNodeWithTag(CartoonPlayV2TestTags.DOWNLOAD_SELECTION).assertExists()
        composeRule.onNodeWithText("已选择 0 集").assertExists()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.DOWNLOAD_CONFIRM).assertIsNotEnabled()
        episodes.forEach {
            composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode(it.id))
                .performScrollTo()
                .assertIsNotSelected()
        }

        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode("episode-2")).performClick()
        composeRule.onNodeWithText("已选择 1 集").assertExists()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode("episode-2")).assertIsSelected()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode("episode-1")).assertIsNotSelected()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.DOWNLOAD_CONFIRM)
            .assertIsEnabled()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(listOf("episode-2"), submittedEpisodeIds)
        }
    }

    @Test
    fun downloadSelection_selectAllTogglesAllEpisodesWithoutChangingPlayback() {
        val episodes = listOf(
            Episode("episode-1", "第 1 集", 1),
            Episode("episode-2", "第 2 集", 2),
        )
        var playbackClicks = 0
        setMaterialContent {
            DownloadSelectionHarness(
                episodes = episodes,
                onPlaybackClick = { playbackClicks++ },
            )
        }

        composeRule.onNodeWithTag(CartoonPlayV2TestTags.DOWNLOAD_SELECT_ALL).performClick()
        composeRule.onNodeWithText("已选择 2 集").assertExists()
        episodes.forEach {
            composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode(it.id)).assertIsSelected()
        }
        composeRule.runOnIdle { assertEquals(0, playbackClicks) }

        composeRule.onNodeWithTag(CartoonPlayV2TestTags.DOWNLOAD_SELECT_ALL).performClick()
        composeRule.onNodeWithText("已选择 0 集").assertExists()
        episodes.forEach {
            composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode(it.id)).assertIsNotSelected()
        }
    }

    @Test
    fun sortSheet_cyclesDefaultAndNameDirections_withoutLosingSourceOrEpisodeSelection() {
        val firstLine = PlayLine(
            id = "line-a",
            label = "线路 A",
            episode = arrayListOf(
                Episode("a-2", "第 2 集", 2),
                Episode("a-1", "第 1 集", 1),
            ),
        )
        val selectedEpisode = Episode("b-2", "Beta", 2)
        val selectedLine = PlayLine(
            id = "line-b",
            label = "线路 B",
            episode = arrayListOf(
                selectedEpisode,
                Episode("b-1", "Alpha", 1),
            ),
        )
        val sortOptions = listOf(
            SortBy<Episode>(PlayLineWrapper.SORT_DEFAULT_KEY, "默认", compareBy(Episode::order)),
            SortBy("label", "名称", compareBy(Episode::label)),
        )

        setMaterialContent {
            SortAndSelectionHarness(
                rawLines = listOf(firstLine, selectedLine),
                selectedLineId = selectedLine.id,
                selectedEpisode = selectedEpisode,
                sortOptions = sortOptions,
            )
        }

        assertSelectionIsStable()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.SORT_CONTROL).performClick()
        assertSortState("默认，正序")

        clickSortOption("名称")
        assertSortState("名称，正序")
        assertSelectionIsStable()

        clickSortOption("名称")
        assertSortState("名称，倒序")
        assertSelectionIsStable()

        clickSortOption("默认")
        assertSortState("默认，正序")
        assertSelectionIsStable()

        clickSortOption("默认")
        assertSortState("默认，倒序")
        assertSelectionIsStable()
    }

    @Test
    fun episodePicker_switchesSource_filtersAndSelectsEpisode_thenCanOpenSortSheet() {
        val firstLine = PlayLine(
            id = "line-a",
            label = "线路 A",
            episode = arrayListOf(Episode("a-1", "第 1 集", 1)),
        )
        val targetEpisode = Episode("b-12", "特别长的第 12 集标签", 12)
        val secondLine = PlayLine(
            id = "line-b",
            label = "线路 B",
            episode = arrayListOf(
                Episode("b-2", "第 2 集", 2),
                targetEpisode,
            ),
        )
        val sortOptions = listOf(
            SortBy<Episode>(PlayLineWrapper.SORT_DEFAULT_KEY, "默认", compareBy(Episode::order)),
            SortBy("label", "名称", compareBy(Episode::label)),
        )
        var selectedLineId: String? = null
        var selectedEpisodeId: String? = null

        setMaterialContent {
            EpisodePickerHarness(
                rawLines = listOf(firstLine, secondLine),
                sortOptions = sortOptions,
                onSelected = { lineId, episodeId ->
                    selectedLineId = lineId
                    selectedEpisodeId = episodeId
                },
            )
        }

        composeRule.onNodeWithText("全部选集").performClick()
        composeRule.onNodeWithText("搜索剧集").assertExists()
        composeRule.onNodeWithText("线路 B").performClick()
        composeRule.onNodeWithText("搜索剧集").performTextInput("特别长")
        composeRule.onNode(
            hasText(targetEpisode.label) and
                hasClickAction() and
                hasAnyAncestor(hasTestTag(CartoonPlayV2TestTags.EPISODE_PICKER)),
        ).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals("line-b", selectedLineId)
            assertEquals(targetEpisode.id, selectedEpisodeId)
        }
        composeRule.onNodeWithText("搜索剧集").assertDoesNotExist()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode(targetEpisode.id)).assertIsSelected()

        composeRule.onNodeWithText("全部选集").performClick()
        composeRule.onNodeWithContentDescription("切换排序").performClick()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.SORT_SHEET).assertExists()
    }

    private fun clickSortOption(label: String) {
        composeRule.onNode(
            hasText(label) and
                hasClickAction() and
                hasAnyAncestor(hasTestTag(CartoonPlayV2TestTags.SORT_SHEET)),
        ).performClick()
        composeRule.waitForIdle()
    }

    private fun assertSortState(expected: String) {
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.SORT_SHEET)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    expected,
                ),
            )
    }

    private fun assertSelectionIsStable() {
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.source("line-a")).assertIsNotSelected()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.source("line-b")).assertIsSelected()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode("b-2")).assertIsSelected()
        composeRule.onNodeWithTag(CartoonPlayV2TestTags.episode("b-1")).assertIsNotSelected()
    }

    private fun setMaterialContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            MaterialTheme {
                Surface { content() }
            }
        }
    }
}

@Composable
private fun DownloadSelectionHarness(
    episodes: List<Episode>,
    onPlaybackClick: () -> Unit = {},
    onSubmit: (List<Episode>) -> Unit = {},
) {
    val line = remember(episodes) {
        PlayLine(
            id = "download-line",
            label = "下载线路",
            episode = ArrayList(episodes),
        )
    }
    val wrappedLine = remember(line) {
        PlayLineWrapper(
            playLine = line,
            comparator = compareBy(Episode::order),
        )
    }
    var selection by remember {
        mutableStateOf(DownloadEpisodeSelection(lineId = line.id))
    }
    val playingState = remember(wrappedLine, episodes) {
        CartoonPlayViewModel.CartoonPlayState(
            cartoonSummary = CartoonSummary("cartoon-1", "source-1"),
            playLine = wrappedLine,
            episode = episodes.first(),
        )
    }

    V2EpisodeSection(
        playLines = listOf(wrappedLine),
        selectedLineIndex = 0,
        playingState = playingState,
        sortState = SortState(
            sortList = listOf(
                SortBy<Episode>(
                    PlayLineWrapper.SORT_DEFAULT_KEY,
                    "默认",
                    compareBy(Episode::order),
                ),
            ),
            current = PlayLineWrapper.SORT_DEFAULT_KEY,
            isReverse = false,
        ),
        onEpisodeSelect = { _, _ -> onPlaybackClick() },
        onSort = {},
        onAllEpisodes = {},
        downloadSelection = selection,
        onDownloadEpisodeToggle = { selection = selection.toggle(it.id) },
        onDownloadSelectAll = { selection = selection.toggleAll(wrappedLine.sortedEpisodeList) },
        onDownloadCancel = {},
        onDownloadConfirm = { onSubmit(selection.resolve(wrappedLine.sortedEpisodeList)) },
    )
}

@Composable
private fun SortAndSelectionHarness(
    rawLines: List<PlayLine>,
    selectedLineId: String,
    selectedEpisode: Episode,
    sortOptions: List<SortBy<Episode>>,
) {
    var sortKey by remember { mutableStateOf(PlayLineWrapper.SORT_DEFAULT_KEY) }
    var isReverse by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    val comparator = sortOptions.first { it.id == sortKey }.comparator
    val playLines = rawLines.map { PlayLineWrapper(it, isReverse, comparator = comparator) }
    val selectedLineIndex = playLines.indexOfFirst { it.playLine.id == selectedLineId }
    val originalPlayingState = remember {
        CartoonPlayViewModel.CartoonPlayState(
            cartoonSummary = CartoonSummary("cartoon-1", "source-1"),
            playLine = PlayLineWrapper(
                rawLines.first { it.id == selectedLineId },
                comparator = sortOptions.first().comparator,
            ),
            episode = selectedEpisode,
        )
    }
    val sortState = SortState(sortOptions, sortKey, isReverse)

    Column {
        V2PlaySourceSection(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingLine = originalPlayingState.playLine,
            onLineSelect = {},
        )
        V2EpisodeSection(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingState = originalPlayingState,
            sortState = sortState,
            onEpisodeSelect = { _, _ -> },
            onSort = { showSort = true },
            onAllEpisodes = {},
        )
    }
    if (showSort) {
        EpisodeSortBottomSheet(
            sortState = sortState,
            onSortChange = { key, reverse ->
                sortKey = key
                isReverse = reverse
            },
            onDismiss = { showSort = false },
        )
    }
}

@Composable
private fun EpisodePickerHarness(
    rawLines: List<PlayLine>,
    sortOptions: List<SortBy<Episode>>,
    onSelected: (String, String) -> Unit,
) {
    var selectedLineIndex by remember { mutableStateOf(0) }
    var selectedEpisode by remember { mutableStateOf(rawLines.first().episode.first()) }
    var showPicker by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    val comparator = sortOptions.first().comparator
    val playLines = rawLines.map { PlayLineWrapper(it, comparator = comparator) }
    val playingState = CartoonPlayViewModel.CartoonPlayState(
        cartoonSummary = CartoonSummary("cartoon-1", "source-1"),
        playLine = playLines[selectedLineIndex],
        episode = selectedEpisode,
    )
    val sortState = SortState(sortOptions, PlayLineWrapper.SORT_DEFAULT_KEY, false)

    V2EpisodeSection(
        playLines = playLines,
        selectedLineIndex = selectedLineIndex,
        playingState = playingState,
        sortState = sortState,
        onEpisodeSelect = { line, episode ->
            selectedLineIndex = playLines.indexOfFirst { it.playLine.id == line.playLine.id }
            selectedEpisode = episode
            onSelected(line.playLine.id, episode.id)
        },
        onSort = { showSort = true },
        onAllEpisodes = { showPicker = true },
    )
    if (showPicker) {
        EpisodePickerBottomSheet(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingState = playingState,
            sortState = sortState,
            onLineSelect = { selectedLineIndex = it },
            onEpisodeSelect = { line, episode ->
                selectedLineIndex = playLines.indexOfFirst { it.playLine.id == line.playLine.id }
                selectedEpisode = episode
                onSelected(line.playLine.id, episode.id)
                showPicker = false
            },
            onSort = {
                showPicker = false
                showSort = true
            },
            onDismiss = { showPicker = false },
        )
    }
    if (showSort) {
        EpisodeSortBottomSheet(
            sortState = sortState,
            onSortChange = { _, _ -> },
            onDismiss = { showSort = false },
        )
    }
}
