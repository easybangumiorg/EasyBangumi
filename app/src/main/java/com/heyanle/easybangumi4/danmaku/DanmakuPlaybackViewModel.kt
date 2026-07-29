package com.heyanle.easybangumi4.danmaku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class DanmakuBangumiContext(
    val cartoonId: String,
    val cartoonSourceId: String,
    val title: String,
)

data class DanmakuEpisodeContext(
    val playbackKey: DanmakuPlaybackKey,
    /** One-based position in the currently selected local sort order. */
    val sortedEpisodePosition: Int,
)

sealed interface DanmakuPlaybackStatus {
    data object Disabled : DanmakuPlaybackStatus
    data object MatchingBangumi : DanmakuPlaybackStatus
    data object MatchingEpisode : DanmakuPlaybackStatus
    data object LoadingComments : DanmakuPlaybackStatus
    data class Matched(
        val binding: DanmakuBinding,
        val comments: List<DanmakuComment>,
        val fromCache: Boolean,
    ) : DanmakuPlaybackStatus

    data class Empty(val binding: DanmakuBinding) : DanmakuPlaybackStatus
    data class Unmatched(val message: String) : DanmakuPlaybackStatus
    data class Unavailable(val message: String) : DanmakuPlaybackStatus
}

data class DanmakuManualMatchState(
    val sourceId: String,
    val query: String,
    val page: DanmakuMatchPage = DanmakuMatchPage.BANGUMI,
    /** Distinguishes the initial prompt from a completed search with no candidates. */
    val hasSearched: Boolean = false,
    val isSearching: Boolean = false,
    val candidates: List<DanmakuBangumi> = emptyList(),
    val selectedBangumi: DanmakuBangumi? = null,
    val isLoadingEpisodes: Boolean = false,
    val episodes: List<DanmakuEpisode> = emptyList(),
    val selectedEpisode: DanmakuEpisode? = null,
    val errorMessage: String? = null,
)

enum class DanmakuMatchPage {
    BANGUMI,
    EPISODE,
}

data class DanmakuPlaybackState(
    val bangumiContext: DanmakuBangumiContext? = null,
    val context: DanmakuEpisodeContext? = null,
    val source: DanmakuSourceMetadata? = null,
    val bangumiSelection: DanmakuBangumiSelection? = null,
    val status: DanmakuPlaybackStatus = DanmakuPlaybackStatus.Disabled,
    val manualMatch: DanmakuManualMatchState? = null,
    val isManualMatchVisible: Boolean = false,
)

/**
 * Playback-owned danmaku state machine with two independent lifetimes:
 *
 * - bangumi matching is page-scoped and runs at most once after detail is available;
 * - episode matching is playback-target-scoped and reruns for every real line/episode transition.
 *
 * A manually committed bangumi selection wins for the rest of the current page. Late automatic
 * results and late episode results are guarded by separate generations.
 */
