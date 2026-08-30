@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.heyanle.easybangumi4.v2.ui.playback

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
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.heyanle.easybangumi4.navigationDlna
import com.heyanle.easybangumi4.navigationSetting
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.cartoon_play.AdaptivePlayerSettingsPanel
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonDownloadDialog
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonRecordedHost
import com.heyanle.easybangumi4.ui.cartoon_play.DanmakuMatchBottomSheet
import com.heyanle.easybangumi4.ui.cartoon_play.DfmDanmakuOverlay
import com.heyanle.easybangumi4.ui.cartoon_play.DfmDanmakuRenderer
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerSettingsSection
import com.heyanle.easybangumi4.ui.cartoon_play.VideoControl
import com.heyanle.easybangumi4.ui.cartoon_play.VideoFloat
import com.heyanle.easybangumi4.ui.cartoon_play.toPlayerDanmakuControlState
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.DetailedViewModelFactory
import com.heyanle.easybangumi4.ui.common.DetailedContainer
import com.heyanle.easybangumi4.ui.common.EasyMutiSelectionDialog
import com.heyanle.easybangumi4.ui.common.ErrorPage
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.ui.common.proc.SortColumn
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.ui.setting.SettingPage
import com.heyanle.easybangumi4.utils.isCurPadeMode
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.downloadImage
import com.heyanle.easybangumi4.utils.MediaAndroidUtils
import com.heyanle.easybangumi4.utils.shareText
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.inject.core.Inject
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.ControlViewModelFactory
import loli.ball.easyplayer2.EasyPlayerScaffoldBase
import loli.ball.easyplayer2.EasyPlayerStateSync
import kotlinx.coroutines.launch

internal object CartoonPlayV2TestTags {
    const val MEDIA_IDENTITY = "playback_media_identity"
    const val MEDIA_COLLAPSED = "playback_media_collapsed"
    const val MEDIA_EXPANDED = "playback_media_expanded"
    const val ACTIONS = "playback_actions"
    const val MORE_ACTIONS = "playback_more_actions"
    const val SORT_CONTROL = "playback_sort_control"
    const val SORT_SHEET = "playback_sort_sheet"
    const val EPISODE_PICKER = "playback_episode_picker"
    const val LINE_PICKER = "playback_line_picker"
    const val DANMAKU_PANEL = "playback_danmaku_panel"
    const val DOWNLOAD_SELECTION = "playback_download_selection"
    const val DOWNLOAD_SELECT_ALL = "playback_download_select_all"
    const val DOWNLOAD_CONFIRM = "playback_download_confirm"
    const val DOWNLOAD_CANCEL = "playback_download_cancel"

    fun action(label: String): String = "playback_action_$label"
    fun source(id: String): String = "playback_source_$id"
    fun episode(id: String): String = "playback_episode_$id"
}

@Composable
private fun playbackDetailColorScheme() = darkColorScheme(
    primary = V2Theme.colors.immersiveAccent,
    onPrimary = V2Theme.colors.onImmersiveAccent,
    primaryContainer = V2Theme.colors.immersiveAccentContainer,
    onPrimaryContainer = V2Theme.colors.onImmersiveAccentContainer,
    secondary = V2Theme.colors.immersiveAccent,
    onSecondary = V2Theme.colors.onImmersiveAccent,
    secondaryContainer = V2Theme.colors.immersiveAccentContainer,
    onSecondaryContainer = V2Theme.colors.onImmersiveAccentContainer,
    background = V2Tokens.PlayerDark,
    onBackground = Color.White,
    surface = V2Tokens.PlayerDark,
    onSurface = Color.White,
    surfaceVariant = V2Tokens.PlayerSurface,
    onSurfaceVariant = V2Tokens.PlayerTextSecondary,
    outline = V2Tokens.PlayerDivider,
    error = V2Tokens.PlayerError,
)

@Composable
private fun playbackSheetColorScheme() = darkColorScheme(
    primary = V2Theme.colors.immersiveAccent,
    onPrimary = V2Theme.colors.onImmersiveAccent,
    primaryContainer = V2Theme.colors.immersiveAccentContainer,
    onPrimaryContainer = V2Theme.colors.onImmersiveAccentContainer,
    secondary = V2Theme.colors.immersiveAccent,
    onSecondary = V2Theme.colors.onImmersiveAccent,
    secondaryContainer = V2Theme.colors.immersiveAccentContainer,
    onSecondaryContainer = V2Theme.colors.onImmersiveAccentContainer,
    background = V2Tokens.PlayerDark,
    onBackground = V2Tokens.PlayerTextPrimary,
    surface = V2Tokens.PlayerSurface,
    onSurface = V2Tokens.PlayerTextPrimary,
    surfaceVariant = V2Tokens.PlayerSurfaceMuted,
    onSurfaceVariant = V2Tokens.PlayerTextSecondary,
    outline = V2Tokens.PlayerDivider,
    error = V2Tokens.PlayerError,
)

