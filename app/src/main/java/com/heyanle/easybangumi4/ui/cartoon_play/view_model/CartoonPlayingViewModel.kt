package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.Log
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceException
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.plugin.source.utils.network.WebViewHelperV2Impl
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.player.mpv.MpvPlaybackController
import com.heyanle.easybangumi4.player.mpv.MpvAnime4KStatus
import com.heyanle.easybangumi4.player.exo.ExoAdAudioProbeController
import com.heyanle.easybangumi4.plugin.source.utils.VerificationHelper
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.CartoonRecordedModel
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import loli.ball.easyplayer2.EasyPlaybackState
import loli.ball.easyplayer2.EasyPlayerController
import loli.ball.easyplayer2.ExoEasyPlayerController
import loli.ball.easyplayer2.render.EasyPlayerRender
import loli.ball.easyplayer2.texture.TexturePlayerRender
import java.io.File

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
@UnstableApi
class CartoonPlayingViewModel(
) : ViewModel(), Player.Listener, TextureView.SurfaceTextureListener {

    data class PlaybackDiagnostic(
        val mediaUrl: String,
        val headers: Map<String, String>,
    )

    internal enum class ContinuationAction {
        KEEP_PLAYER,
        LOAD_RESOLVED_MEDIA,
        RESOLVE_SOURCE,
    }

    companion object {
        const val TAG = "CartoonPlayingViewModel"

        /**
         * Source resolution and player attachment are separate stages. Once a target has a
         * resolved [PlayerInfo], losing a Surface/player media queue must never call the source
         * plugin again; the player can rebuild its own connection from the resolved URI.
         */
        internal fun continuationAction(
            hasResolvedTarget: Boolean,
            playerHasMedia: Boolean,
        ): ContinuationAction = when {
            hasResolvedTarget && playerHasMedia -> ContinuationAction.KEEP_PLAYER
            hasResolvedTarget -> ContinuationAction.LOAD_RESOLVED_MEDIA
            else -> ContinuationAction.RESOLVE_SOURCE
        }

        internal fun isSamePlaybackTarget(
            previousSummary: CartoonSummary?,
            previousPlayLine: PlayLine?,
            previousEpisode: Episode?,
            nextSummary: CartoonSummary,
            nextPlayLine: PlayLine,
            nextEpisode: Episode,
        ): Boolean {
            return previousSummary == nextSummary &&
                previousPlayLine == nextPlayLine &&
                previousEpisode == nextEpisode
        }
    }

    /**
     * 页面跳转会销毁播放器视图，并可能间接清空 ExoPlayer 的媒体队列，但导航返回时
     * [CartoonPlayingViewModel] 本身仍然存活。恢复点因此由 ViewModel 持有，而不是交给
     * Composable 或异步落库的历史记录，避免返回页面时从 0 重新加载。
     */
    internal class PlaybackResumeCheckpoint {
        data class ResumeDirective(
            val positionMs: Long,
            val playWhenReady: Boolean,
        )

        private data class Target(
            val summary: CartoonSummary,
            val playLine: PlayLine,
            val episode: Episode,
        )

        private var target: Target? = null
        private var positionMs: Long = -1L
        private var playWhenReady: Boolean = true

        fun capture(
            summary: CartoonSummary?,
            playLine: PlayLine?,
            episode: Episode?,
            positionMs: Long,
            playWhenReady: Boolean,
        ) {
            if (summary == null || playLine == null || episode == null || positionMs < 0L) {
                return
            }
            target = Target(summary, playLine, episode)
            this.positionMs = positionMs
            this.playWhenReady = playWhenReady
        }

        /**
         * 显式传入的播放进度（例如从历史页进入）优先于页面恢复点。恢复点只消费一次，
         * 且仅能用于完全相同的番剧、线路和选集，避免串到用户主动切换的新内容。
         */
        fun consume(
            summary: CartoonSummary,
            playLine: PlayLine,
            episode: Episode,
            explicitPositionMs: Long,
        ): ResumeDirective {
            val isMatchingCheckpoint = target == Target(summary, playLine, episode)
            val checkpointPosition = positionMs.takeIf {
                explicitPositionMs < 0L && isMatchingCheckpoint
            } ?: -1L
            val directive = ResumeDirective(
                positionMs = if (explicitPositionMs >= 0L) explicitPositionMs else checkpointPosition,
                playWhenReady = if (isMatchingCheckpoint) playWhenReady else true,
            )
            clear()
            return directive
        }

        private fun clear() {
            target = null
            positionMs = -1L
            playWhenReady = true
        }
    }

    // 播放器状态 =================================================
    private val settingPreferences: SettingPreferences by Inject.injectLazy()
    private fun buildPlayer(): ExoPlayer = ExoPlayer.Builder(APP)
        .setSeekBackIncrementMs(settingPreferences.fastSecond.get() * 1_000L)
        .setSeekForwardIncrementMs(settingPreferences.fastSecond.get() * 1_000L)
        .build()
        .apply {
            addListener(this@CartoonPlayingViewModel)
        }
    val exoPlayer: ExoPlayer = buildPlayer()
    private val exoPlayerController = ExoEasyPlayerController(exoPlayer)
    private val requestedPlaybackEngine = settingPreferences.playbackEngine.get()
    private val mpvPlayerController = if (
        requestedPlaybackEngine == SettingPreferences.PlaybackEngine.MPV &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    ) {
        runCatching { MpvPlaybackController(APP, settingPreferences) }
            .onFailure { Log.e(TAG, "mpv init failed; fallback to ExoPlayer", it) }
            .getOrNull()
    } else {
        null
    }
    val playerController: EasyPlayerController = mpvPlayerController ?: exoPlayerController
    val activePlaybackEngine: SettingPreferences.PlaybackEngine = if (mpvPlayerController != null) {
        SettingPreferences.PlaybackEngine.MPV
    } else {
        SettingPreferences.PlaybackEngine.EXO_PLAYER
    }
    val isMpvEngine: Boolean get() = activePlaybackEngine == SettingPreferences.PlaybackEngine.MPV
    private val exoAdAudioProbeController = if (isMpvEngine) null else {
        ExoAdAudioProbeController(APP, exoPlayer, settingPreferences)
    }

    // 渲染器 =================================================
    private fun buildTextureRenderer(): TexturePlayerRender =
        TexturePlayerRender().apply {
            setExtSurfaceTextureListener(this@CartoonPlayingViewModel)
        }
    // ExoPlayer 也统一使用 TextureView，恢复截图与录屏依赖的可读像素帧；mpv 仍沿用
    // TextureView 以保持横竖屏切换时的 BufferQueue。
    val render: EasyPlayerRender = buildTextureRenderer()
    val easyTextRenderer: TexturePlayerRender get() = render as TexturePlayerRender

    // 当前播放番剧缓存 =================================================
    private var cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState? = null
    private var playingPlayLine: PlayLine? = null
    private var playingEpisode: Episode? = null
    private var playingInfo: PlayerInfo? = null
    private var playingInfoIsCache: Boolean = false
    private var resolvedPlayback: ResolvedPlayback? = null
    private var forceNoCacheRetrying: Boolean = false
    private var forceClearMediaCacheRetrying: Boolean = false
    private val playbackResumeCheckpoint = PlaybackResumeCheckpoint()

    // 播放状态 =================================================
    data class PlayingState(
        val isLoading: Boolean = true,
        val loadingPhase: LoadingPhase = LoadingPhase.SOURCE_RESOLUTION,
        val isPlaying: Boolean = false,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val errorThrowable: Throwable? = null
    )

    enum class LoadingPhase {
        SOURCE_RESOLUTION,
        PLAYER_CONNECTION,
    }

    private data class ResolvedPlayback(
        val summary: CartoonSummary,
        val playLine: PlayLine,
        val episode: Episode,
        val playerInfo: PlayerInfo,
        val canMediaCache: Boolean,
        val sourceResultIsCache: Boolean,
    ) {
        fun matches(state: CartoonPlayViewModel.CartoonPlayState): Boolean =
            summary == state.cartoonSummary &&
                playLine == state.playLine.playLine &&
                episode == state.episode
    }

    private val _playingState = MutableStateFlow<PlayingState>(PlayingState())
    val playingState = _playingState.asStateFlow()

    // 协程
    private val dispatcher = CoroutineProvider.newSingleDispatcher
    private val singleScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val scope = MainScope()

    // 任务管理 =================================================
    // 加载任务
    private var lastJob: Job? = null

    // 获取缩略图任务
    private var thumbnailJob: Job? = null

    // 其他模块注入 =================================================
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()
    private val cartoonMediaSourceFactory: CartoonMediaSourceFactory by Inject.injectLazy()
    private val sourceStateCase: SourceStateCase by Inject.injectLazy()
    init {
        playerController.addListener(object : EasyPlayerController.Listener {
            override fun onPlaybackStateChanged(state: EasyPlaybackState) {
                when (state) {
                    EasyPlaybackState.BUFFERING -> _playingState.update {
                        it.copy(
                            isLoading = true,
                            loadingPhase = LoadingPhase.PLAYER_CONNECTION,
                        )
                    }
                    EasyPlaybackState.READY -> {
                        duringTemp = playerController.duration
                        _playingState.update {
                            it.copy(isLoading = false, isPlaying = true, isError = false)
                        }
                    }
                    EasyPlaybackState.ENDED -> {
                        trySaveHistory(playerController.duration)
                        _playingState.update { it.copy(isLoading = false, isPlaying = false) }
                    }
                    EasyPlaybackState.IDLE -> Unit
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean) {
                if (!playWhenReady && playerController.hasMedia) trySaveHistory()
            }
        })
    }

    // 各种配置（找机会拆单独一个 ViewModel 和播放无关 =================================================
    private val customSpeedPref = settingPreferences.customSpeed
    val customSpeed = customSpeedPref.stateIn(viewModelScope)
    val isCustomSpeed = mutableStateOf(false)


    val videoScaleTypeSelection = settingPreferences.scaleTypeSelection
    private val videoScaleTypePref = settingPreferences.videoScaleType
    val videoScaleType = videoScaleTypePref.stateIn(viewModelScope)
    val mpvAnime4kEnabled = settingPreferences.mpvAnime4kEnabled.stateIn(viewModelScope)
    val mpvAnime4kPreset = settingPreferences.mpvAnime4kPreset.stateIn(viewModelScope)
    val exoAdAudioProbeEnabled = settingPreferences.exoAdAudioProbeEnabled.stateIn(viewModelScope)
    val exoAdAudioProbeRulesUrl = settingPreferences.exoAdAudioProbeRulesUrl.stateIn(viewModelScope)
    private val disabledAnime4KStatus = MutableStateFlow(MpvAnime4KStatus())
    val mpvAnime4KStatus: StateFlow<MpvAnime4KStatus> =
        mpvPlayerController?.anime4KStatus ?: disabledAnime4KStatus

    val isCustomSpeedDialog = mutableStateOf(false)

    val fastWeight = settingPreferences.fastWeight.stateIn(viewModelScope)
    val fastSecond = settingPreferences.fastSecond.stateIn(viewModelScope)

    val fastTopSecond = settingPreferences.fastTopSecond.stateIn(viewModelScope)

    val fastTopWeightMolecule = settingPreferences.fastWeightTopMolecule.stateIn(viewModelScope)
    val fastWeightTopDenominator = settingPreferences.fastWeightTopDenominator

    val playerSeekFullWidthTimeMS =
        settingPreferences.playerSeekFullWidthTimeMS.stateIn(viewModelScope)

    val defaultSpeed = settingPreferences.defaultSpeed.stateIn(viewModelScope)

    // 剪辑模式
    val showRecording = mutableStateOf<CartoonRecordedModel?>(null)

    // 缩略图缓存
    var thumbnailBuffer: ThumbnailBuffer? = null
    val thumbnailFolder: File = File(APP.getCachePath("thumbnail")).apply { mkdirs() }

    init {
        // 清理上次遗留的缩略图缓存；deleteRecursively 是进入播放页的主线程热点，移到 IO。
        clearThumbnailFolderAsync()
    }

    /** 在 IO 线程清理缩略图目录。缩略图缓存本质 best-effort，删除与新建并发安全（见 ThumbnailBuffer）。 */
    private fun clearThumbnailFolderAsync() {
        singleScope.launch(Dispatchers.IO) {
            runCatching { thumbnailFolder.deleteRecursively() }
            thumbnailFolder.mkdirs()
        }
    }

    @OptIn(UnstableApi::class)
    fun showRecord() {
        if (isMpvEngine) {
            "mpv 引擎暂不支持截图与录制".moeSnackBar()
            return
        }
        if (showRecording.value != null) {
            return
        }
        val playerInfo = playingInfo
        if (playerInfo == null) {
            stringRes(com.heyanle.easy_i18n.R.string.waiting_parsing)
            return
        }
        showRecording.value = CartoonRecordedModel(
            APP,
            exoPlayer,
            playerInfo,
            cartoonMediaSourceFactory,
            scope,
            thumbnailBuffer ?: ThumbnailBuffer(thumbnailFolder),
            Math.max(0, exoPlayer.currentPosition - 30000),
            Math.min(exoPlayer.currentPosition + 30000, exoPlayer.duration),
            exoPlayer.currentPosition
        )
    }

    fun setCustomSpeedDialog() {
        isCustomSpeedDialog.value = true
    }

    fun setCustomSpeed(speed: Float) {
        customSpeedPref.set(speed)
        if (speed <= 0) {
            isCustomSpeed.value = false
        }
    }

    fun enableCustomSpeed() {
        if (customSpeed.value <= 0) {
            setCustomSpeedDialog()
        } else {
            isCustomSpeed.value = true
        }
    }

    fun disableCustomSpeed() {
        isCustomSpeed.value = false
    }

    fun setVideoScaleType(scaleType: Int) {
        videoScaleTypePref.set(scaleType)
    }

    fun setMpvAnime4kEnabled(enabled: Boolean) {
        settingPreferences.mpvAnime4kEnabled.set(enabled)
        mpvPlayerController?.applyAnime4K()
    }

    fun setMpvAnime4kPreset(preset: SettingPreferences.MpvAnime4KPreset) {
        settingPreferences.mpvAnime4kPreset.set(preset)
        mpvPlayerController?.applyAnime4K()
    }

    fun setExoAdAudioProbeEnabled(enabled: Boolean) {
        settingPreferences.exoAdAudioProbeEnabled.set(enabled)
        exoAdAudioProbeController?.refreshConfiguration()
    }

    fun setExoAdAudioProbeRulesUrl(url: String) {
        settingPreferences.exoAdAudioProbeRulesUrl.set(url.trim())
        exoAdAudioProbeController?.refreshConfiguration()
    }

    // 刷新 & 播放 ===================================

    fun tryRefresh() {
        lastJob?.cancel()
        lastJob = scope.launch {
            cartoonPlayingState?.let {
                innerPlay(it, 0)
            }
        }

    }

    private fun tryRefreshNoCache() {
        if (forceNoCacheRetrying) return
        forceNoCacheRetrying = true
        lastJob?.cancel()
        lastJob = scope.launch {
            cartoonPlayingState?.let {
                innerPlay(it, playerController.currentPosition, canCache = false)
            }
        }
    }

    fun changePlay(
        cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState?,
        adviceProcess: Long,
    ) {
        val previousRequestedState = this.cartoonPlayingState
        val sameRequestedTarget = cartoonPlayingState != null && previousRequestedState != null &&
            isSamePlaybackTarget(
                previousSummary = previousRequestedState.cartoonSummary,
                previousPlayLine = previousRequestedState.playLine.playLine,
                previousEpisode = previousRequestedState.episode,
                nextSummary = cartoonPlayingState.cartoonSummary,
                nextPlayLine = cartoonPlayingState.playLine.playLine,
                nextEpisode = cartoonPlayingState.episode,
            )
        if (sameRequestedTarget && lastJob?.isActive == true) {
            val duplicateTarget = checkNotNull(cartoonPlayingState)
            "play-target action=ignore-duplicate-active source=${duplicateTarget.cartoonSummary.source} cartoonId=${duplicateTarget.cartoonSummary.id} lineId=${duplicateTarget.playLine.playLine.id} episodeId=${duplicateTarget.episode.id}".logi(TAG)
            return
        }
        lastJob?.cancel()
        lastJob = scope.launch {
            val previousSummary = previousRequestedState?.cartoonSummary
            val previousPlayerUri = playingInfo?.uri
            this@CartoonPlayingViewModel.cartoonPlayingState = cartoonPlayingState
            if (cartoonPlayingState == null) {
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        isError = false
                    )
                }
            } else {
                val resumeDirective = playbackResumeCheckpoint.consume(
                    summary = cartoonPlayingState.cartoonSummary,
                    playLine = cartoonPlayingState.playLine.playLine,
                    episode = cartoonPlayingState.episode,
                    explicitPositionMs = adviceProcess,
                )
                val resolved = resolvedPlayback?.takeIf { it.matches(cartoonPlayingState) }
                val action = continuationAction(
                    hasResolvedTarget = resolved != null,
                    playerHasMedia = playerController.hasMedia,
                )
                "play-target previousSource=${previousSummary?.source} previousId=${previousSummary?.id} previousUri=$previousPlayerUri nextSource=${cartoonPlayingState.cartoonSummary.source} nextId=${cartoonPlayingState.cartoonSummary.id} lineId=${cartoonPlayingState.playLine.playLine.id} episodeId=${cartoonPlayingState.episode.id} action=$action vm=${System.identityHashCode(this@CartoonPlayingViewModel)}".logi(TAG)
                when (action) {
                    ContinuationAction.KEEP_PLAYER -> {
                        if (resumeDirective.positionMs >= 0) {
                            playerController.seekTo(resumeDirective.positionMs)
                        }
                        playerController.playWhenReady = resumeDirective.playWhenReady
                        _playingState.update {
                            it.copy(
                                isLoading = false,
                                isPlaying = true,
                                isError = false,
                            )
                        }
                    }
                    ContinuationAction.LOAD_RESOLVED_MEDIA -> {
                        val cached = checkNotNull(resolved)
                        "play-media action=restore-resolved uri=${cached.playerInfo.uri} source=${cached.summary.source} cartoonId=${cached.summary.id}".logi(TAG)
                        playingInfoIsCache = cached.sourceResultIsCache
                        innerPlay(
                            playerInfo = cached.playerInfo,
                            adviceProcess = resumeDirective.positionMs,
                            canMediaCache = cached.canMediaCache,
                            playWhenReady = resumeDirective.playWhenReady,
                        )
                    }
                    ContinuationAction.RESOLVE_SOURCE -> innerPlay(
                        cartoonPlayingState = cartoonPlayingState,
                        adviceProcess = resumeDirective.positionMs,
                        playWhenReady = resumeDirective.playWhenReady,
                    )
                }
            }
        }

    }

    /**
     * 调用外部播放器播放，当前状态为 Playing 时候才有效
     * @return 当前播放状态是否为 Playing
     */
    fun playCurrentExternal(): Boolean {
        val oldPlayingState = _playingState.value
        if (oldPlayingState.isPlaying) {
            val playerInfo = playingInfo ?: return false
            innerPlayExternal(playerInfo)
            return true
        }
        return false
    }

    fun hasCustomPlaybackHeaders(): Boolean = playingInfo?.header?.isNotEmpty() == true

    fun playbackDiagnostic(): PlaybackDiagnostic? = playingInfo?.let {
        PlaybackDiagnostic(it.uri, it.header.orEmpty())
    }

    /**
     * 调用外部播放器播放
     */
    private fun innerPlayExternal(playerInfo: PlayerInfo) {
        var uri = playerInfo.uri.toUri()
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                try {
                    uri = FileProvider.getUriForFile(
                        APP,
                        APP.packageName + ".provider",
                        file
                    )
                }catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        }
        APP.startActivity(Intent("android.intent.action.VIEW").apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // for mx player https://mx.j2inter.com/api
            putExtra("video_list", arrayOf(playerInfo.uri.toUri()))
            val list = arrayListOf<String>()
            playerInfo.header?.iterator()?.forEach {
                list.add(it.key)
                list.add(it.value)
            }
            putExtra("headers", list.toTypedArray())
        })
    }

    val webViewHelperV2Impl: WebViewHelperV2Impl by Inject.injectLazy()

    // 很 hard 但是不管了
    private var verificationResultTemp: VerificationResult? = null
    private var verificationTempSummary: CartoonSummary? = null
    private var verificationTempLine: PlayLine? = null
    private var verificationTempEpisode: Episode? = null


    private suspend fun innerPlay(
        cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState,
        adviceProcess: Long,
        canCache: Boolean = true,
        playWhenReady: Boolean = true,
    ) {


        playerController.pause()
        _playingState.update {
            it.copy(
                isLoading = true,
                loadingPhase = LoadingPhase.SOURCE_RESOLUTION,
            )
        }
        val play = sourceStateCase.awaitBundle().play(cartoonPlayingState.cartoonSummary.source)
        if (play == null) {
            _playingState.update {
                it.copy(
                    isLoading = false,
                    isError = true,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.source_not_found)
                )
            }
            return
        }

        "play-resolve source=${cartoonPlayingState.cartoonSummary.source} cartoonId=${cartoonPlayingState.cartoonSummary.id} lineId=${cartoonPlayingState.playLine.playLine.id} episodeId=${cartoonPlayingState.episode.id} canCache=$canCache".logi(TAG)

        val verificationResult = verificationResultTemp
        val tsummary = verificationTempSummary
        val tline = verificationTempLine
        val tepisode = verificationTempEpisode
        if (verificationResult != null && tsummary != null && tline != null && tepisode != null &&
            tsummary == cartoonPlayingState.cartoonSummary
            && tline == cartoonPlayingState.playLine.playLine
            && tepisode == cartoonPlayingState.episode
            ) {
            verificationResultTemp = null
            verificationTempSummary = null
            verificationTempLine = null
            verificationTempEpisode = null
            cartoonPlayingState.cartoon?.let { cartoon ->
                play.getPlayInfo(
                    cartoon,
                    cartoonPlayingState.playLine.playLine,
                    cartoonPlayingState.episode,
                    verificationResult,
                    canCache = canCache,
                )
            } ?: play.getPlayInfo(
                cartoonPlayingState.cartoonSummary,
                cartoonPlayingState.playLine.playLine,
                cartoonPlayingState.episode,
                verificationResult,
                canCache = canCache,
            )
        } else {
            verificationResultTemp = null
            verificationTempSummary = null
            verificationTempLine = null
            verificationTempEpisode = null
            cartoonPlayingState.cartoon?.let { cartoon ->
                play.getPlayInfo(
                    cartoon,
                    cartoonPlayingState.playLine.playLine,
                    cartoonPlayingState.episode,
                    canCache = canCache,
                )
            } ?: play.getPlayInfo(
                cartoonPlayingState.cartoonSummary,
                cartoonPlayingState.playLine.playLine,
                cartoonPlayingState.episode,
                canCache = canCache,
            )
        }
            .complete {
                yield()
                "play-resolve action=complete source=${cartoonPlayingState.cartoonSummary.source} cartoonId=${cartoonPlayingState.cartoonSummary.id} uri=${it.data.uri} resultCache=${it.isCache}".logi(TAG)
                playingPlayLine = cartoonPlayingState.playLine.playLine
                playingEpisode = cartoonPlayingState.episode
                playingInfoIsCache = it.isCache
                resolvedPlayback = ResolvedPlayback(
                    summary = cartoonPlayingState.cartoonSummary,
                    playLine = cartoonPlayingState.playLine.playLine,
                    episode = cartoonPlayingState.episode,
                    playerInfo = it.data,
                    canMediaCache = canCache,
                    sourceResultIsCache = it.isCache,
                )
                forceNoCacheRetrying = false
                innerPlay(
                    playerInfo = it.data,
                    adviceProcess = adviceProcess,
                    canMediaCache = canCache,
                    playWhenReady = playWhenReady,
                )
            }
            .error { state ->
                yield()
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMsg = state.throwable?.message?:"解析失败",
                        errorThrowable = state.throwable
                    )
                }
            }


    }

    fun onSearchNeedWebCheck(
        playInfoNeedWebViewCheckBusinessException: PlayInfoNeedVerificationBusinessException,
    ){
        scope.launch {
            val request = playInfoNeedWebViewCheckBusinessException.request
            if (request != null) {
                verificationTempSummary = request.summary
                verificationTempLine = request.playLine
                verificationTempEpisode = request.episode
            } else {
                val state = cartoonPlayingState
                verificationTempSummary = state?.cartoonSummary
                verificationTempLine = state?.playLine?.playLine
                verificationTempEpisode = state?.episode
            }
            verificationResultTemp = VerificationHelper.start(
                playInfoNeedWebViewCheckBusinessException.verificationParam,
                webViewHelperV2Impl,
            )
            tryRefresh()
        }

    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private suspend fun innerPlay(
        playerInfo: PlayerInfo,
        adviceProcess: Long,
        canMediaCache: Boolean = true,
        playWhenReady: Boolean = true,
    ) {
        playerController.pause()
        if (lastJob?.isCancelled != false || lastJob?.isActive != true) {
            return
        }
        if (this.playingInfo != null) {
            if (
                playingInfo?.uri == playerInfo.uri
                && playingInfo?.decodeType == playerInfo.decodeType
                && playerController.hasMedia
            ) {
                "play-media action=reuse uri=${playerInfo.uri} source=${cartoonPlayingState?.cartoonSummary?.source} cartoonId=${cartoonPlayingState?.cartoonSummary?.id}".logi(TAG)
                playingInfo = playerInfo
                if (adviceProcess >= 0) {
                    playerController.seekTo(adviceProcess)
                }
                playerController.playWhenReady = playWhenReady
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = true,
                        isError = false
                    )
                }
                return
            }
        }
        thumbnailBuffer?.clear()
        clearThumbnailFolderAsync()
        thumbnailBuffer = ThumbnailBuffer(thumbnailFolder)
        playingInfo = playerInfo
        "play-media action=set uri=${playerInfo.uri} source=${cartoonPlayingState?.cartoonSummary?.source} cartoonId=${cartoonPlayingState?.cartoonSummary?.id} cache=$canMediaCache".logi(TAG)
        if (isMpvEngine) {
            playingInfoIsCache = false
            mpvPlayerController?.load(
                uri = playerInfo.uri,
                headers = playerInfo.header.orEmpty(),
                startPositionMs = adviceProcess.coerceAtLeast(0L),
                playWhenReady = playWhenReady,
            )
            _playingState.update {
                it.copy(
                    isLoading = true,
                    loadingPhase = LoadingPhase.PLAYER_CONNECTION,
                    isPlaying = false,
                    isError = false,
                )
            }
            return
        }
        // Media3 效果管线（VideoGraph）要求 codec 初始化前有有效输出 surface，
        // 否则 renderer 退回 placeholder surface，EGL 渲染报
        // "Make sure the SurfaceView or associated SurfaceHolder has a valid Surface"。
        // 播放前等待渲染视图 surface 就绪（最多 5s）。
        runCatching {
            withTimeoutOrNull(5000) {
                while (true) {
                    val v = render.getViewOrNull()
                    val ready = when (v) {
                        is android.view.SurfaceView -> v.holder?.surface?.isValid == true
                        is android.view.TextureView -> v.isAvailable
                        else -> false
                    }
                    if (ready) break
                    delay(50)
                }
            }
        }
        // 本地番源不过缓存
        val media =
            if (!canMediaCache || cartoonPlayingState?.cartoonSummary?.source?.equals(LocalSource.LOCAL_SOURCE_KEY) == true)
                cartoonMediaSourceFactory.getWithoutCache(playerInfo) else
                cartoonMediaSourceFactory.getWithCache(playerInfo)
        exoPlayer.setMediaSource(media, adviceProcess)
        exoPlayer.prepare()
        duringTemp = -1L
        exoPlayer.playWhenReady = playWhenReady
        exoAdAudioProbeController?.open(
            playerInfo = playerInfo,
            mediaId = currentProbeMediaId(),
        )
        _playingState.update {
            it.copy(
                isLoading = false,
                isPlaying = true,
                isError = false
            )
        }
    }
    var duringTemp = -1L
    fun trySaveHistory(ps: Long = -1) {

        "save1".logi(TAG)
        val line = playingPlayLine ?: return
        val epi = playingEpisode ?: return
        val cartoon = cartoonPlayingState?.cartoonSummary ?: return
        CoroutineProvider.globalMainScope.launch {

            runCatching {
                var po = if (ps >= 0) ps else playerController.currentPosition
                if (ps < 0L) {
                    when (playerController.playbackState) {
                        EasyPlaybackState.BUFFERING, EasyPlaybackState.READY -> {
                            po = playerController.currentPosition
                        }
                        EasyPlaybackState.ENDED -> {
                            if (duringTemp > 0) {
                                po = duringTemp
                            } else {
                                return@launch
                            }
                        }
                        EasyPlaybackState.IDLE -> return@launch
                    }
                }
                "save $po".logi(TAG)
//            if (exoPlayer.playbackState == ExoPlayer.STATE_ENDED)
                val process = po
                cartoonInfoDao.transaction {
                    val old = cartoonInfoDao.getByCartoonSummary(cartoon.id, cartoon.source)
                    if (old != null) {
                        val lineIndex = old.playLine.indexOf(line)
                        if (lineIndex >= 0) {
                            cartoonInfoDao.modify(
                                old.copyHistory(
                                    lineIndex,
                                    line,
                                    epi,
                                    process

                                )
                            )
                        }

                    }
                }
            }

        }
    }

    // onDispose
    fun onExit() {
        val exitPosition = playerController.currentPosition
        playbackResumeCheckpoint.capture(
            summary = cartoonPlayingState?.cartoonSummary,
            playLine = playingPlayLine,
            episode = playingEpisode,
            positionMs = exitPosition,
            playWhenReady = playerController.playWhenReady,
        )
        trySaveHistory(exitPosition)
        lastJob?.cancel()
        playerController.pause()
    }

    // exoPlayer 回调 ==================================================

    override fun onPlaybackStateChanged(playbackState: Int) {
        super<Player.Listener>.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
            exoPlayer.duration.logi(TAG)
            duringTemp = exoPlayer.duration
            forceClearMediaCacheRetrying = false
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super<Player.Listener>.onPlayerError(error)
        var cause: Throwable? = error
        var level = 0
        while (cause != null) {
            Log.e(TAG, "playback cause[$level]: ${cause::class.java.name}: ${cause.message}", cause)
            cause = cause.cause
            level++
        }
        if (error.hasReadPositionOutOfRangeCause() && tryClearMediaCacheAndRetry()) {
            return
        }
        if (playingInfoIsCache) {
            tryRefreshNoCache()
            return
        }
        _playingState.update {
            it.copy(
                isLoading = false,
                isPlaying = false,
                isError = true,
                errorMsg = error.message ?: "play error",
                errorThrowable = error,
            )
        }
    }

    private fun tryClearMediaCacheAndRetry(): Boolean {
        if (forceClearMediaCacheRetrying) return false
        val playerInfo = playingInfo ?: return false
        forceClearMediaCacheRetrying = true
        val position = playerController.currentPosition
        lastJob?.cancel()
        lastJob = scope.launch {
            runCatching {
                cartoonMediaSourceFactory.removeNormalCache(playerInfo)
            }.onFailure {
                Log.e(TAG, "remove media cache failed: ${it.message}", it)
            }
            innerPlay(playerInfo, position, canMediaCache = false)
        }
        return true
    }

    private fun Throwable.hasReadPositionOutOfRangeCause(): Boolean {
        var cause: Throwable? = this
        while (cause != null) {
            if (cause is DataSourceException &&
                cause.reason == PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE
            ) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super<Player.Listener>.onPlayWhenReadyChanged(playWhenReady, reason)
        if (_playingState.value.isPlaying && !playerController.playWhenReady && playerController.hasMedia) {
            trySaveHistory()
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        super<Player.Listener>.onPositionDiscontinuity(oldPosition, newPosition, reason)
        if (reason != Player.DISCONTINUITY_REASON_INTERNAL) {
            exoAdAudioProbeController?.notifyHostDiscontinuity()
        }
    }


    // ViewModel clear

    override fun onCleared() {
        super.onCleared()
        lastJob?.cancel()
        try {
            trySaveHistory()
        }catch (e: Throwable) {
            e.printStackTrace()
        }

        scope.cancel()
        exoAdAudioProbeController?.close()
        mpvPlayerController?.release()
        exoPlayer.release()
    }

    private fun currentProbeMediaId(): String {
        val state = cartoonPlayingState ?: return "easybangumi-unknown"
        return listOf(
            state.cartoonSummary.source,
            state.cartoonSummary.id,
            state.playLine.playLine.id,
            state.episode.id,
        ).joinToString(":" )
    }

    // surfaceTexture 回调 ==============================================

    private var lastThumbnailTime = 0L

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // 耗电有点猛，先关闭
//        //"onSurfaceTextureUpdated 1".logi(TAG)
//        if (thumbnailBuffer == null) {
//            return
//        }
//        scope.launch {
//            //"onSurfaceTextureUpdated 2".logi(TAG)
//            val currentPosition = exoPlayer.currentPosition
//            // 如果该进度前后两秒都没有缩略图就保存一张
//            val currentFile = thumbnailBuffer?.getThumbnail(currentPosition, 2000)
//            val current = System.currentTimeMillis()
//
//            // 频次控制
//            if (currentFile == null && current - lastThumbnailTime > 2000) {
//                lastThumbnailTime = current
//                //"onSurfaceTextureUpdated 3".logi(TAG)
//                // 保存缩略图
//                thumbnailJob?.cancel()
//                thumbnailJob = singleScope.launch {
//                    yield()
//                    val textureView = easyTextRenderer.getTextureViewOrNull() ?: return@launch
//                    val bmp = textureView.bitmap ?: return@launch
//                    thumbnailFolder.mkdirs()
//                    val file = File(thumbnailFolder, "${currentPosition}.jpg")
//                    file.delete()
//                    file.createNewFile()
//                    file.deleteOnExit()
//                    file.outputStream().use {
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 10, it)
//                    }
//                    //"onSurfaceTextureUpdated 4".logi(TAG)
//                    thumbnailBuffer?.addThumbnail(currentPosition, file)
//                    bmp.recycle()
//                }
//
//            }
//        }
    }

    private fun ExoPlayer.isMedia(): Boolean {
        return playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY
    }
}
