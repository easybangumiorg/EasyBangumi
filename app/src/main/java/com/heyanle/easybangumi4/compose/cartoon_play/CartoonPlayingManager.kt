package com.heyanle.easybangumi4.compose.cartoon_play

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Clock
import com.heyanle.bangumi_source_api.api.component.play.PlayComponent
import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.db.dao.CartoonHistoryDao
import com.heyanle.easybangumi4.base.entity.CartoonHistory
import com.heyanle.easybangumi4.compose.common.moeSnackBar
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.source.SourceLibraryController
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by HeYanLe on 2023/3/7 14:45.
 * https://github.com/heyanLE
 */
object CartoonPlayingManager : Player.Listener {

    val defaultScope = MainScope()

    val settingPreference: SettingPreferences by Injekt.injectLazy()
    val sourceLibraryController: SourceLibraryController by Injekt.injectLazy()
    val cartoonHistoryDao: CartoonHistoryDao by Injekt.injectLazy()


    sealed class PlayingState {
        object None : PlayingState()

        class Loading(
            val playLineIndex: Int,
            val playLine: PlayLine,
            val curEpisode: Int,
            val cartoon: Cartoon,
        ) : PlayingState()

        class Playing(
            val playLineIndex: Int,
            val playerInfo: PlayerInfo,
            val playLine: PlayLine,
            val curEpisode: Int,
            val cartoon: Cartoon,
        ) : PlayingState()

        class Error(
            val playLineIndex: Int,
            val errMsg: String,
            val throwable: Throwable?,
            val playLine: PlayLine,
            val curEpisode: Int,
        ) : PlayingState()

        fun playLine(): PlayLine? {
            return when (this) {
                None -> null
                is Loading -> playLine
                is Playing -> playLine
                is Error -> playLine
            }
        }

        fun cartoon(): Cartoon? {
            return when (this) {
                None -> null
                is Loading -> cartoon
                is Playing -> cartoon
                is Error -> cartoon
            }
        }

        fun playLineIndex(): Int? {
            return when (this) {
                None -> null
                is Loading -> playLineIndex
                is Playing -> playLineIndex
                is Error -> playLineIndex
            }
        }

        fun episode(): Int {
            return when (this) {
                None -> -1
                is Loading -> curEpisode
                is Playing -> curEpisode
                is Error -> curEpisode
            }
        }
    }

    var state by mutableStateOf<PlayingState>(PlayingState.None)


    private var lastPlayerInfo: PlayerInfo? = null

    private var playComponent: PlayComponent? = null
    private var cartoon: Cartoon? = null

