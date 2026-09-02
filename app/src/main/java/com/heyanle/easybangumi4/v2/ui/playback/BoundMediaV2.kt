package com.heyanle.easybangumi4.v2.ui.playback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.cartoon.story.bound.BoundMedia
import com.heyanle.easybangumi4.cartoon.story.bound.BoundMediaCase
import com.heyanle.easybangumi4.cartoon.story.bound.CartoonEpisodeBinding
import com.heyanle.easybangumi4.cartoon.story.bound.CartoonEpisodeBindingController
import com.heyanle.easybangumi4.cartoon.story.bound.FlatDownloadController
import com.heyanle.easybangumi4.cartoon.story.bound.FlatVideoItem
import com.heyanle.easybangumi4.plugin.source.js.source.getIconWithAsyncOrDrawable
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 播放页绑定数据中枢：跟踪当前播放目标的本地可用性，
 * 并提供换绑（扁平目录视频 / 本地番源剧集）与解绑操作。
 */
class BoundMediaViewModel : ViewModel() {

    private val boundMediaCase: BoundMediaCase by Inject.injectLazy()
    private val bindingController: CartoonEpisodeBindingController by Inject.injectLazy()
    private val flatDownloadController: FlatDownloadController by Inject.injectLazy()
    private val cartoonStoryController: CartoonStoryController by Inject.injectLazy()

    private val playStateFlow =
        MutableStateFlow<CartoonPlayViewModel.CartoonPlayState?>(null)

    fun onPlayStateChanged(state: CartoonPlayViewModel.CartoonPlayState?) {
        playStateFlow.value = state
    }