@Composable
private fun playbackTypography() = MaterialTheme.typography.copy(
    headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 17.sp),
)

@Composable
private fun PlaybackSheetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = playbackSheetColorScheme(),
        typography = playbackTypography(),
        content = content,
    )
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

/**
 * The new playback-detail route. It intentionally lives beside [CartoonPlay] instead of changing
 * the legacy page, so that the former UI remains an independent rollback target.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlaybackDetailV2(
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
        playingVM.playerController,
        isPad,
        render = playingVM.render,
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
    LaunchedEffect(
        playState?.cartoonSummary?.id,
        playState?.cartoonSummary?.source,
        playState?.playLine?.playLine?.id,
        playState?.episode?.id,
    ) {
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

    MaterialTheme(
        colorScheme = playbackDetailColorScheme(),
        typography = playbackTypography(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = V2Tokens.PlayerDark,
                contentColor = Color.White,
            ) {
                DetailedContainer(sourceKey = source) { _, _, _ ->
                    PlaybackDetailV2Content(
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
}

@OptIn(UnstableApi::class)
@Composable
private fun PlaybackDetailV2Content(
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
    val mpvAnime4kEnabled by playingVM.mpvAnime4kEnabled.collectAsState()
    val mpvAnime4kPreset by playingVM.mpvAnime4kPreset.collectAsState()
    val mpvAnime4kStatus by playingVM.mpvAnime4KStatus.collectAsState()
    val exoAdAudioProbeEnabled by playingVM.exoAdAudioProbeEnabled.collectAsState()
    val exoAdAudioProbeRulesUrl by playingVM.exoAdAudioProbeRulesUrl.collectAsState()
    val orientationMode by settingPreferences.playerOrientationMode.flow().collectAsState(
        initial = settingPreferences.playerOrientationMode.get(),
    )
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
    LaunchedEffect(playState, detailState.cartoonInfo, controlVM) {
        controlVM.title = if (playState == null || detailState.cartoonInfo == null) "" else {
            "${detailState.cartoonInfo.name} - ${playState.episode.label}"
        }
    }
    LaunchedEffect(orientationMode, controlVM) {
        controlVM.orientationEnableMode = when (orientationMode) {
            SettingPreferences.PlayerOrientationMode.Auto -> ControlViewModel.OrientationEnableMode.AUTO
            SettingPreferences.PlayerOrientationMode.Enable -> ControlViewModel.OrientationEnableMode.ENABLE
            SettingPreferences.PlayerOrientationMode.Disable -> ControlViewModel.OrientationEnableMode.DISABLE
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
                    useNormalSpeedBottomSheet = true,
                )
            }
        },
        control = {
            val matched = danmakuState.status as? DanmakuPlaybackStatus.Matched
            val openManualDanmakuMatch = {
                showEpisodeWindow.value = false
                showSpeed.value = false
                showScaleType.value = false
                showPlayerSettings = false
                onManualMatch()
            }
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
                onManualMatch = openManualDanmakuMatch,
                onRetry = onDanmakuRetry,
                onOpenSourceSettings = openDanmakuSourceSettings,
            )
            Box(Modifier.fillMaxSize()) {
                DfmDanmakuOverlay(
                    renderer = danmakuRenderer,
                    player = playingVM.playerController,
                    comments = matched?.comments.orEmpty(),
                    bindingOffsetMillis = matched?.binding?.timeOffsetMillis ?: 0L,
                    displayConfig = danmakuDisplayConfig,
                    isFullScreen = controlVM.isFullScreen,
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
                    danmakuRenderer = danmakuRenderer,
                    includeDanmakuInScreenshot = danmakuDisplayConfig.enabled,
                    showNormalCastAndShare = false,
                    showNormalSpeedInTopBar = true,
                    showNormalSpeedInBottomBar = false,
                    showNormalDanmakuInTopBar = true,
                    enableNormalScreenSeekGestures = true,
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
                    isMpvEngine = playingVM.isMpvEngine,
                    mpvAnime4kEnabled = mpvAnime4kEnabled,
                    mpvAnime4kPreset = mpvAnime4kPreset,
                    mpvAnime4kStatus = mpvAnime4kStatus,
                    onMpvAnime4kEnabledChange = playingVM::setMpvAnime4kEnabled,
                    onMpvAnime4kPresetChange = playingVM::setMpvAnime4kPreset,
                    isExoPlayerEngine = !playingVM.isMpvEngine,
                    exoAdAudioProbeEnabled = exoAdAudioProbeEnabled,
                    exoAdAudioProbeRulesUrl = exoAdAudioProbeRulesUrl,
                    onExoAdAudioProbeEnabledChange = playingVM::setExoAdAudioProbeEnabled,
                    onExoAdAudioProbeRulesUrlChange = playingVM::setExoAdAudioProbeRulesUrl,
                )
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            onDownload = { line, episodes ->
                                downloadRequest = Triple(detailState.cartoonInfo, line, episodes)
                            },
                            onExternalPlay = playingVM::playCurrentExternal,
                            hasCustomPlaybackHeaders = playingVM.hasCustomPlaybackHeaders(),
                            onMediaData = { playingVM.playbackDiagnostic() },
                            onCast = {
                                if (detailState.cartoonInfo.source == LocalSource.LOCAL_SOURCE_KEY) {
                                    "本地番源不支持投屏".moeSnackBar()
                                } else {
                                    playState?.let { current ->
                                        nav.navigationDlna(
                                            detailState.cartoonInfo.id,
                                            detailState.cartoonInfo.source,
                                            CartoonPlayViewModel.EnterData(
                                                playLineId = current.playLine.playLine.id,
                                                playLineLabel = current.playLine.playLine.label,
                                                playLineIndex = playLines.indexOfFirst {
                                                    it.playLine.id == current.playLine.playLine.id
                                                },
                                                episodeId = current.episode.id,
                                                episodeLabel = current.episode.label,
                                                episodeOrder = current.episode.order,
                                                episodeIndex = current.playLine.playLine.episode.indexOfFirst {
                                                    it.id == current.episode.id
                                                },
                                                adviceProgress = controlVM.position,
                                            ),
                                        )
                                    }
                                }
                            },
                            onSortChange = { key, reverse ->
                                detailedVM.setCartoonSort(key, reverse, detailState.cartoonInfo)
                            },
                            onManualMatch = onManualMatch,
                            onDanmakuRetry = onDanmakuRetry,
                            onOpenDanmakuDisplaySettings = {
                                openPlayerSettings(PlayerSettingsSection.Danmaku)
                            },
                        )
                    }
                }
            }
        }
    }

    downloadRequest?.let { request ->
        PlaybackSheetTheme {
            CartoonDownloadDialog(
                cartoonInfo = request.first,
                playerLineWrapper = request.second,
                episodes = request.third,
                onDismissRequest = { downloadRequest = null },
            )
        }
    }
    danmakuState.manualMatch?.takeIf { danmakuState.isManualMatchVisible }?.let { manual ->
        PlaybackSheetTheme {
            DanmakuMatchBottomSheet(
                state = manual,
                onQueryChange = onManualQueryChange,
                onSearch = onManualSearch,
                onBangumiSelect = onManualBangumiSelect,
                onEpisodeSelect = onManualEpisodeSelect,
                onBackToBangumiSelection = onBackToBangumiSelection,
                onDismiss = onManualDismiss,
                isFullScreen = controlVM.isFullScreen,
            )
        }
    }
    detailState.starDialogState?.let { starDialog ->
        PlaybackSheetTheme {
            EasyMutiSelectionDialog(
                show = true,
                title = { Text("选择追番标签") },
                items = starDialog.tagList,
                initSelection = emptyList(),
                confirmText = "追番",
                onConfirm = { detailedVM.dialogSetCartoonStar(starDialog.cartoon, it) },
                onDismissRequest = detailedVM::dialogExit,
            )
        }
    }
    if (playingVM.isCustomSpeedDialog.value) {
        var customSpeedText by rememberSaveable {
            mutableStateOf(playingVM.customSpeed.value.takeIf { it > 0f }?.toString() ?: "1.0")
        }
        PlaybackSheetTheme {
            AlertDialog(
                onDismissRequest = { playingVM.isCustomSpeedDialog.value = false },
                title = { Text("自定义倍速") },
                text = {
                    OutlinedTextField(
                        value = customSpeedText,
                        onValueChange = { value ->
                            customSpeedText = value.filterIndexed { index, char ->
                                char.isDigit() || (char == '.' && index > 0 && '.' !in value.take(index))
                            }
                        },
                        singleLine = true,
                        label = { Text("播放倍速") },
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val speed = customSpeedText.toFloatOrNull()?.takeIf { it > 0f }
                            if (speed != null) {
                                playingVM.setCustomSpeed(speed)
                                playingVM.isCustomSpeed.value = true
                                controlVM.setSpeed(speed)
                                playingVM.isCustomSpeedDialog.value = false
                            }
                        },
                    ) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { playingVM.isCustomSpeedDialog.value = false }) {
                        Text("取消")
                    }
                },
            )
        }
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
    onDownload: (PlayLineWrapper, List<Episode>) -> Unit,
    onExternalPlay: () -> Unit,
    hasCustomPlaybackHeaders: Boolean,
    onMediaData: () -> CartoonPlayingViewModel.PlaybackDiagnostic?,
    onCast: () -> Unit,
    onSortChange: (String, Boolean) -> Unit,
    onManualMatch: () -> Unit,
    onDanmakuRetry: () -> Unit,
    onOpenDanmakuDisplaySettings: () -> Unit,
) {
    var showPlaybackLines by remember { mutableStateOf(false) }
    var showAllEpisodes by remember { mutableStateOf(false) }
    var showSort by remember { mutableStateOf(false) }
    var showDanmakuPanel by remember { mutableStateOf(false) }
    var showMoreActions by remember { mutableStateOf(false) }
    var showMediaData by remember { mutableStateOf<CartoonPlayingViewModel.PlaybackDiagnostic?>(null) }
    var downloadSelection by remember(cartoon.id, cartoon.source) {
        mutableStateOf<DownloadEpisodeSelection?>(null)
    }

    BackHandler(enabled = downloadSelection != null) {
        downloadSelection = null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(V2Tokens.PlayerDark),
    ) {
        item {
            V2MediaIdentity(cartoon)
        }
        item {
            PlayerDividerV2()
            V2ActionRow(
                isStar = cartoon.starTime > 0,
                onStar = onStar,
                onSearch = onSearch,
                onDownload = {
                    playLines.getOrNull(selectedLineIndex)?.let { selectedLine ->
                        downloadSelection = DownloadEpisodeSelection(lineId = selectedLine.playLine.id)
                        showAllEpisodes = true
                    }
                },
                isSelectingDownloads = downloadSelection != null,
                onCast = onCast,
                onMore = { showMoreActions = true },
            )
            PlayerDividerV2()
        }
        item {
            V2PlaybackLineSection(
                playLines = playLines,
                selectedLineIndex = selectedLineIndex,
                playingLine = playingState?.playLine,
                onOpen = { showPlaybackLines = true },
            )
            PlayerDividerV2()
        }
        item {
            V2EpisodeSection(
                playLines = playLines,
                selectedLineIndex = selectedLineIndex,
                playingState = playingState,
                sortState = sortState,
                onEpisodeSelect = onEpisodeSelect,
                onSort = { showSort = true },
                onAllEpisodes = { showAllEpisodes = true },
            )
            PlayerDividerV2()
        }
        item {
            DanmakuEntryV2(
                state = danmakuState,
                onClick = { showDanmakuPanel = true },
            )
        }
        item { Spacer(Modifier.height(36.dp)) }
    }
    if (showPlaybackLines) {
        PlaybackLineBottomSheetV2(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingLine = playingState?.playLine,
            onLineSelect = { index ->
                downloadSelection = null
                val targetLine = playLines.getOrNull(index)
                val currentEpisode = playingState?.episode
                val currentEpisodeIndex = playingState?.playLine?.sortedEpisodeList
                    ?.indexOfFirst { it.id == currentEpisode?.id }
                    ?.takeIf { it >= 0 }
                val targetEpisode = targetLine?.sortedEpisodeList?.firstOrNull {
                    it.id == currentEpisode?.id || it.order == currentEpisode?.order
                } ?: currentEpisodeIndex?.let { targetLine?.sortedEpisodeList?.getOrNull(it) }
                    ?: targetLine?.sortedEpisodeList?.firstOrNull()
                onLineSelect(index)
                if (targetLine != null && targetEpisode != null) {
                    onEpisodeSelect(targetLine, targetEpisode)
                }
                showPlaybackLines = false
            },
            onDismiss = { showPlaybackLines = false },
        )
    }
    if (showAllEpisodes) {
        val selectedLine = playLines.getOrNull(selectedLineIndex)
        val activeDownloadSelection = downloadSelection?.takeIf {
            it.lineId == selectedLine?.playLine?.id
        }
        EpisodePickerBottomSheet(
            playLines = playLines,
            selectedLineIndex = selectedLineIndex,
            playingState = playingState,
            sortState = sortState,
            onEpisodeSelect = { line, episode ->
                if (activeDownloadSelection == null) {
                    onEpisodeSelect(line, episode)
                    showAllEpisodes = false
                } else {
                    downloadSelection = activeDownloadSelection.toggle(episode.id)
                }
            },
            onSort = {
                showAllEpisodes = false
                showSort = true
            },
            downloadSelection = activeDownloadSelection,
            onEnterDownloadMode = {
                selectedLine?.let {
                    downloadSelection = DownloadEpisodeSelection(it.playLine.id)
                }
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
                    showAllEpisodes = false
                    onDownload(selectedLine, episodes)
                }
            },
            onDismiss = {
                downloadSelection = null
                showAllEpisodes = false
            },
        )
    }
    if (showSort) {
        EpisodeSortBottomSheet(
            sortState = sortState,
            onSortChange = onSortChange,
            onDismiss = { showSort = false },
        )
    }
    if (showDanmakuPanel) {
        DanmakuStatusBottomSheetV2(
            state = danmakuState,
            onManualMatch = {
                showDanmakuPanel = false
                onManualMatch()
            },
            onRetry = {
                showDanmakuPanel = false
                onDanmakuRetry()
            },
            onOpenDisplaySettings = {
                showDanmakuPanel = false
                onOpenDanmakuDisplaySettings()
            },
            onDismiss = { showDanmakuPanel = false },
        )
    }
    if (showMoreActions) {
        PlaybackMoreActionsBottomSheetV2(
            onExternalPlay = {
                showMoreActions = false
                onExternalPlay()
            },
            hasCustomPlaybackHeaders = hasCustomPlaybackHeaders,
            onMediaData = {
                showMoreActions = false
                showMediaData = onMediaData()
            },
            onDismiss = { showMoreActions = false },
        )
    }
    showMediaData?.let { diagnostic ->
        PlaybackMediaDataBottomSheetV2(
            cartoon = cartoon,
            diagnostic = diagnostic,
            onDismiss = { showMediaData = null },
        )
    }
}

@Composable
internal fun V2MediaIdentity(cartoon: CartoonInfo) {
    var expanded by rememberSaveable(cartoon.id, cartoon.source) { mutableStateOf(false) }
    val synopsis = cartoon.description.ifBlank { cartoon.intro }
    val interactionSource = remember { MutableInteractionSource() }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CartoonPlayV2TestTags.MEDIA_IDENTITY)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { expanded = !expanded },
                onLongClick = {
                    val text = listOf(cartoon.name, synopsis, cartoon.url)
                        .filter(String::isNotBlank)
                        .joinToString("\n")
                    clipboard.setText(AnnotatedString(text))
                    "详情已复制".moeSnackBar()
                },
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        .width(84.dp)
                        .aspectRatio(0.7f)
                        .clip(RoundedCornerShape(9.dp))
                        .combinedClickable(
                            onClick = { expanded = !expanded },
                            onLongClick = {
                                scope.launch {
                                    val image = downloadImage(cartoon.coverUrl)
                                    if (image == null) {
                                        "封面保存失败".moeSnackBar()
                                    } else {
                                        MediaAndroidUtils.saveImage(
                                            image,
                                            "${cartoon.name.take(48)}.png",
                                        ).onSuccess { "封面已保存".moeSnackBar() }
                                            .onFailure { "封面保存失败".moeSnackBar() }
                                    }
                                }
                            },
                        ),
                    image = cartoon.coverUrl,
                    contentDescription = cartoon.name,
                    errorRes = R.drawable.placeholder,
                )
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = cartoon.name,
                        maxLines = if (showFullDetails) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (cartoon.genres.isNotEmpty()) {
                        Text(
                            cartoon.genres.joinToString(" · "),
                            color = V2Tokens.PlayerTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (synopsis.isNotBlank()) {
                        Text(
                            text = synopsis,
                            maxLines = if (showFullDetails) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            color = V2Tokens.PlayerTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = if (expanded) "收起" else "展开",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "收起详情" else "展开详情",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PlayerDividerV2() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 1.dp,
        color = V2Tokens.PlayerDivider,
    )
}

@Composable
private fun DanmakuEntryV2(
    state: DanmakuPlaybackState,
    onClick: () -> Unit,
) {
    PlaybackSettingRowV2(
        title = "弹幕",
        value = danmakuSummaryV2(state),
        onClick = onClick,
    )
}

private fun danmakuSummaryV2(state: DanmakuPlaybackState): String = when (val status = state.status) {
    DanmakuPlaybackStatus.Disabled -> "未启用"
    DanmakuPlaybackStatus.MatchingBangumi -> "正在匹配番剧"
    DanmakuPlaybackStatus.MatchingEpisode -> "正在匹配选集"
    DanmakuPlaybackStatus.LoadingComments -> "正在加载弹幕"
    is DanmakuPlaybackStatus.Matched -> "已开启 · ${status.comments.size} 条"
    is DanmakuPlaybackStatus.Empty -> "已匹配 · 暂无弹幕"
    is DanmakuPlaybackStatus.Unmatched -> "尚未匹配"
    is DanmakuPlaybackStatus.Unavailable -> "加载失败"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DanmakuStatusBottomSheetV2(
    state: DanmakuPlaybackState,
    onManualMatch: () -> Unit,
    onRetry: () -> Unit,
    onOpenDisplaySettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val status = state.status
    val primaryAction: Pair<String, () -> Unit>? = when (status) {
        is DanmakuPlaybackStatus.Unavailable -> "重试" to onRetry
        DanmakuPlaybackStatus.MatchingBangumi,
        DanmakuPlaybackStatus.MatchingEpisode,
        DanmakuPlaybackStatus.LoadingComments,
        DanmakuPlaybackStatus.Disabled,
        -> null
        is DanmakuPlaybackStatus.Matched -> "重匹配" to onManualMatch
        is DanmakuPlaybackStatus.Empty,
        is DanmakuPlaybackStatus.Unmatched,
        -> "去匹配" to onManualMatch
    }
    PlaybackSheetTheme {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Black.copy(alpha = 0.48f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CartoonPlayV2TestTags.DANMAKU_PANEL)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "弹幕",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = danmakuSummaryV2(state),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        state.source?.displayName?.let { sourceName ->
                            Text(
                                text = "弹幕源 · $sourceName",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        when (status) {
                            is DanmakuPlaybackStatus.Matched -> Text(
                                text = "${status.binding.bangumiTitle} · ${status.binding.episodeTitle}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            is DanmakuPlaybackStatus.Unmatched -> Text(
                                text = status.message,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            is DanmakuPlaybackStatus.Unavailable -> Text(
                                text = status.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            else -> Unit
                        }
                    }
                }
                primaryAction?.let { (label, action) ->
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = action,
                    ) {
                        Text(label)
                    }
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenDisplaySettings,
                ) {
                    Text("显示设置")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlaybackSettingRowV2(
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            modifier = Modifier.widthIn(max = 220.dp),
            color = V2Tokens.PlayerTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = V2Tokens.PlayerTextSecondary,
            modifier = Modifier
                .padding(start = 4.dp)
                .size(20.dp),
        )
    }
}

@Composable
internal fun V2ActionRow(
    isStar: Boolean,
    onStar: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onDownload: () -> Unit,
    isSelectingDownloads: Boolean = false,
    onCast: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CartoonPlayV2TestTags.ACTIONS)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        V2Action(
            "追番",
            if (isStar) Icons.Filled.Star else Icons.Filled.StarOutline,
            selected = isStar,
            onClick = { onStar(!isStar) },
        )
        V2Action("换源", Icons.Filled.Search, onClick = onSearch)
        V2Action(
            "下载",
            Icons.Filled.Download,
            selected = isSelectingDownloads,
            onClick = onDownload,
        )
        V2Action("投屏", Icons.Filled.Cast, onClick = onCast)
        V2Action("更多", Icons.Filled.MoreHoriz, onClick = onMore)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaybackMoreActionsBottomSheetV2(
    onExternalPlay: () -> Unit,
    hasCustomPlaybackHeaders: Boolean,
    onMediaData: () -> Unit,
    onDismiss: () -> Unit,
) {
    PlaybackSheetTheme {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Black.copy(alpha = 0.48f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CartoonPlayV2TestTags.MORE_ACTIONS)
                    .padding(bottom = 20.dp),
            ) {
                Text(
                    text = "更多操作",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                PlaybackMoreActionRowV2("媒体数据", "查看番剧、播放地址与请求 Header", Icons.Filled.MoreHoriz, onMediaData)
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = MaterialTheme.colorScheme.outline)
                PlaybackMoreActionRowV2(
                    "外播",
                    if (hasCustomPlaybackHeaders) "当前链接含自定义 Header，外部播放器可能无法播放" else "使用其他播放器打开",
                    Icons.Filled.ScreenShare,
                    onExternalPlay,
                )
            }
        }
    }
}

@Composable
private fun PlaybackMoreActionRowV2(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.padding(start = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackMediaDataBottomSheetV2(
    cartoon: CartoonInfo,
    diagnostic: CartoonPlayingViewModel.PlaybackDiagnostic,
    onDismiss: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    PlaybackSheetTheme {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text("媒体数据", Modifier.padding(20.dp), style = MaterialTheme.typography.headlineSmall)
                PlaybackDataRow("番剧地址", cartoon.url, clipboard, openOnClick = true)
                PlaybackDataRow("播放地址", diagnostic.mediaUrl, clipboard, openOnClick = true)
                PlaybackDataRow("封面地址", cartoon.coverUrl, clipboard, openOnClick = true)
                diagnostic.headers.forEach { (name, value) ->
                    PlaybackDataRow("Header · $name", value, clipboard, openOnClick = false)
                }
            }
        }
    }
}

@Composable
private fun PlaybackDataRow(
    label: String,
    value: String,
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    openOnClick: Boolean,
) {
    if (value.isBlank()) return
    Column(
        Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (openOnClick) {
                        value.openUrl()
                    } else {
                        clipboard.setText(AnnotatedString(value))
                        "已复制 $label".moeSnackBar()
                    }
                },
                onLongClick = {
                    clipboard.setText(AnnotatedString(value))
                    "已复制 $label".moeSnackBar()
                },
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun V2Action(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    width: androidx.compose.ui.unit.Dp = 72.dp,
    selected: Boolean? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width)
            .testTag(CartoonPlayV2TestTags.action(label))
            .clip(RoundedCornerShape(10.dp))
            .semantics {
                selected?.let {
                    this.selected = it
                    stateDescription = if (it) "已开启" else "未开启"
                }
            }
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            modifier = Modifier.size(23.dp),
            imageVector = icon,
            contentDescription = label,
            tint = if (selected == true) MaterialTheme.colorScheme.primary else Color.White,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            color = if (selected == true) MaterialTheme.colorScheme.primary else Color.White,
            fontWeight = if (selected == true) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
internal fun V2PlaybackLineSection(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingLine: PlayLineWrapper?,
    onOpen: () -> Unit = {},
) {
    val selectedLine = playLines.getOrNull(selectedLineIndex)
    val value = when {
        selectedLine == null -> "暂无可用线路"
        selectedLine.playLine.id == playingLine?.playLine?.id -> "${selectedLine.playLine.label} · 播放中"
        else -> selectedLine.playLine.label
    }
    PlaybackSettingRowV2(
        modifier = Modifier
            .testTag(selectedLine?.let { CartoonPlayV2TestTags.source(it.playLine.id) } ?: "playback_source_empty")
            .semantics { selected = selectedLine != null },
        title = "播放线路",
        value = value,
        onClick = onOpen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlaybackLineBottomSheetV2(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingLine: PlayLineWrapper?,
    onLineSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    PlaybackSheetTheme {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Black.copy(alpha = 0.48f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CartoonPlayV2TestTags.LINE_PICKER),
            ) {
                Text(
                    text = "选择播放线路",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "切换后将尽量继续播放当前剧集",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    itemsIndexed(playLines, key = { _, line -> line.playLine.id }) { index, line ->
                        val selected = index == selectedLineIndex
                        val isPlaying = line.playLine.id == playingLine?.playLine?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(CartoonPlayV2TestTags.source(line.playLine.id))
                                .selectable(
                                    selected = selected,
                                    role = Role.RadioButton,
                                    onClick = { onLineSelect(index) },
                                ),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = line.playLine.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    )
                                    if (isPlaying) {
                                        Text(
                                            text = "当前播放线路",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "已选择",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun V2EpisodeSection(
    playLines: List<PlayLineWrapper>,
    selectedLineIndex: Int,
    playingState: CartoonPlayViewModel.CartoonPlayState?,
    @Suppress("UNUSED_PARAMETER") sortState: SortState<Episode>,
    @Suppress("UNUSED_PARAMETER")
    onEpisodeSelect: (PlayLineWrapper, Episode) -> Unit,
    @Suppress("UNUSED_PARAMETER")
    onSort: () -> Unit,
    onAllEpisodes: () -> Unit,
) {
    val selectedLine = playLines.getOrNull(selectedLineIndex)
    val episodeCount = selectedLine?.sortedEpisodeList?.size ?: 0
    val playingEpisode = playingState?.takeIf {
        it.playLine.playLine.id == selectedLine?.playLine?.id
    }?.episode?.label
    PlaybackSettingRowV2(
        title = "剧集",
        value = when {
            selectedLine == null -> "暂无可播放剧集"
            playingEpisode != null -> "$playingEpisode · 共 $episodeCount 集"
            else -> "共 $episodeCount 集"
        },
        onClick = onAllEpisodes,
    )
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
    onEpisodeSelect: (PlayLineWrapper, Episode) -> Unit,
    onSort: () -> Unit,
    downloadSelection: DownloadEpisodeSelection? = null,
    onEnterDownloadMode: () -> Unit = {},
    onDownloadSelectAll: () -> Unit = {},
    onDownloadCancel: () -> Unit = {},
    onDownloadConfirm: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    var keyword by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val selectedLine = playLines.getOrNull(selectedLineIndex)
    val allEpisodes = selectedLine?.sortedEpisodeList.orEmpty()
    val episodes = allEpisodes.filter { it.label.contains(keyword, ignoreCase = true) }
    val isDownloadMode = downloadSelection != null
    val selectedDownloadCount = downloadSelection?.episodeIds?.size ?: 0
    val allDownloadEpisodesSelected = allEpisodes.isNotEmpty() &&
        selectedDownloadCount == allEpisodes.size
    LaunchedEffect(selectedLine?.playLine?.id, playingState?.episode?.id) {
        val index = allEpisodes.indexOfFirst { it.id == playingState?.episode?.id }
        if (index >= 0) gridState.scrollToItem(index)
    }

    PlaybackSheetTheme {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = 0.48f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CartoonPlayV2TestTags.EPISODE_PICKER)
                .then(
                    if (isDownloadMode) {
                        Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_SELECTION)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (isDownloadMode) "选择下载剧集" else "全部选集",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                if (isDownloadMode) {
                    TextButton(
                        modifier = Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_CANCEL),
                        onClick = onDownloadCancel,
                    ) {
                        Text("取消")
                    }
                } else {
                    TextButton(onClick = onEnterDownloadMode) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("下载")
                    }
                    IconButton(onClick = onSort) {
                        Icon(Icons.Filled.Sort, contentDescription = "切换排序")
                    }
                }
            }
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = if (isDownloadMode) {
                    "已选择 $selectedDownloadCount 集"
                } else {
                    "${sortState.sortList.find { it.id == sortState.current }?.label ?: "默认"} · ${if (sortState.isReverse) "倒序" else "正序"}"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            selectedLine?.let { line ->
                Text(
                    text = "当前线路 · ${line.playLine.label}",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            androidx.compose.material3.OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                value = keyword,
                onValueChange = { keyword = it },
                label = { Text("搜索剧集") },
                singleLine = true,
            )
            if (isDownloadMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        modifier = Modifier.testTag(CartoonPlayV2TestTags.DOWNLOAD_SELECT_ALL),
                        onClick = onDownloadSelectAll,
                    ) {
                        Icon(Icons.Filled.SelectAll, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (allDownloadEpisodesSelected) "取消全选" else "全选")
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "共 ${allEpisodes.size} 集",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            LazyVerticalGrid(
                modifier = Modifier
                    .height(if (isDownloadMode) 390.dp else 450.dp)
                    .padding(horizontal = 20.dp),
                columns = GridCells.Adaptive(100.dp),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(episodes.size, key = { episodes[it].id }) { index ->
                    val episode = episodes[index]
                    EpisodeRailButton(
                        episode = episode,
                        selected = if (isDownloadMode) {
                            episode.id in downloadSelection?.episodeIds.orEmpty()
                        } else {
                            playingState?.playLine?.playLine?.id == selectedLine?.playLine?.id &&
                                playingState?.episode?.id == episode.id
                        },
                        selectionMode = isDownloadMode,
                        unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { selectedLine?.let { onEpisodeSelect(it, episode) } },
                    )
                }
            }
            if (isDownloadMode) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .testTag(CartoonPlayV2TestTags.DOWNLOAD_CONFIRM),
                    enabled = selectedDownloadCount > 0,
                    onClick = onDownloadConfirm,
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedDownloadCount > 0) "下载 $selectedDownloadCount 集" else "请选择剧集")
                }
            } else {
                Spacer(Modifier.height(16.dp))
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
    PlaybackSheetTheme {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.48f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
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
}