    private var saveLoopJob: Job? = null


    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(
            APP,
            DefaultRenderersFactory(APP),
            DefaultMediaSourceFactory(APP),
            DefaultTrackSelector(APP),
            DefaultLoadControl(),
            DefaultBandwidthMeter.getSingletonInstance(APP),
            DefaultAnalyticsCollector(Clock.DEFAULT)
        ).build().apply {
            addListener(CartoonPlayingManager)
        }
    }

    suspend fun refresh() {
        val cartoonSummary = cartoon ?: return
        val playComponent = playComponent ?: return
        val playLine = state.playLine() ?: return
        val playIndex = state.playLineIndex() ?: return
        changePlay(playComponent, cartoonSummary, playIndex, playLine, state.episode(), 0)
    }

    suspend fun changeLine(
        sourceKey: String,
        cartoon: Cartoon,
        playLineIndex: Int,
        playLine: PlayLine,
        defaultEpisode: Int = 0,
        defaultProgress: Long = 0L,
    ) {

        val playComponent = sourceLibraryController.sourceBundleFlow.value.play(sourceKey) ?: return
        CartoonPlayingManager.playComponent = playComponent
        CartoonPlayingManager.cartoon = cartoon
        changePlay(playComponent, cartoon, playLineIndex, playLine, defaultEpisode, defaultProgress)
    }

    suspend fun tryNext(
        defaultProgress: Long = 0L,
        isReverse: Boolean = false,
    ): Boolean {
        val playingState = (state as? PlayingState.Playing) ?: return false
        val target = if (isReverse) playingState.curEpisode - 1 else playingState.curEpisode + 1
        if (target < 0 || target >= playingState.playLine.episode.size) {
            return false
        }
        changeEpisode(target, defaultProgress)
        return true
    }

    suspend fun changeEpisode(
        episode: Int,
        defaultProgress: Long = 0L,
    ): Boolean {
        val cartoonSummary = cartoon ?: return false
        val playComponent = playComponent ?: return false
        val playLineIndex = state.playLineIndex() ?: return false
        val playLine = state.playLine() ?: return false
        changePlay(
            playComponent,
            cartoonSummary,
            playLineIndex,
            playLine,
            episode,
            defaultProgress
        )
        return true
    }

    private var lastJob: Job? = null

    private suspend fun changePlay(
        playComponent: PlayComponent,
        cartoon: Cartoon,
        playLineIndex: Int,
        playLine: PlayLine,
        episode: Int = 0,
        adviceProgress: Long = 0L,
    ) {
        val sta = state
        if (sta is PlayingState.Playing) {
            if (sta.cartoon == cartoon && sta.playLineIndex == playLineIndex && episode == sta.curEpisode) {
                innerPlay(sta.playerInfo, adviceProgress)
                return
            }
        }

        exoPlayer.pause()
        if (playLine.episode.isEmpty()) {
            return
        }
        val realEpisode = if (episode < 0 || episode >= playLine.episode.size) 0 else episode

        state = PlayingState.Loading(playLineIndex, playLine, realEpisode, cartoon)

        lastJob?.cancel()
        lastJob = null
        lastJob = defaultScope.launch {
            playComponent.getPlayInfo(
                CartoonSummary(
                    id = cartoon.id,
                    url = cartoon.url,
                    source = cartoon.source
                ),
                playLine,
                episode
            )
                .complete {
                    if (isActive) {
                        innerPlay(it.data, adviceProgress)
                        state = PlayingState.Playing(
                            playLineIndex, it.data, playLine, episode, cartoon
                        )
                    }

                }
                .error {
                    if (isActive) {
                        it.throwable.printStackTrace()
                        error(
                            if (it.isParserError) stringRes(
                                com.heyanle.easy_i18n.R.string.source_error
                            ) else stringRes(com.heyanle.easy_i18n.R.string.loading_error),
                            it.throwable, playLineIndex, playLine, episode
                        )
                    }
                }
        }


    }

    private suspend fun innerTrySaveHistory(ps: Long = -1) {
        if (settingPreference.isInPrivate.get()) {
            return

        }
        var process = ps
        if (ps == -1L) {
            process = exoPlayer.currentPosition
        }
        if (process == -1L) {
            process = 0L
        }
        val playLineIndex = state.playLineIndex() ?: return
        val playLine = state.playLine() ?: return
        val curEpisode = state.episode() ?: return


        val curCartoon = cartoon ?: return

        val history = CartoonHistory(
            id = curCartoon.id,
            source = curCartoon.source,
            url = curCartoon.url,

            cover = curCartoon.coverUrl ?: "",
            name = curCartoon.title,
            intro = curCartoon.intro ?: "",

            lastLinesIndex = playLineIndex,
            lastLineTitle = playLine.label,
            lastEpisodeIndex = curEpisode,
            lastEpisodeTitle = playLine.episode[curEpisode],
            lastProcessTime = process,

            createTime = System.currentTimeMillis()
        )
        withContext(Dispatchers.IO) {
            cartoonHistoryDao.modify(history)
        }

    }

    fun trySaveHistory(ps: Long = -1) {
        if (settingPreference.isInPrivate.get()) {
            return
        }
        var process = ps
        if (ps == -1L) {
            process = exoPlayer.currentPosition
        }
        if (process == -1L) {
            process = 0L
        }
        val curCartoon = cartoon ?: return
        val playLineIndex = state.playLineIndex() ?: return
        val playLine = state.playLine() ?: return
        val curEpisode = state.episode() ?: return


        defaultScope.launch(Dispatchers.IO) {
            val history = CartoonHistory(
                id = curCartoon.id,
                source = curCartoon.source,
                url = curCartoon.url,

                cover = curCartoon.coverUrl ?: "",
                name = curCartoon.title,
                intro = curCartoon.intro ?: "",

                lastLinesIndex = playLineIndex,
                lastLineTitle = playLine.label,
                lastEpisodeIndex = curEpisode,
                lastEpisodeTitle = playLine.episode[curEpisode],
                lastProcessTime = process,

                createTime = System.currentTimeMillis()
            )
            cartoonHistoryDao.modify(history)
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (!playWhenReady && exoPlayer.isMedia()) {
            defaultScope.launch(Dispatchers.Main) {
                innerTrySaveHistory()
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            if (saveLoopJob == null || saveLoopJob?.isActive != true) {
                saveLoopJob = defaultScope.launch(Dispatchers.Main) {
                    innerTrySaveHistory()
                }
            }
        } else {
            saveLoopJob?.cancel()
            saveLoopJob = null
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        val playLineIndex = state.playLineIndex() ?: return
        val playLine = state.playLine() ?: return
        val curEpisode = state.episode() ?: return
        error(
            errMsg = error.message ?: error.errorCodeName,
            throwable = error,
            playLineIndex = playLineIndex,
            playLine = playLine,
            episode = curEpisode,
        )
    }

    private fun innerPlay(playerInfo: PlayerInfo, adviceProgress: Long = 0L) {

        trySaveHistory(adviceProgress)

        if (settingPreference.useExternalVideoPlayer.get()) {
            kotlin.runCatching {
                APP.startActivity(Intent().apply {
                    setDataAndType(playerInfo.uri.toUri(), "video/*")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // for mx player https://mx.j2inter.com/api
                    putExtra("video_list", arrayOf(playerInfo.uri.toUri()))
                    val array = Array<String>((playerInfo.header?.size ?: 0) * 2) { "" }
                    val list = arrayListOf<String>()
                    playerInfo.header?.iterator()?.forEach {
                        list.add(it.key)
                        list.add(it.value)
                    }
                    putExtra("headers", list.toTypedArray())
                })
            }.onFailure {
                stringRes(com.heyanle.easy_i18n.R.string.loading_error).moeSnackBar()
            }
            return
        }

        // 如果播放器当前状态不在播放，则肯定要刷新播放源
        if (!exoPlayer.isMedia() || lastPlayerInfo?.uri != playerInfo.uri || lastPlayerInfo?.decodeType != playerInfo.decodeType) {

            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                APP,
                DefaultHttpDataSource.Factory()
                    .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
            )

            val media = when (playerInfo.decodeType) {
                C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(playerInfo.uri))

                C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(playerInfo.uri)
                    )

                else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(playerInfo.uri)
                    )
            }

            exoPlayer.setMediaSource(media, adviceProgress)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } else {
            // 已经在播放同一部，直接 seekTo 对应 progress
            exoPlayer.seekTo(adviceProgress)
            exoPlayer.playWhenReady = true
        }
    }

    private fun error(
        errMsg: String,
        throwable: Throwable? = null,
        playLineIndex: Int,
        playLine: PlayLine,
        episode: Int,
    ) {
        state = PlayingState.Error(playLineIndex, errMsg, throwable, playLine, episode)
    }


    private fun ExoPlayer.isMedia(): Boolean {
        return exoPlayer.playbackState == Player.STATE_BUFFERING || exoPlayer.playbackState == Player.STATE_READY
    }

    fun release() {
        lastJob?.cancel()
        exoPlayer.stop()
    }

}