    /** 当前集可用的本地媒体；null = 无绑定或文件缺失。绑定/目录/剧集变化时自动重算。 */
    val localMedia: StateFlow<BoundMedia?> = combine(
        playStateFlow,
        bindingController.bindings,
        flatDownloadController.flatVideos,
        cartoonStoryController.storyItemList,
    ) { state, _, _, _ ->
        state?.let { s ->
            runCatching {
                boundMediaCase.findLocalMedia(
                    s.cartoonSummary,
                    s.playLine.playLine.id,
                    s.episode,
                )
            }.getOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** 当前番剧实体的全部集级绑定（剧集角标数据源）。 */
    val currentBindings: StateFlow<List<CartoonEpisodeBinding>> = combine(
        playStateFlow,
        bindingController.bindings,
    ) { state, bindings ->
        state?.let { s ->
            bindings.filter {
                it.source == s.cartoonSummary.source && it.cartoonId == s.cartoonSummary.id
            }
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val flatVideos: StateFlow<List<FlatVideoItem>> = flatDownloadController.flatVideos

    val storyItems: StateFlow<List<CartoonStoryItem>> = cartoonStoryController.storyItemList
        .map { (it as? DataResult.Ok)?.data.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun currentBindingOf(
        state: CartoonPlayViewModel.CartoonPlayState,
        bindings: List<CartoonEpisodeBinding> = currentBindings.value,
    ): CartoonEpisodeBinding? {
        val candidates = bindings.filter {
            it.source == state.cartoonSummary.source &&
                it.cartoonId == state.cartoonSummary.id
        }
        val lineId = state.playLine.playLine.id
        val episode = state.episode
        return candidates.firstOrNull {
            it.lineId == lineId && it.episodeId.isNotEmpty() && it.episodeId == episode.id
        } ?: candidates.firstOrNull {
            it.episodeId.isNotEmpty() && it.episodeId == episode.id
        } ?: candidates.firstOrNull {
            it.lineId == lineId && it.episodeOrder == episode.order
        } ?: candidates.firstOrNull {
            it.episodeOrder == episode.order
        }
    }

    fun manualBindFlatFile(
        cartoon: CartoonInfo,
        state: CartoonPlayViewModel.CartoonPlayState,
        fileName: String,
        onBound: () -> Unit = {},
    ) {
        viewModelScope.launch {
            boundMediaCase.manualBindFlatFile(
                cartoon,
                state.playLine.playLine,
                state.episode,
                fileName,
            )
            onBound()
        }
    }

    fun manualBindLocalStory(
        cartoon: CartoonInfo,
        state: CartoonPlayViewModel.CartoonPlayState,
        itemId: String,
        localEpisode: Int,
        onBound: () -> Unit = {},
    ) {
        viewModelScope.launch {
            boundMediaCase.manualBindLocalStory(
                cartoon,
                state.playLine.playLine,
                state.episode,
                itemId,
                localEpisode,
            )
            onBound()
        }
    }

    fun unbind(state: CartoonPlayViewModel.CartoonPlayState) {
        boundMediaCase.unbind(
            state.cartoonSummary.source,
            state.cartoonSummary.id,
            state.episode.id,
            state.episode.order,
        )
    }

}

private enum class PlaybackSourcePage { SUMMARY, SOURCE, VIDEO }

private sealed interface PlaybackSourceTarget {
    data object Flat : PlaybackSourceTarget
    data class Story(val itemId: String) : PlaybackSourceTarget
}

/** 播放来源在详情页只保留一个入口，右侧始终反映播放器实际使用的来源。 */
@Composable
internal fun V2MediaSourceSection(
    isPlayingLocal: Boolean,
    sourceLabel: String,
    currentBinding: CartoonEpisodeBinding?,
    onOpen: () -> Unit,
) {
    val value = when {
        !isPlayingLocal -> "在线播放"
        currentBinding?.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE -> "本地缓存"
        else -> "本地番源"
    }
    PlaybackSettingRowV2(
        modifier = Modifier.testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_ENTRY),
        title = stringResource(id = R.string.playback_source),
        value = value,
        onClick = onOpen,
    )
}

/**
 * 与弹幕匹配一致的层级：先展示当前状态；点击切换后，第一步选来源，
 * 扁平目录或本地番源再进入第二步选视频，云端直接完成切换。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoundMediaBottomSheetV2(
    isPlayingLocal: Boolean,
    sourceKey: String = "",
    sourceLabel: String,
    cloudDetail: String,
    localMedia: BoundMedia?,
    currentBinding: CartoonEpisodeBinding?,
    flatVideos: List<FlatVideoItem>,
    storyItems: List<CartoonStoryItem>,
    onSelectCloud: () -> Unit,
    onReResolve: () -> Unit,
    onDownload: () -> Unit,
    onBindFlatFile: (String) -> Unit,
    onBindLocalStory: (String, Int) -> Unit,
    onUnbind: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sourceStateCase: com.heyanle.easybangumi4.case.SourceStateCase by Inject.injectLazy()
    val sourceBundle by sourceStateCase.stateFlowBundle().collectAsState()
    val cloudIcon = remember(sourceBundle, sourceKey) {
        sourceBundle?.icon(sourceKey)?.getIconWithAsyncOrDrawable()
    }
    var page by remember { mutableStateOf(PlaybackSourcePage.SUMMARY) }
    var selectedTarget by remember { mutableStateOf<PlaybackSourceTarget?>(null) }
    var keyword by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val transitionScope = rememberCoroutineScope()
    val selectedStory = (selectedTarget as? PlaybackSourceTarget.Story)?.let { target ->
        storyItems.firstOrNull { it.cartoonLocalItem.itemId == target.itemId }
    }
    val boundStory = currentBinding
        ?.takeIf { it.targetType == CartoonEpisodeBinding.TARGET_LOCAL_STORY }
        ?.let { binding -> storyItems.firstOrNull { it.cartoonLocalItem.itemId == binding.localItemId } }
    val boundStoryEpisode = boundStory?.cartoonLocalItem?.episodes?.firstOrNull {
        it.episode == currentBinding?.localEpisode
    }

    BackHandler(enabled = page != PlaybackSourcePage.SUMMARY) {
        page = if (page == PlaybackSourcePage.VIDEO) {
            PlaybackSourcePage.SOURCE
        } else {
            PlaybackSourcePage.SUMMARY
        }
        keyword = ""
    }

    PlaybackSheetTheme {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            scrimColor = Color.Black.copy(alpha = 0.48f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_SHEET),
            ) {
                Text(
                    text = if (page == PlaybackSourcePage.SUMMARY) "播放来源" else "切换播放来源",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    text = if (page == PlaybackSourcePage.SUMMARY) {
                        "查看当前实际播放的媒体，或切换到其他可用来源。"
                    } else {
                        "选择要从哪里播放；选择本地来源后，再选择具体视频。"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )

                when (page) {
                    PlaybackSourcePage.SUMMARY -> PlaybackSourceSummary(
                        isPlayingLocal = isPlayingLocal,
                        sourceLabel = sourceLabel,
                        cloudDetail = cloudDetail,
                        cloudIcon = cloudIcon,
                        localMedia = localMedia,
                        currentBinding = currentBinding,
                        boundStory = boundStory,
                        boundStoryEpisodeTitle = boundStoryEpisode?.title.orEmpty(),
                        onReResolve = onReResolve,
                        onSwitch = {
                            transitionScope.launch {
                                // 与弹幕“重匹配”一致：旧信息面板先退场，
                                // 再以新的 BottomSheet 入场动画打开来源选择。
                                sheetState.hide()
                                page = PlaybackSourcePage.SOURCE
                                keyword = ""
                                sheetState.show()
                            }
                        },
                        onDownload = {
                            onDismiss()
                            onDownload()
                        },
                        onUnbind = {
                            onUnbind()
                            onDismiss()
                        },
                    )

                    PlaybackSourcePage.SOURCE -> PlaybackSourceFirstStep(
                        isPlayingLocal = isPlayingLocal,
                        sourceLabel = sourceLabel,
                        cloudDetail = cloudDetail,
                        cloudIcon = cloudIcon,
                        keyword = keyword,
                        onKeywordChange = { keyword = it },
                        currentBinding = currentBinding,
                        flatVideos = flatVideos,
                        storyItems = storyItems,
                        onCloudSelect = {
                            onSelectCloud()
                            onDismiss()
                        },
                        onTargetSelect = { target ->
                            selectedTarget = target
                            keyword = ""
                            page = PlaybackSourcePage.VIDEO
                        },
                    )

                    PlaybackSourcePage.VIDEO -> PlaybackSourceSecondStep(
                        target = selectedTarget,
                        selectedStory = selectedStory,
                        keyword = keyword,
                        onKeywordChange = { keyword = it },
                        currentBinding = currentBinding,
                        flatVideos = flatVideos,
                        onChangeSource = {
                            keyword = ""
                            page = PlaybackSourcePage.SOURCE
                        },
                        onBindFlatFile = {
                            onBindFlatFile(it)
                            onDismiss()
                        },
                        onBindLocalStory = { itemId, number ->
                            onBindLocalStory(itemId, number)
                            onDismiss()
                        },
                        onDownload = {
                            onDismiss()
                            onDownload()
                        },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PlaybackSourceSummary(
    isPlayingLocal: Boolean,
    sourceLabel: String,
    cloudDetail: String,
    cloudIcon: Any?,
    localMedia: BoundMedia?,
    currentBinding: CartoonEpisodeBinding?,
    boundStory: CartoonStoryItem?,
    boundStoryEpisodeTitle: String,
    onReResolve: () -> Unit,
    onSwitch: () -> Unit,
    onDownload: () -> Unit,
    onUnbind: () -> Unit,
) {
    val isFlat = currentBinding?.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE
    val title = when {
        !isPlayingLocal -> "在线播放"
        isFlat -> "本地缓存"
        else -> boundStory?.cartoonLocalItem?.title ?: "本地番源"
    }
    val detail = when {
        !isPlayingLocal -> listOf(sourceLabel, cloudDetail)
            .filter(String::isNotBlank)
            .joinToString(" · ")
        isFlat -> localMedia?.displayName ?: currentBinding?.flatFileName.orEmpty()
        else -> listOfNotNull(
            currentBinding?.localEpisode?.toString(),
            boundStoryEpisodeTitle.takeIf(String::isNotBlank),
        ).joinToString("、").ifBlank { localMedia?.displayName.orEmpty() }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_SUMMARY)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "正在播放",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CurrentPlaybackCard(
            isPlayingLocal = isPlayingLocal,
            title = title,
            detail = detail,
            leadingIcon = {
                when {
                    !isPlayingLocal -> CloudSourceIcon(cloudIcon, sourceLabel)
                    isFlat -> PlaybackTypeIcon(PlaybackSourceTarget.Flat)
                    boundStory != null -> LocalStoryCover(boundStory)
                    else -> PlaybackTypeIcon(PlaybackSourceTarget.Story(""))
                }
            },
            onReResolve = onReResolve,
        )
        if (currentBinding != null && !isPlayingLocal) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("已绑定本地备选", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = currentBinding.description(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_SWITCH),
            onClick = onSwitch,
        ) {
            Text("切换来源")
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(modifier = Modifier.weight(1f), onClick = onDownload) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(id = R.string.download_new_video))
            }
            if (currentBinding != null) {
                TextButton(modifier = Modifier.weight(1f), onClick = onUnbind) {
                    Text(stringResource(id = R.string.unbind_media))
                }
            }
        }
    }
}

@Composable
private fun PlaybackSourceFirstStep(
    isPlayingLocal: Boolean,
    sourceLabel: String,
    cloudDetail: String,
    cloudIcon: Any?,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    currentBinding: CartoonEpisodeBinding?,
    flatVideos: List<FlatVideoItem>,
    storyItems: List<CartoonStoryItem>,
    onCloudSelect: () -> Unit,
    onTargetSelect: (PlaybackSourceTarget) -> Unit,
) {
    val filteredStories = storyItems.filter {
        it.cartoonLocalItem.title.contains(keyword.trim(), ignoreCase = true)
    }
    PlaybackSourceProgress(PlaybackSourcePage.SOURCE)
    HorizontalDivider()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item(key = "cloud") {
            BoundTargetRow(
                modifier = Modifier.testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_CLOUD),
                title = "在线播放",
                subtitle = if (!isPlayingLocal) "$cloudDetail · 正在播放" else cloudDetail,
                selected = !isPlayingLocal,
                leadingIcon = { CloudSourceIcon(cloudIcon, sourceLabel) },
                trailing = {
                    Text(
                        text = sourceLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = onCloudSelect,
            )
        }
        item(key = "flat") {
            val isBound = currentBinding?.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE
            BoundTargetRow(
                modifier = Modifier.testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_FLAT),
                title = "本地缓存",
                subtitle = if (isBound) {
                    "已绑定 · ${currentBinding.flatFileName}"
                } else {
                    "${flatVideos.size} 个本地视频"
                },
                selected = isPlayingLocal && isBound,
                leadingIcon = { PlaybackTypeIcon(PlaybackSourceTarget.Flat) },
                trailing = {
                    SourceChoiceTrailing(bound = isBound)
                },
                onClick = { onTargetSelect(PlaybackSourceTarget.Flat) },
            )
        }
        item(key = "local_story_header") {
            Text(
                text = "本地番源",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item(key = "search") {
            OutlinedTextField(
                value = keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("搜索本地番源") },
            )
        }
        items(filteredStories, key = { "story_${it.cartoonLocalItem.itemId}" }) { story ->
            val binding = currentBinding?.takeIf {
                it.targetType == CartoonEpisodeBinding.TARGET_LOCAL_STORY &&
                    it.localItemId == story.cartoonLocalItem.itemId
            }
            BoundTargetRow(
                modifier = Modifier.testTag(
                    CartoonPlayV2TestTags.localStory(story.cartoonLocalItem.itemId),
                ),
                title = story.cartoonLocalItem.title,
                subtitle = binding?.let { "已绑定 · 编号 ${it.localEpisode}" }
                    ?: "${story.cartoonLocalItem.episodes.size} 个本地视频",
                selected = isPlayingLocal && binding != null,
                leadingIcon = { LocalStoryCover(story) },
                trailing = { SourceChoiceTrailing(bound = binding != null) },
                onClick = {
                    onTargetSelect(PlaybackSourceTarget.Story(story.cartoonLocalItem.itemId))
                },
            )
        }
        if (filteredStories.isEmpty()) {
            item(key = "no_story") {
                Text(
                    text = if (keyword.isBlank()) "暂无本地番源" else "没有找到匹配的本地番源",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaybackSourceSecondStep(
    target: PlaybackSourceTarget?,
    selectedStory: CartoonStoryItem?,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    currentBinding: CartoonEpisodeBinding?,
    flatVideos: List<FlatVideoItem>,
    onChangeSource: () -> Unit,
    onBindFlatFile: (String) -> Unit,
    onBindLocalStory: (String, Int) -> Unit,
    onDownload: () -> Unit,
) {
    val normalizedKeyword = keyword.trim()
    PlaybackSourceProgress(PlaybackSourcePage.VIDEO)
    SelectedPlaybackSource(
        target = target,
        selectedStory = selectedStory,
        currentBinding = currentBinding,
        onChangeSource = onChangeSource,
    )
    OutlinedTextField(
        value = keyword,
        onValueChange = onKeywordChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text("搜索视频") },
    )
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (target) {
            PlaybackSourceTarget.Flat -> {
                val videos = flatVideos.filter {
                    it.fileName.contains(normalizedKeyword, ignoreCase = true)
                }
                items(videos, key = { it.uri }) { video ->
                    val bound = currentBinding?.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE &&
                        currentBinding.flatFileName == video.fileName
                    BoundTargetRow(
                        modifier = Modifier.testTag(
                            CartoonPlayV2TestTags.localSource("flat_${video.uri}"),
                        ),
                        title = video.fileName,
                        subtitle = if (bound) "已绑定当前剧集 · ${formatSize(video.size)}"
                        else formatSize(video.size),
                        selected = bound,
                        onClick = { onBindFlatFile(video.fileName) },
                    )
                }
                if (videos.isEmpty()) {
                    item(key = "empty_flat") { EmptyVideoResult(normalizedKeyword) }
                }
                item(key = "download") {
                    BoundTargetRow(
                        title = stringResource(id = R.string.download_new_video),
                        subtitle = "下载完成后自动绑定当前剧集",
                        selected = false,
                        leadingIcon = {
                            Icon(Icons.Filled.Download, contentDescription = null)
                        },
                        onClick = onDownload,
                    )
                }
            }

            is PlaybackSourceTarget.Story -> {
                val episodes = selectedStory?.cartoonLocalItem?.episodes.orEmpty().filter {
                    it.title.contains(normalizedKeyword, ignoreCase = true) ||
                        it.episode.toString().contains(normalizedKeyword)
                }
                items(episodes, key = { "${target.itemId}_${it.episode}" }) { episode ->
                    val bound = currentBinding?.targetType == CartoonEpisodeBinding.TARGET_LOCAL_STORY &&
                        currentBinding.localItemId == target.itemId &&
                        currentBinding.localEpisode == episode.episode
                    BoundTargetRow(
                        modifier = Modifier.testTag(
                            CartoonPlayV2TestTags.localSource("story_${target.itemId}_${episode.episode}"),
                        ),
                        title = "${episode.episode}、${episode.title.ifBlank { "未命名视频" }}",
                        subtitle = if (bound) {
                            "已绑定当前剧集"
                        } else {
                            "本地视频"
                        },
                        selected = bound,
                        onClick = { onBindLocalStory(target.itemId, episode.episode) },
                    )
                }
                if (episodes.isEmpty()) {
                    item(key = "empty_story") { EmptyVideoResult(normalizedKeyword) }
                }
            }

            null -> item(key = "empty_target") { EmptyVideoResult("") }
        }
    }
}

@Composable
private fun PlaybackSourceProgress(page: PlaybackSourcePage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlaybackSourceStep(
            modifier = Modifier.testTag(CartoonPlayV2TestTags.MEDIA_SOURCE_STEP),
            index = 1,
            label = "选择来源",
            active = page == PlaybackSourcePage.SOURCE,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PlaybackSourceStep(
            modifier = Modifier.testTag(CartoonPlayV2TestTags.MEDIA_VIDEO_STEP),
            index = 2,
            label = "选择视频",
            active = page == PlaybackSourcePage.VIDEO,
        )
    }
}

@Composable
private fun PlaybackSourceStep(
    modifier: Modifier = Modifier,
    index: Int,
    label: String,
    active: Boolean,
) {
    Surface(
        modifier = modifier.semantics { selected = active },
        color = if (active) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (active) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            text = "$index  $label",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SelectedPlaybackSource(
    target: PlaybackSourceTarget?,
    selectedStory: CartoonStoryItem?,
    currentBinding: CartoonEpisodeBinding?,
    onChangeSource: () -> Unit,
) {
    val isFlat = target == PlaybackSourceTarget.Flat
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isFlat) PlaybackTypeIcon(PlaybackSourceTarget.Flat)
            else if (selectedStory != null) LocalStoryCover(selectedStory)
            else PlaybackTypeIcon(PlaybackSourceTarget.Story(""))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = if (isFlat) "本地缓存" else selectedStory?.cartoonLocalItem?.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val boundHere = when (target) {
                    PlaybackSourceTarget.Flat -> currentBinding?.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE
                    is PlaybackSourceTarget.Story -> currentBinding?.targetType == CartoonEpisodeBinding.TARGET_LOCAL_STORY &&
                        currentBinding.localItemId == target.itemId
                    null -> false
                }
                Text(
                    text = if (boundHere) currentBinding?.description().orEmpty() else "请选择具体视频",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onChangeSource) { Text("更换来源") }
        }
    }
}

@Composable
private fun SourceChoiceTrailing(bound: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (bound) {
            Text(
                text = "已绑定",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CloudSourceIcon(icon: Any?, label: String) {
    Surface(
        modifier = Modifier.size(48.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        OkImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            image = icon,
            contentDescription = "$label 图标",
            crossFade = false,
            placeholderRes = com.heyanle.easybangumi4.R.drawable.ic_source_default,
            errorRes = com.heyanle.easybangumi4.R.drawable.ic_source_default,
        )
    }
}

@Composable
private fun PlaybackTypeIcon(target: PlaybackSourceTarget) {
    Surface(
        modifier = Modifier.size(48.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Icon(
            modifier = Modifier.padding(12.dp),
            imageVector = if (target == PlaybackSourceTarget.Flat) {
                Icons.Filled.Folder
            } else {
                Icons.Filled.VideoLibrary
            },
            contentDescription = if (target == PlaybackSourceTarget.Flat) "本地缓存" else "本地番源",
        )
    }
}

@Composable
private fun LocalStoryCover(story: CartoonStoryItem) {
    OkImage(
        modifier = Modifier
            .size(width = 42.dp, height = 56.dp)
            .clip(RoundedCornerShape(8.dp)),
        image = story.cartoonLocalItem.cartoonCover.coverUrl.orEmpty(),
        contentDescription = "${story.cartoonLocalItem.title}封面",
        contentScale = ContentScale.Crop,
        errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
    )
}

@Composable
private fun EmptyVideoResult(keyword: String) {
    Text(
        text = if (keyword.isBlank()) "暂无可用视频" else "没有找到匹配的视频",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun CurrentPlaybackCard(
    isPlayingLocal: Boolean,
    title: String,
    detail: String,
    leadingIcon: @Composable () -> Unit,
    onReResolve: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon()
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "播放中",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!isPlayingLocal) {
                TextButton(onClick = onReResolve) {
                    Text(stringResource(id = R.string.re_resolve_cloud))
                }
            }
        }
    }
}

@Composable
private fun BoundTargetRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                it()
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            trailing?.invoke()
            if (selected) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun CartoonEpisodeBinding.description(): String {
    return when (targetType) {
        CartoonEpisodeBinding.TARGET_FLAT_FILE -> "已绑定 · $flatFileName"
        else -> "已绑定 · $localItemId · 编号 $localEpisode"
    }
}

private fun formatSize(size: Long): String {
    return when {
        size >= 1L shl 30 -> String.format("%.2f GB", size.toDouble() / (1L shl 30))
        size >= 1L shl 20 -> String.format("%.1f MB", size.toDouble() / (1L shl 20))
        size >= 1L shl 10 -> String.format("%.1f KB", size.toDouble() / (1L shl 10))
        else -> "$size B"
    }
}