class DanmakuPlaybackViewModel(
    private val repositoryOverride: DanmakuPlaybackStore? = null,
    private val requestCoordinatorOverride: DanmakuRequestCoordinator? = null,
) : ViewModel() {
    private val injectedRepository: DanmakuPlaybackStore by Inject.injectLazy()
    private val injectedRequestCoordinator: DanmakuRequestCoordinator by Inject.injectLazy()
    private val repository: DanmakuPlaybackStore
        get() = repositoryOverride ?: injectedRepository
    private val requestCoordinator: DanmakuRequestCoordinator
        get() = requestCoordinatorOverride ?: injectedRequestCoordinator

    private val _state = MutableStateFlow(DanmakuPlaybackState())
    val state = _state.asStateFlow()

    private var currentBangumiContext: DanmakuBangumiContext? = null
    private var currentEpisodeContext: DanmakuEpisodeContext? = null
    private var currentSession: DanmakuRequestSession? = null
    private var bangumiJob: Job? = null
    private var episodeJob: Job? = null
    private var manualJob: Job? = null
    private var bangumiGeneration = 0L
    private var manualRequestGeneration = 0L
    private var automaticBangumiAttempted = false

    fun onBangumiDetailAvailable(context: DanmakuBangumiContext?) {
        if (context == currentBangumiContext) return
        val compatibleTarget = currentEpisodeContext?.takeIf {
            context != null &&
                it.playbackKey.cartoonId == context.cartoonId &&
                it.playbackKey.cartoonSourceId == context.cartoonSourceId
        }
        resetBangumiSession()
        currentBangumiContext = context
        currentEpisodeContext = compatibleTarget
        if (context == null) {
            currentEpisodeContext = null
            _state.value = DanmakuPlaybackState()
            return
        }

        val source = repository.defaultSource()
        when {
            source == null -> {
                _state.value = DanmakuPlaybackState(
                    bangumiContext = context,
                    status = DanmakuPlaybackStatus.Disabled,
                )
            }

            !source.isAvailable() -> {
                _state.value = DanmakuPlaybackState(
                    bangumiContext = context,
                    source = source.metadata,
                    status = DanmakuPlaybackStatus.Unavailable("弹幕源尚未配置"),
                )
            }

            else -> {
                compatibleTarget?.let {
                    currentSession = requestCoordinator.begin(it.playbackKey)
                }
                _state.value = DanmakuPlaybackState(
                    bangumiContext = context,
                    context = compatibleTarget,
                    source = source.metadata,
                    status = DanmakuPlaybackStatus.MatchingBangumi,
                )
                tryResolveBangumi()
            }
        }
    }

    fun onPlaybackTargetChanged(context: DanmakuEpisodeContext?) {
        if (context == currentEpisodeContext) return
        episodeJob?.cancel()
        requestCoordinator.invalidate()
        currentSession = null
        currentEpisodeContext = context
        invalidateManualRequests()
        _state.update {
            it.copy(
                context = context,
                manualMatch = null,
                isManualMatchVisible = false,
            )
        }
        if (context == null) return

        val bangumi = currentBangumiContext
        if (bangumi == null ||
            context.playbackKey.cartoonId != bangumi.cartoonId ||
            context.playbackKey.cartoonSourceId != bangumi.cartoonSourceId
        ) {
            _state.update {
                it.copy(
                    bangumiSelection = null,
                    status = DanmakuPlaybackStatus.MatchingBangumi,
                )
            }
            return
        }

        currentSession = requestCoordinator.begin(context.playbackKey)
        when {
            _state.value.bangumiSelection != null -> startAutomaticEpisodeMatch()
            automaticBangumiAttempted || bangumiJob != null -> Unit
            else -> tryResolveBangumi()
        }
    }

    fun retry() {
        val source = repository.defaultSource() ?: return
        if (!source.isAvailable()) {
            _state.update {
                it.copy(
                    source = source.metadata,
                    status = DanmakuPlaybackStatus.Unavailable("弹幕源尚未配置"),
                )
            }
            return
        }
        if (currentSession == null) {
            currentEpisodeContext?.let {
                currentSession = requestCoordinator.begin(it.playbackKey)
            }
        }
        _state.update { it.copy(source = source.metadata) }
        if (_state.value.bangumiSelection == null) {
            automaticBangumiAttempted = false
            tryResolveBangumi()
        } else {
            startAutomaticEpisodeMatch()
        }
    }

    fun beginManualMatch() {
        val bangumi = currentBangumiContext ?: return
        val sourceId = _state.value.source?.id ?: repository.defaultSource()?.metadata?.id ?: return
        _state.update { current ->
            val manualMatch = current.manualMatch
                ?: current.bangumiSelection?.let {
                    createManualMatchState(
                        selection = it,
                        playbackStatus = current.status,
                        query = bangumi.title,
                    )
                }
                ?: DanmakuManualMatchState(
                    sourceId = sourceId,
                    query = bangumi.title,
                )
            current.copy(
                manualMatch = manualMatch,
                isManualMatchVisible = true,
            )
        }
    }

    fun dismissManualMatch() {
        _state.update { it.copy(isManualMatchVisible = false) }
    }

    fun updateManualQuery(query: String) {
        val manual = _state.value.manualMatch ?: return
        if (query == manual.query) return
        invalidateManualRequests()
        _state.update { current ->
            current.manualMatch?.let {
                current.copy(
                    manualMatch = it.copy(
                        query = query,
                        page = DanmakuMatchPage.BANGUMI,
                        hasSearched = false,
                        isSearching = false,
                        candidates = emptyList(),
                        selectedBangumi = null,
                        isLoadingEpisodes = false,
                        episodes = emptyList(),
                        selectedEpisode = null,
                        errorMessage = null,
                    ),
                )
            } ?: current
        }
    }

    fun searchManualMatch() {
        val manual = _state.value.manualMatch ?: return
        val generation = beginManualRequest()
        _state.update { current ->
            current.copy(
                manualMatch = manual.copy(
                    page = DanmakuMatchPage.BANGUMI,
                    hasSearched = true,
                    isSearching = true,
                    candidates = emptyList(),
                    selectedBangumi = null,
                    episodes = emptyList(),
                    selectedEpisode = null,
                    errorMessage = null,
                ),
            )
        }
        manualJob = viewModelScope.launch {
            when (val result = repository.searchBangumi(manual.sourceId, manual.query)) {
                is DanmakuResult.Success -> updateManualFor(generation) {
                    it.copy(
                        hasSearched = true,
                        isSearching = false,
                        candidates = result.value,
                    )
                }

                DanmakuResult.CredentialsMissing -> updateManualError(generation, "弹幕源尚未配置")
                is DanmakuResult.Unavailable -> updateManualError(generation, result.message)
                is DanmakuResult.InvalidResponse -> updateManualError(generation, result.message)
                DanmakuResult.Stale -> Unit
            }
        }
    }

    fun selectManualBangumi(bangumi: DanmakuBangumi) {
        val manual = _state.value.manualMatch ?: return
        val generation = beginManualRequest()
        _state.update { current ->
            current.copy(
                manualMatch = manual.copy(
                    page = DanmakuMatchPage.EPISODE,
                    selectedBangumi = bangumi,
                    isLoadingEpisodes = true,
                    episodes = emptyList(),
                    selectedEpisode = null,
                    errorMessage = null,
                ),
            )
        }
        manualJob = viewModelScope.launch {
            when (val result = repository.loadEpisodes(manual.sourceId, bangumi)) {
                is DanmakuResult.Success -> updateManualFor(generation) {
                    it.copy(
                        isLoadingEpisodes = false,
                        selectedBangumi = bangumi,
                        episodes = result.value,
                    )
                }

                DanmakuResult.CredentialsMissing -> updateManualError(
                    generation,
                    "弹幕源尚未配置",
                    loadingEpisodes = true,
                )
                is DanmakuResult.Unavailable -> updateManualError(
                    generation,
                    result.message,
                    loadingEpisodes = true,
                )
                is DanmakuResult.InvalidResponse -> updateManualError(
                    generation,
                    result.message,
                    loadingEpisodes = true,
                )
                DanmakuResult.Stale -> Unit
            }
        }
    }

    fun showBangumiSelection() {
        invalidateManualRequests()
        _state.update { current ->
            val manual = current.manualMatch ?: return@update current
            current.copy(
                manualMatch = manual.copy(
                    page = DanmakuMatchPage.BANGUMI,
                    hasSearched = false,
                    selectedBangumi = null,
                    isSearching = false,
                    candidates = emptyList(),
                    isLoadingEpisodes = false,
                    episodes = emptyList(),
                    selectedEpisode = null,
                    errorMessage = null,
                ),
            )
        }
    }

    fun selectManualEpisode(episode: DanmakuEpisode) {
        val context = currentEpisodeContext ?: return
        val manual = _state.value.manualMatch ?: return
        val selectedBangumi = manual.selectedBangumi ?: return
        if (manual.page != DanmakuMatchPage.EPISODE ||
            episode !in manual.episodes ||
            episode.remoteAnimeId != selectedBangumi.remoteAnimeId
        ) {
            return
        }
        val session = currentSession ?: return
        bangumiJob?.cancel()
        bangumiJob = null
        bangumiGeneration++
        automaticBangumiAttempted = true
        val committedSelection = DanmakuBangumiSelection(
            sourceId = manual.sourceId,
            bangumi = selectedBangumi,
            episodes = manual.episodes,
            origin = DanmakuMatchOrigin.MANUAL,
        )
        invalidateManualRequests()

        val binding = createBinding(
            context = context,
            sourceId = manual.sourceId,
            episode = episode,
            origin = DanmakuMatchOrigin.MANUAL,
        )
        repository.saveBinding(binding)
        _state.update {
            it.copy(
                bangumiSelection = committedSelection,
                manualMatch = null,
                isManualMatchVisible = false,
                status = DanmakuPlaybackStatus.LoadingComments,
            )
        }
        episodeJob?.cancel()
        episodeJob = viewModelScope.launch { loadBoundComments(session, binding) }
    }

    private fun tryResolveBangumi() {
        val context = currentBangumiContext ?: return
        val episodeContext = currentEpisodeContext ?: return
        val source = repository.defaultSource() ?: return
        if (!source.isAvailable() ||
            _state.value.bangumiSelection != null ||
            automaticBangumiAttempted ||
            bangumiJob != null
        ) {
            return
        }

        val existingBinding = repository.binding(episodeContext.playbackKey)
        automaticBangumiAttempted = true
        val generation = ++bangumiGeneration
        _state.update { it.copy(status = DanmakuPlaybackStatus.MatchingBangumi) }
        bangumiJob = viewModelScope.launch {
            if (existingBinding != null) {
                restoreBangumiFromBinding(generation, existingBinding)
            } else {
                matchBangumiAutomatically(generation, source.metadata.id, context)
            }
        }.also { job ->
            job.invokeOnCompletion {
                if (bangumiJob === job) bangumiJob = null
            }
        }
    }

    private suspend fun restoreBangumiFromBinding(
        generation: Long,
        binding: DanmakuBinding,
    ) {
        val bangumi = DanmakuBangumi(
            remoteAnimeId = binding.remoteAnimeId,
            remoteBangumiId = binding.remoteBangumiId,
            title = binding.bangumiTitle,
        )
        when (val result = withBangumiTimeout {
            repository.loadEpisodes(binding.sourceId, bangumi)
        }) {
            is DanmakuResult.Success -> commitBangumiSelection(
                generation = generation,
                selection = DanmakuBangumiSelection(
                    sourceId = binding.sourceId,
                    bangumi = bangumi,
                    episodes = result.value,
                    origin = binding.origin,
                ),
            )

            DanmakuResult.CredentialsMissing -> updateBangumiUnavailable(generation, "弹幕源尚未配置")
            is DanmakuResult.Unavailable -> updateBangumiUnavailable(generation, result.message)
            is DanmakuResult.InvalidResponse -> updateBangumiUnavailable(generation, result.message)
            DanmakuResult.Stale -> Unit
        }
    }

    private suspend fun matchBangumiAutomatically(
        generation: Long,
        sourceId: String,
        context: DanmakuBangumiContext,
    ) {
        when (val matchResult = withBangumiTimeout {
            DanmakuMatchPolicy.matchBangumi(context.title) { query ->
                repository.searchBangumi(sourceId, query)
            }
        }) {
            is DanmakuResult.Success -> {
                val matchedBangumi = matchResult.value.matchedBangumi
                if (matchedBangumi == null) {
                    updateBangumiUnmatched(generation, "未找到可自动匹配的番剧")
                    return
                }
                when (val episodeResult = withBangumiTimeout {
                    repository.loadEpisodes(sourceId, matchedBangumi)
                }) {
                    is DanmakuResult.Success -> commitBangumiSelection(
                        generation = generation,
                        selection = DanmakuBangumiSelection(
                            sourceId = sourceId,
                            bangumi = matchedBangumi,
                            episodes = episodeResult.value,
                            origin = DanmakuMatchOrigin.AUTOMATIC,
                        ),
                    )

                    DanmakuResult.CredentialsMissing -> updateBangumiUnavailable(generation, "弹幕源尚未配置")
                    is DanmakuResult.Unavailable -> updateBangumiUnavailable(generation, episodeResult.message)
                    is DanmakuResult.InvalidResponse -> updateBangumiUnavailable(generation, episodeResult.message)
                    DanmakuResult.Stale -> Unit
                }
            }

            DanmakuResult.CredentialsMissing -> updateBangumiUnavailable(generation, "弹幕源尚未配置")
            is DanmakuResult.Unavailable -> updateBangumiUnavailable(generation, matchResult.message)
            is DanmakuResult.InvalidResponse -> updateBangumiUnavailable(generation, matchResult.message)
            DanmakuResult.Stale -> Unit
        }
    }

    private fun commitBangumiSelection(
        generation: Long,
        selection: DanmakuBangumiSelection,
    ) {
        if (generation != bangumiGeneration || currentBangumiContext == null) return
        val currentSelection = _state.value.bangumiSelection
        if (currentSelection?.origin == DanmakuMatchOrigin.MANUAL) return
        _state.update { current ->
            val manualMatch = current.manualMatch
            val syncedManualMatch = if (
                manualMatch != null &&
                manualMatch.canAdoptAutomaticSelection(currentBangumiContext)
            ) {
                createManualMatchState(
                    selection = selection,
                    playbackStatus = current.status,
                    query = currentBangumiContext?.title ?: selection.bangumi.title,
                )
            } else {
                manualMatch
            }
            current.copy(
                bangumiSelection = selection,
                manualMatch = syncedManualMatch,
                status = if (currentEpisodeContext == null) {
                    DanmakuPlaybackStatus.Unmatched("等待选择播放选集")
                } else {
                    DanmakuPlaybackStatus.MatchingEpisode
                },
            )
        }
        startAutomaticEpisodeMatch()
    }

    private fun startAutomaticEpisodeMatch() {
        val context = currentEpisodeContext ?: return
        val selection = _state.value.bangumiSelection ?: return
        val session = currentSession ?: return
        episodeJob?.cancel()
        _state.update { it.copy(status = DanmakuPlaybackStatus.MatchingEpisode) }
        episodeJob = viewModelScope.launch {
            val episode = DanmakuMatchPolicy.matchEpisode(
                request = DanmakuEpisodeMatchRequest(context.sortedEpisodePosition),
                episodes = selection.episodes,
            ).matchedEpisodeOrNull()
            if (!requestCoordinator.isCurrent(session) ||
                _state.value.bangumiSelection !== selection ||
                currentEpisodeContext != context
            ) {
                return@launch
            }
            if (episode == null) {
                _state.update {
                    it.copy(status = DanmakuPlaybackStatus.Unmatched("当前排序位置没有对应的弹幕选集"))
                }
                return@launch
            }
            val binding = createBinding(
                context = context,
                sourceId = selection.sourceId,
                episode = episode,
                origin = DanmakuMatchOrigin.AUTOMATIC,
            )
            repository.saveBinding(binding)
            _state.updateFor(session) { current ->
                current.copy(
                    manualMatch = current.manualMatch?.withSelectedEpisode(
                        selection = selection,
                        episode = episode,
                    ),
                )
            }
            loadBoundComments(session, binding)
        }
    }

    private suspend fun loadBoundComments(
        session: DanmakuRequestSession,
        binding: DanmakuBinding,
    ) {
        _state.updateFor(session) { it.copy(status = DanmakuPlaybackStatus.LoadingComments) }
        when (val result = requestCoordinator.loadComments(session, binding)) {
            is DanmakuResult.Success -> _state.updateFor(session) {
                it.copy(
                    status = if (result.value.isEmpty()) {
                        DanmakuPlaybackStatus.Empty(binding)
                    } else {
                        DanmakuPlaybackStatus.Matched(binding, result.value, result.fromCache)
                    },
                )
            }

            DanmakuResult.CredentialsMissing -> updateEpisodeUnavailable(session, "弹幕源尚未配置")
            is DanmakuResult.Unavailable -> updateEpisodeUnavailable(session, result.message)
            is DanmakuResult.InvalidResponse -> updateEpisodeUnavailable(session, result.message)
            DanmakuResult.Stale -> Unit
        }
    }

    private fun createBinding(
        context: DanmakuEpisodeContext,
        sourceId: String,
        episode: DanmakuEpisode,
        origin: DanmakuMatchOrigin,
    ): DanmakuBinding {
        val now = System.currentTimeMillis()
        return DanmakuBinding(
            playbackKey = context.playbackKey,
            sourceId = sourceId,
            remoteEpisodeId = episode.remoteEpisodeId,
            remoteAnimeId = episode.remoteAnimeId,
            remoteBangumiId = episode.remoteBangumiId,
            bangumiTitle = episode.bangumiTitle,
            episodeTitle = episode.episodeTitle,
            timeOffsetMillis = episode.timeOffsetMillis,
            origin = origin,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
    }

    private suspend fun <T> withBangumiTimeout(
        request: suspend () -> DanmakuResult<T>,
    ): DanmakuResult<T> {
        return withTimeoutOrNull(BANGUMI_REQUEST_TIMEOUT_MILLIS) { request() }
            ?: DanmakuResult.Unavailable("弹幕请求超时")
    }

    private fun updateBangumiUnmatched(generation: Long, message: String) {
        if (generation != bangumiGeneration || _state.value.bangumiSelection != null) return
        _state.update { it.copy(status = DanmakuPlaybackStatus.Unmatched(message)) }
    }

    private fun updateBangumiUnavailable(generation: Long, message: String) {
        if (generation != bangumiGeneration || _state.value.bangumiSelection != null) return
        _state.update { it.copy(status = DanmakuPlaybackStatus.Unavailable(message)) }
    }

    private fun createManualMatchState(
        selection: DanmakuBangumiSelection,
        playbackStatus: DanmakuPlaybackStatus,
        query: String,
    ): DanmakuManualMatchState {
        val selectedEpisodeId = when (playbackStatus) {
            is DanmakuPlaybackStatus.Matched -> playbackStatus.binding.remoteEpisodeId
            is DanmakuPlaybackStatus.Empty -> playbackStatus.binding.remoteEpisodeId
            else -> null
        }
        return DanmakuManualMatchState(
            sourceId = selection.sourceId,
            query = query,
            page = DanmakuMatchPage.EPISODE,
            selectedBangumi = selection.bangumi,
            episodes = selection.episodes,
            selectedEpisode = selection.episodes.firstOrNull {
                it.remoteEpisodeId == selectedEpisodeId
            },
        )
    }

    private fun DanmakuManualMatchState.canAdoptAutomaticSelection(
        context: DanmakuBangumiContext?,
    ): Boolean {
        return page == DanmakuMatchPage.BANGUMI &&
            selectedBangumi == null &&
            !hasSearched &&
            candidates.isEmpty() &&
            query == context?.title
    }

    private fun DanmakuManualMatchState.withSelectedEpisode(
        selection: DanmakuBangumiSelection,
        episode: DanmakuEpisode,
    ): DanmakuManualMatchState {
        return if (
            page == DanmakuMatchPage.EPISODE &&
            selectedBangumi?.remoteAnimeId == selection.bangumi.remoteAnimeId
        ) {
            copy(selectedEpisode = episode)
        } else {
            this
        }
    }

    private fun updateEpisodeUnavailable(session: DanmakuRequestSession, message: String) {
        _state.updateFor(session) { it.copy(status = DanmakuPlaybackStatus.Unavailable(message)) }
    }

    private fun beginManualRequest(): Long {
        manualJob?.cancel()
        return ++manualRequestGeneration
    }

    private fun invalidateManualRequests() {
        manualJob?.cancel()
        manualJob = null
        manualRequestGeneration++
    }

    private fun updateManualError(
        generation: Long,
        message: String,
        loadingEpisodes: Boolean = false,
    ) {
        updateManualFor(generation) {
            it.copy(
                hasSearched = if (loadingEpisodes) it.hasSearched else true,
                isSearching = false,
                isLoadingEpisodes = if (loadingEpisodes) false else it.isLoadingEpisodes,
                errorMessage = message,
            )
        }
    }

    private inline fun updateManualFor(
        generation: Long,
        transform: (DanmakuManualMatchState) -> DanmakuManualMatchState,
    ) {
        _state.update { current ->
            val manual = current.manualMatch
            if (generation != manualRequestGeneration || manual == null) {
                current
            } else {
                current.copy(manualMatch = transform(manual))
            }
        }
    }

    private fun MutableStateFlow<DanmakuPlaybackState>.updateFor(
        session: DanmakuRequestSession,
        transform: (DanmakuPlaybackState) -> DanmakuPlaybackState,
    ) {
        if (requestCoordinator.isCurrent(session)) update(transform)
    }

    private fun resetBangumiSession() {
        bangumiJob?.cancel()
        bangumiJob = null
        episodeJob?.cancel()
        episodeJob = null
        invalidateManualRequests()
        requestCoordinator.invalidate()
        currentSession = null
        _state.update {
            it.copy(
                bangumiSelection = null,
                manualMatch = null,
                isManualMatchVisible = false,
            )
        }
        automaticBangumiAttempted = false
        bangumiGeneration++
    }

    override fun onCleared() {
        resetBangumiSession()
        super.onCleared()
    }

    private companion object {
        const val BANGUMI_REQUEST_TIMEOUT_MILLIS = 15_000L
    }
}
