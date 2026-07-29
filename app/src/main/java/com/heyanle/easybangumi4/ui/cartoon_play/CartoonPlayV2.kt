package com.heyanle.easybangumi4.ui.cartoon_play

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayPreferences
import com.heyanle.easybangumi4.danmaku.DanmakuEpisodeContext
import com.heyanle.easybangumi4.danmaku.DanmakuBangumiContext
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackStatus
import com.heyanle.easybangumi4.danmaku.DanmakuPlaybackViewModel
import com.heyanle.easybangumi4.navigationSearch
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.proc.SortColumn
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.inject.core.Inject
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase
import loli.ball.easyplayer2.EasyPlayerStateSync

internal object CartoonPlayV2TestTags {
    const val MEDIA_IDENTITY = "playback_media_identity"
    const val MEDIA_COLLAPSED = "playback_media_collapsed"
    const val MEDIA_EXPANDED = "playback_media_expanded"
    const val ACTIONS = "playback_actions"
    const val SORT_CONTROL = "playback_sort_control"
    const val SORT_SHEET = "playback_sort_sheet"
    const val EPISODE_PICKER = "playback_episode_picker"
    const val DOWNLOAD_SELECTION = "playback_download_selection"
    const val DOWNLOAD_SELECT_ALL = "playback_download_select_all"
    const val DOWNLOAD_CONFIRM = "playback_download_confirm"
    const val DOWNLOAD_CANCEL = "playback_download_cancel"

    fun action(label: String): String = "playback_action_$label"
    fun source(id: String): String = "playback_source_$id"
    fun episode(id: String): String = "playback_episode_$id"
}

/**
 * Resolves the only local coordinate used by automatic danmaku episode matching.
 * The stable IDs locate the latest projection; neither Episode metadata nor the source order is
 * exposed to the matching policy.
 */
internal fun resolveDanmakuSortedEpisodePosition(
    playLines: List<PlayLineWrapper>,
    playLineId: String,
    episodeId: String,
): Int {
    val line = playLines.firstOrNull { it.playLine.id == playLineId } ?: return 0
    return line.sortedEpisodeList
        .indexOfFirst { it.id == episodeId }
        .takeIf { it >= 0 }
        ?.plus(1)
        ?: 0
}

/**
 * Transient selection owned by the playback detail UI.
 *
 * Download selection deliberately stays separate from the playing episode: selecting an item for
 * download must never switch playback, and sorting may recreate [PlayLineWrapper] instances. Stable
 * source episode IDs therefore form the selection key.
 */
internal data class DownloadEpisodeSelection(
    val lineId: String,
    val episodeIds: Set<String> = emptySet(),
) {
    fun toggle(episodeId: String): DownloadEpisodeSelection = copy(
        episodeIds = if (episodeId in episodeIds) {
            episodeIds - episodeId
        } else {
            episodeIds + episodeId
        },
    )

    fun toggleAll(episodes: List<Episode>): DownloadEpisodeSelection {
        val allIds = episodes.mapTo(linkedSetOf(), Episode::id)
        return copy(episodeIds = if (allIds.isNotEmpty() && allIds.all(episodeIds::contains)) emptySet() else allIds)
    }

    fun resolve(episodes: List<Episode>): List<Episode> = episodes.filter { it.id in episodeIds }
}

private data class EpisodeRailItem(
    val episode: Episode,
    val selected: Boolean,
    val selectionMode: Boolean,
)

/**
 * The new playback-detail route. It intentionally lives beside [CartoonPlay] instead of changing
 * the legacy page, so that the former UI remains an independent rollback target.
 */
@OptIn(UnstableApi::class)
@Composable
fun CartoonPlayV2(
    id: String,
    source: String,
    enterData: CartoonPlayViewModel.EnterData? = null,
) {
    val summary = remember(id, source) { CartoonSummary(id, source) }
    val detailedVM = viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary))
    val playVM = viewModel<CartoonPlayViewModel>(factory = CartoonPlayViewModelFactory(enterData))
    val playingVM = viewModel<CartoonPlayingViewModel>()
    val danmakuVM = viewModel<DanmakuPlaybackViewModel>()
    val danmakuDisplayPreferences: DanmakuDisplayPreferences by Inject.injectLazy()
    val isPad = isCurPadeMode()
    val controlVM = ControlViewModelFactory.viewModel(
        playingVM.exoPlayer,
        isPad,
        render = playingVM.easyTextRenderer,
    )
    val detailState by detailedVM.stateFlow.collectAsState()
    val playState by playVM.curringPlayState.collectAsState()
    val playingState by playingVM.playingState.collectAsState()
    val danmakuState by danmakuVM.state.collectAsState()
    val danmakuDisplayConfig by remember(danmakuDisplayPreferences) {
        danmakuDisplayPreferences.configFlow()
    }.collectAsState(danmakuDisplayPreferences.getConfig())

    EasyPlayerStateSync(controlVM)

    LaunchedEffect(detailState.cartoonInfo) {
        detailState.cartoonInfo?.let(playVM::onCartoonInfoChange)
    }
    LaunchedEffect(playState) {
        playingVM.changePlay(playState, playVM.adviceProgress)
        playVM.adviceProgress = -1L
    }
    LaunchedEffect(
        detailState.cartoonInfo?.id,
        detailState.cartoonInfo?.source,
        detailState.cartoonInfo?.name,
    ) {
        danmakuVM.onBangumiDetailAvailable(
            detailState.cartoonInfo?.let {
                DanmakuBangumiContext(
                    cartoonId = it.id,
                    cartoonSourceId = it.source,
                    title = it.name,
                )
            },
        )
    }
    LaunchedEffect(
        playState?.cartoonSummary,
        playState?.playLine?.playLine?.id,
        playState?.episode?.id,
        detailState.cartoonInfo?.id,
        detailState.cartoonInfo?.source,
    ) {
        val cartoon = detailState.cartoonInfo
        val current = playState
        val sortedPosition = if (cartoon == null || current == null) {
            0
        } else {
            resolveDanmakuSortedEpisodePosition(
                playLines = cartoon.playLineWrapper,
                playLineId = current.playLine.playLine.id,
                episodeId = current.episode.id,
            )
        }
        danmakuVM.onPlaybackTargetChanged(
            if (cartoon == null || current == null) null else DanmakuEpisodeContext(
                playbackKey = com.heyanle.easybangumi4.danmaku.DanmakuPlaybackKey(
                    cartoonId = cartoon.id,
                    cartoonSourceId = cartoon.source,
                    playLineId = current.playLine.playLine.id,
                    episodeId = current.episode.id,
                ),
                sortedEpisodePosition = sortedPosition,
            ),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            DetailedContainer(sourceKey = source) { _, _, _ ->
                CartoonPlayV2Content(
                    isPad = isPad,
                    controlVM = controlVM,
                    detailedVM = detailedVM,
                    playVM = playVM,
                    playingVM = playingVM,
                    detailState = detailState,
                    playState = playState,
                    playingState = playingState,
                    danmakuState = danmakuState,
                    danmakuDisplayConfig = danmakuDisplayConfig,
                    onDanmakuDisplayConfigChange = danmakuDisplayPreferences::setConfig,
                    onResetDanmakuDisplayConfig = danmakuDisplayPreferences::resetToDefaults,
                    onManualMatch = danmakuVM::beginManualMatch,
                    onDanmakuRetry = danmakuVM::retry,
                    onManualQueryChange = danmakuVM::updateManualQuery,
                    onManualSearch = danmakuVM::searchManualMatch,
                    onManualBangumiSelect = danmakuVM::selectManualBangumi,
                    onManualEpisodeSelect = danmakuVM::selectManualEpisode,
                    onBackToBangumiSelection = danmakuVM::showBangumiSelection,
                    onManualDismiss = danmakuVM::dismissManualMatch,
                )
            }
        }

        CartoonRecordedHost(
            controlViewModel = controlVM,
            recording = playingVM.showRecording.value,
            onDismissRequest = { playingVM.showRecording.value = null },
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun CartoonPlayV2Content(
    isPad: Boolean,
    controlVM: ControlViewModel,
    detailedVM: DetailedViewModel,
    playVM: CartoonPlayViewModel,
    playingVM: CartoonPlayingViewModel,
    detailState: DetailedViewModel.DetailState,
    playState: CartoonPlayViewModel.CartoonPlayState?,
    playingState: CartoonPlayingViewModel.PlayingState,
    danmakuState: DanmakuPlaybackState,
    danmakuDisplayConfig: DanmakuDisplayConfig,
    onDanmakuDisplayConfigChange: (DanmakuDisplayConfig) -> Unit,
    onResetDanmakuDisplayConfig: () -> Unit,
    onManualMatch: () -> Unit,
    onDanmakuRetry: () -> Unit,
    onManualQueryChange: (String) -> Unit,
    onManualSearch: () -> Unit,
    onManualBangumiSelect: (com.heyanle.easybangumi4.danmaku.DanmakuBangumi) -> Unit,
    onManualEpisodeSelect: (com.heyanle.easybangumi4.danmaku.DanmakuEpisode) -> Unit,
    onBackToBangumiSelection: () -> Unit,
    onManualDismiss: () -> Unit,
) {
    val settingPreferences: SettingPreferences by Inject.injectLazy()
    val nav = LocalNavController.current
    val activity = LocalContext.current as Activity
    val showSpeed = remember { mutableStateOf(false) }
    val showEpisodeWindow = remember { mutableStateOf(false) }
    val showScaleType = remember { mutableStateOf(false) }
    var showPlayerSettings by rememberSaveable { mutableStateOf(false) }
    var playerSettingsSection by rememberSaveable {
        mutableStateOf(PlayerSettingsSection.Danmaku)
    }
    val openPlayerSettings: (PlayerSettingsSection?) -> Unit = { section ->
        section?.let { playerSettingsSection = it }
        showPlayerSettings = true
    }
    val videoScaleType by playingVM.videoScaleType.collectAsState()
    var downloadRequest by remember { mutableStateOf<Triple<CartoonInfo, PlayLineWrapper, List<Episode>>?>(null) }
    // Own the native danmaku view at the page level. AndroidView holders are allowed to come and
    // go during configuration changes without releasing the DFM clock or its prepared item set.
    val danmakuRenderer = remember { DfmDanmakuRenderer() }

    DisposableEffect(Unit) {
        onDispose { playingVM.onExit() }
    }
    DisposableEffect(danmakuRenderer) {
        onDispose { danmakuRenderer.release() }
    }
    LaunchedEffect(playState, detailState.cartoonInfo) {
        controlVM.title = if (playState == null || detailState.cartoonInfo == null) "" else {
            "${detailState.cartoonInfo.name} - ${playState.episode.label}"
        }
    }

    EasyPlayerScaffoldBase(
        modifier = Modifier
            .fillMaxSize()
            .let { if (settingPreferences.playerBottomNavigationBarPadding.get()) it.navigationBarsPadding() else it },
        needSync = false,
        vm = controlVM,
        isPadMode = isPad,
        contentWeight = 0.5f,
        videoFloat = {
            playState?.let {
                VideoFloat(
                    cartoonPlayingViewModel = playingVM,
                    cartoonPlayViewModel = playVM,
                    playingState = playingState,
                    playState = it,
                    controlVM = controlVM,
                    showSpeedWin = showSpeed,
                    showEpisodeWin = showEpisodeWindow,
                    showScaleTypeWin = showScaleType,
                )
            }
        },
        control = {
            val matched = danmakuState.status as? DanmakuPlaybackStatus.Matched
            val openDanmakuSourceSettings = {
                showPlayerSettings = false
                if (controlVM.isFullScreen) {
                    controlVM.onFullScreen(
                        fullScreen = false,
                        reverse = false,
                        ctx = activity,
                    )
                }
                nav.navigationSetting(SettingPage.DanmakuSource)
            }
            val danmakuControlState = danmakuState.toPlayerDanmakuControlState(
                displayEnabled = danmakuDisplayConfig.enabled,
                onToggleDisplay = {
                    onDanmakuDisplayConfigChange(
                        danmakuDisplayConfig.copy(enabled = it),
                    )
                },
                onManualMatch = onManualMatch,
                onRetry = onDanmakuRetry,
                onOpenSourceSettings = openDanmakuSourceSettings,
            )
            Box(Modifier.fillMaxSize()) {
                DfmDanmakuOverlay(
                    renderer = danmakuRenderer,
                    player = playingVM.exoPlayer,
                    comments = matched?.comments.orEmpty(),
                    bindingOffsetMillis = matched?.binding?.timeOffsetMillis ?: 0L,
                    displayConfig = danmakuDisplayConfig,
                )
                VideoControl(
                    controlVM = controlVM,
                    cartoonPlayingVM = playingVM,
                    cartoonPlayVM = playVM,
                    playingState = playingState,
                    detailState = detailState,
                    sourcePlayState = playState,
                    showSpeedWin = showSpeed,
                    showEpisodeWin = showEpisodeWindow,
                    showVideoScaleTypeWin = showScaleType,
                    danmakuControlState = danmakuControlState,
                    onShowPlayerSettings = {
                        // “更多”没有预设的配置类别，重开时延续用户上一次浏览的 Tab。
                        openPlayerSettings(null)
                    },
                )
                AdaptivePlayerSettingsPanel(
                    visible = showPlayerSettings,
                    selectedSection = playerSettingsSection,
                    onSectionSelected = { playerSettingsSection = it },
                    onDismiss = { showPlayerSettings = false },
                    danmakuConfig = danmakuDisplayConfig,
                    danmakuSummary = matched?.let {
                        buildString {
                            append(danmakuState.source?.displayName ?: "当前弹幕源")
                            append(" · ")
                            append(it.comments.size)
                            append(" 条")
                        }
                    },
                    onDanmakuConfigChange = onDanmakuDisplayConfigChange,
                    onResetDanmaku = onResetDanmakuDisplayConfig,
                    videoScaleType = videoScaleType,
                    videoScaleOptions = playingVM.videoScaleTypeSelection,
                    onVideoScaleSelected = playingVM::setVideoScaleType,
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlaybackProgressDivider(
                positionMillis = controlVM.position,
                durationMillis = controlVM.during,
            )
            Box(modifier = Modifier.weight(1f)) {
                when {
                    detailState.isLoading -> LoadingPage(modifier = Modifier.fillMaxSize())
                    detailState.isError || detailState.cartoonInfo == null -> ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        errorMsg = detailState.errorMsg.ifBlank { "加载播放详情失败" },
                        clickEnable = true,
                        onClick = detailedVM::load,
                        other = { Text("点击重试") },
                    )

                    else -> {
                        val sortState by detailedVM.sortStateFlow.collectAsState()
                        val playLines = detailState.cartoonInfo.playLineWrapper
                        CartoonPlayV2Detail(
                            cartoon = detailState.cartoonInfo,
                            playLines = playLines,
                            selectedLineIndex = playVM.resolveSelectedLineIndex(playLines),
                            playingState = playState,
                            sortState = sortState,
                            danmakuState = danmakuState,
                            onLineSelect = { playVM.selectLine(playLines, it) },
                            onEpisodeSelect = { line, episode -> playVM.changePlay(detailState.cartoonInfo, line, episode) },
                            onStar = { detailedVM.setCartoonStar(it, detailState.cartoonInfo) },
                            onSearch = {
                                nav.navigationSearch(detailState.cartoonInfo.name, detailState.cartoonInfo.source)
                            },
                            onWeb = { detailState.cartoonInfo.url.takeIf(String::isNotBlank)?.openUrl() },
                            onDownload = { line, episodes ->
                                downloadRequest = Triple(detailState.cartoonInfo, line, episodes)
                            },
                            onExternalPlay = playingVM::playCurrentExternal,
                            onSortChange = { key, reverse ->
                                detailedVM.setCartoonSort(key, reverse, detailState.cartoonInfo)
                            },
                            onManualMatch = onManualMatch,
                            onDanmakuRetry = onDanmakuRetry,
                            onOpenDanmakuSettings = {
                                openPlayerSettings(PlayerSettingsSection.Danmaku)
                            },
                        )
                    }
                }
            }
        }
    }

    downloadRequest?.let { request ->
        CartoonDownloadDialog(
            cartoonInfo = request.first,
            playerLineWrapper = request.second,
            episodes = request.third,
            onDismissRequest = { downloadRequest = null },
        )
    }
    danmakuState.manualMatch?.takeIf { danmakuState.isManualMatchVisible }?.let { manual ->
        DanmakuMatchBottomSheet(
            state = manual,
            onQueryChange = onManualQueryChange,
            onSearch = onManualSearch,
            onBangumiSelect = onManualBangumiSelect,
            onEpisodeSelect = onManualEpisodeSelect,
            onBackToBangumiSelection = onBackToBangumiSelection,
            onDismiss = onManualDismiss,
        )
    }
}

@Composable
private fun CartoonPlayV2Detail(
    cartoon: CartoonInfo,
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingState: CartoonPlayViewModel.CartoonPlayState?,
    sortState: SortState<Episode>,
    danmakuState: com.heyanle.easybangumi4.danmaku.DanmakuPlaybackState,
    onLineSelect: (Int) -> Unit,
    onEpisodeSelect: (PlayLineWrapper, Episode) -> Unit,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDownload: (PlayLineWrapper, List<Episode>) -> Unit,
    onExternalPlay: () -> Unit,
    onSortChange: (String, Boolean) -> Unit,
    onManualMatch: () -> Unit,
    onDanmakuRetry: () -> Unit,
    onOpenDanmakuSettings: () -> Unit,
) {
    var showAllEpisodes by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var downloadSelection by remember(cartoon.id, cartoon.source) {
        mutableStateOf<DownloadEpisodeSelection?>(null)
    }

    BackHandler(enabled = downloadSelection != null) {
        downloadSelection = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            V2MediaIdentity(cartoon)
        }
        item {
            Column {
                V2ActionRow(
                    isStar = cartoon.starTime > 0,
                    onStar = onStar,
                    onSearch = onSearch,
                    onWeb = onWeb,
                    onDownload = {
                        playLines.getOrNull(selectedLineIndex)?.let { selectedLine ->
                            downloadSelection = if (downloadSelection?.lineId == selectedLine.playLine.id) {
                                null
                            } else {
                                DownloadEpisodeSelection(lineId = selectedLine.playLine.id)
                            }
                        }
                    },
                    isSelectingDownloads = downloadSelection != null,
                    onExternalPlay = onExternalPlay,
                )
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
            }
        }
        item {
            V2PlaySourceSection(
                playLines = playLines,
                selectedLineIndex = selectedLineIndex,
                playingLine = playingState?.playLine,
                onLineSelect = { index ->
                    downloadSelection = null
                    onLineSelect(index)
                },
            )
        }
        item {
            val selectedLine = playLines.getOrNull(selectedLineIndex)
            val activeDownloadSelection = downloadSelection?.takeIf {
                it.lineId == selectedLine?.playLine?.id
            }
            V2EpisodeSection(
                playLines = playLines,
                selectedLineIndex = selectedLineIndex,
                playingState = playingState,
                sortState = sortState,
                onEpisodeSelect = onEpisodeSelect,
                onSort = { showSort = true },
                onAllEpisodes = { showAllEpisodes = true },
                downloadSelection = activeDownloadSelection,
                onDownloadEpisodeToggle = { episode ->
                    downloadSelection = activeDownloadSelection?.toggle(episode.id)
                },
                onDownloadSelectAll = {
                    selectedLine?.let { line ->
                        downloadSelection = activeDownloadSelection?.toggleAll(line.sortedEpisodeList)
                    }
                },
                onDownloadCancel = { downloadSelection = null },
                onDownloadConfirm = {
                    val episodes = selectedLine?.let { line ->
                        activeDownloadSelection?.resolve(line.sortedEpisodeList)
                    }.orEmpty()
                    if (selectedLine != null && episodes.isNotEmpty()) {
                        downloadSelection = null
                        onDownload(selectedLine, episodes)
                    }
                },
            )
        }
        item {
            DanmakuSection(
                modifier = Modifier.padding(horizontal = 20.dp),
                state = danmakuState,
                onManualMatch = onManualMatch,
                onRetry = onDanmakuRetry,
                onOpenDisplaySettings = onOpenDanmakuSettings,
            )
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
    if (showAllEpisodes) {
        EpisodePickerBottomSheet(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingState = playingState,
            sortState = sortState,
            onLineSelect = onLineSelect,
            onEpisodeSelect = { line, episode ->
                onEpisodeSelect(line, episode)
                showAllEpisodes = false
            },
            onSort = {
                showAllEpisodes = false
                showSort = true
            },
            onDismiss = { showAllEpisodes = false },
        )
    }
    if (showSort) {
        EpisodeSortBottomSheet(
            sortState = sortState,
            onSortChange = onSortChange,
            onDismiss = { showSort = false },
        )
    }
}

@Composable
internal fun V2MediaIdentity(cartoon: CartoonInfo) {
    var expanded by rememberSaveable(cartoon.id, cartoon.source) { mutableStateOf(false) }
    val synopsis = cartoon.description.ifBlank { cartoon.intro }
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CartoonPlayV2TestTags.MEDIA_IDENTITY)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { expanded = !expanded },
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, delayMillis = 300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            label = "media_details",
        ) { showFullDetails ->
            Row(
                modifier = Modifier.testTag(
                    if (showFullDetails) CartoonPlayV2TestTags.MEDIA_EXPANDED
                    else CartoonPlayV2TestTags.MEDIA_COLLAPSED,
                ),
                verticalAlignment = Alignment.Top,
            ) {
                OkImage(
                    modifier = Modifier
                        .width(if (showFullDetails) 100.dp else 96.dp)
                        .aspectRatio(if (showFullDetails) 0.68f else 19f / 13.5f)
                        .clip(RoundedCornerShape(if (showFullDetails) 16.dp else 12.dp)),
                    image = cartoon.coverUrl,
                    contentDescription = cartoon.name,
                    errorRes = R.drawable.placeholder,
                )
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(
                        if (showFullDetails) 8.dp else 4.dp,
                    ),
                ) {
                    Text(
                        text = cartoon.name,
                        maxLines = if (showFullDetails) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                        style = if (showFullDetails) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (showFullDetails && cartoon.genres.isNotEmpty()) {
                        Text(
                            cartoon.genres.joinToString(" · "),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    if (synopsis.isNotBlank()) {
                        Text(
                            text = synopsis,
                            maxLines = if (showFullDetails) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = if (showFullDetails) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.bodySmall
                            },
                        )
                    }
                }
            }
        }
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "收起详情" else "展开详情",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlaybackProgressDivider(positionMillis: Long, durationMillis: Long) {
    val targetProgress = if (durationMillis > 0L) {
        (positionMillis.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 450),
        label = "playback_progress_divider",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
internal fun V2ActionRow(
    isStar: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onWeb: () -> Unit,
    onDownload: () -> Unit,
    isSelectingDownloads: Boolean = false,
    onExternalPlay: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .testTag(CartoonPlayV2TestTags.ACTIONS),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        V2Action(
            "追番",
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline,
            selected = isStar,
            onClick = { onStar(!isStar) },
        )
        V2Action("搜索", Icons.Filled.Search, onClick = onSearch)
        V2Action("网站", Icons.Filled.Language, onClick = onWeb)
        V2Action("下载", Icons.Filled.Download, selected = isSelectingDownloads, onClick = onDownload)
        V2Action("外部播放", Icons.Filled.ScreenShare, onClick = onExternalPlay)
    }
}

@Composable
private fun RowScope.V2Action(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .testTag(CartoonPlayV2TestTags.action(label))
            .clip(RoundedCornerShape(18.dp))
            .semantics {
                selected?.let {
                    this.selected = it
                    stateDescription = if (it) "已开启" else "未开启"
                }
            }
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            color = if (selected == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected == true) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun playSourceChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
)

@Composable
internal fun V2PlaySourceSection(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingLine: PlayLineWrapper?,
    onLineSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp),
            text = "播放源",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(playLines, key = { _, line -> line.playLine.id }) { index, line ->
                val selected = index == selectedLineIndex
                FilterChip(
                    modifier = Modifier.testTag(CartoonPlayV2TestTags.source(line.playLine.id)),
                    selected = selected,
                    onClick = { onLineSelect(index) },
                    colors = playSourceChipColors(),
                    label = {
                        Text(
                            if (line.playLine.id == playingLine?.playLine?.id) {
                                "${line.playLine.label} · 播放中"
                            } else {
                                line.playLine.label
                            },
                            maxLines = 1,
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun V2EpisodeSection(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingState: CartoonPlayViewModel.CartoonPlayState?,
    sortState: SortState<Episode>,
    onEpisodeSelect: (PlayLineWrapper, Episode) -> Unit,
    onSort: () -> Unit,
    onAllEpisodes: () -> Unit,
    downloadSelection: DownloadEpisodeSelection? = null,
    onDownloadEpisodeToggle: (Episode) -> Unit = {},
    onDownloadSelectAll: () -> Unit = {},
    onDownloadCancel: () -> Unit = {},
    onDownloadConfirm: () -> Unit = {},
) {
    val selectedLine = playLines.getOrNull(selectedLineIndex)
    // Lazy layouts may skip an item when its Episode instance is unchanged. Model selection as an
    // explicit item property so only the affected episode is recomposed without resetting scroll.
    val episodeItems = selectedLine?.sortedEpisodeList.orEmpty().map { episode ->
        EpisodeRailItem(
            episode = episode,
            selected = if (downloadSelection == null) {
                playingState?.playLine?.playLine?.id == selectedLine?.playLine?.id &&
                    playingState?.episode?.id == episode.id
            } else {
                episode.id in downloadSelection.episodeIds
            },
            selectionMode = downloadSelection != null,
        )
    }
    // Item click lambdas are retained independently of the parent composition.
    val currentOnEpisodeSelect = rememberUpdatedState(onEpisodeSelect)
    val currentOnDownloadEpisodeToggle = rememberUpdatedState(onDownloadEpisodeToggle)
    val episodeListState = rememberLazyListState()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (downloadSelection == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("选集", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextButton(
                    modifier = Modifier.testTag(CartoonPlayV2TestTags.SORT_CONTROL),
                    onClick = onSort,
                ) {
                    Icon(Icons.Filled.Sort, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    val sortLabel = sortState.sortList.firstOrNull { it.id == sortState.current }?.label ?: "默认"
                    Text("$sortLabel${if (sortState.isReverse) " · 倒序" else ""}")
                }
                TextButton(onClick = onAllEpisodes) { Text("全部选集") }
            }
        } else {
            val selectedCount = downloadSelection.episodeIds.size
            val episodeCount = selectedLine?.sortedEpisodeList?.size ?: 0
            val allSelected = episodeCount > 0 && selectedCount == episodeCount
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag(CartoonPlayV2TestTags.DOWNLOAD_SELECTION),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "选择下载剧集",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "已选择 $selectedCount 集",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        modifier = Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_CANCEL),
                        onClick = onDownloadCancel,
                    ) {
                        Text("取消")
                    }
                    FilledTonalButton(
                        modifier = Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_CONFIRM),
                        enabled = selectedCount > 0,
                        onClick = onDownloadConfirm,
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("下载")
                    }
                }
                TextButton(
                    modifier = Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_SELECT_ALL),
                    onClick = onDownloadSelectAll,
                ) {
                    Icon(Icons.Filled.SelectAll, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (allSelected) "取消全选" else "全选")
                }
            }
        }
        if (selectedLine == null) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "暂无可播放剧集",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            key(episodeItems.map(EpisodeRailItem::selected)) {
                LazyRow(
                    state = episodeListState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(episodeItems, key = { it.episode.id }) { item ->
                        EpisodeRailButton(
                            episode = item.episode,
                            selected = item.selected,
                            selectionMode = item.selectionMode,
                            onClick = {
                                if (!item.selectionMode) {
                                    currentOnEpisodeSelect.value(selectedLine, item.episode)
                                } else {
                                    currentOnDownloadEpisodeToggle.value(item.episode)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EpisodeRailButton(
    episode: Episode,
    selected: Boolean,
    selectionMode: Boolean = false,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    onClick: () -> Unit,
) {
    val selectedShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 72.dp, max = 160.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else unselectedContainerColor,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = selectedShape,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .testTag(CartoonPlayV2TestTags.episode(episode.id))
                .selectable(
                    selected = selected,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    stateDescription = when {
                        selectionMode && selected -> "已选中，待下载"
                        selectionMode -> "未选中"
                        selected -> "当前播放"
                        else -> "未播放"
                    }
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (selected && selectionMode) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                )
                Spacer(Modifier.width(7.dp))
            }
            Text(
                text = episode.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodePickerBottomSheet(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingState: CartoonPlayViewModel.CartoonPlayState?,
    sortState: SortState<Episode>,
    onLineSelect: (Int) -> Unit,
    onEpisodeSelect: (PlayLineWrapper, Episode) -> Unit,
    onSort: () -> Unit,
    onDismiss: () -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val selectedLine = playLines.getOrNull(selectedLineIndex)
    val allEpisodes = selectedLine?.sortedEpisodeList.orEmpty()
    val episodes = allEpisodes.filter { it.label.contains(keyword, ignoreCase = true) }
    LaunchedEffect(selectedLine?.playLine?.id, playingState?.episode?.id) {
        val index = allEpisodes.indexOfFirst { it.id == playingState?.episode?.id }
        if (index >= 0) gridState.scrollToItem(index)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CartoonPlayV2TestTags.EPISODE_PICKER),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("全部选集", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onSort) {
                    Icon(Icons.Filled.Sort, contentDescription = "切换排序")
                }
            }
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "${sortState.sortList.find { it.id == sortState.current }?.label ?: "默认"} · ${if (sortState.isReverse) "倒序" else "正序"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                modifier = Modifier.padding(top = 12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(playLines, key = { _, line -> line.playLine.id }) { index, line ->
                    FilterChip(
                        selected = index == selectedLineIndex,
                        onClick = { onLineSelect(index) },
                        colors = playSourceChipColors(),
                        label = { Text(line.playLine.label) },
                    )
                }
            }
            androidx.compose.material3.OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("搜索剧集") },
                singleLine = true,
            )
            LazyVerticalGrid(
                modifier = Modifier.height(450.dp).padding(horizontal = 20.dp),
                columns = GridCells.Adaptive(100.dp),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(episodes.size, key = { episodes[it].id }) { index ->
                    val episode = episodes[index]
                    EpisodeRailButton(
                        episode = episode,
                        selected = playingState?.playLine?.playLine?.id == selectedLine?.playLine?.id &&
                            playingState?.episode?.id == episode.id,
                        unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        onClick = { selectedLine?.let { onEpisodeSelect(it, episode) } },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeSortBottomSheet(
    sortState: SortState<Episode>,
    onSortChange: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectedSortLabel = sortState.sortList
        .firstOrNull { it.id == sortState.current }
        ?.label
        ?: "默认"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .testTag(CartoonPlayV2TestTags.SORT_SHEET)
                .semantics {
                    stateDescription = "$selectedSortLabel，${if (sortState.isReverse) "倒序" else "正序"}"
                },
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                text = "选集排序",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            SortColumn(
                sortState = sortState,
                onClick = { item, state ->
                    onSortChange(
                        item.id,
                        state == SortState.STATUS_ON,
                    )
                },
            )
        }
    }
}
