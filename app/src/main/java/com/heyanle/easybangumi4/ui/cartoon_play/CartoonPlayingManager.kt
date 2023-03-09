package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
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
import com.google.android.exoplayer2.util.Clock
import com.heyanle.bangumi_source_api.api.component.play.PlayComponent
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source.SourceMaster
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.MainScope

/**
 * Created by HeYanLe on 2023/3/7 14:45.
 * https://github.com/heyanLE
 */
object CartoonPlayingManager {

    val defaultScope = MainScope()



    sealed class PlayingState {
        object None : PlayingState()

        class Loading(
            val playLine: PlayLine,
            val curEpisode: Int,
        ) : PlayingState()

        class Playing(
            val playerInfo: PlayerInfo,
            val playLine: PlayLine,
            val curEpisode: Int,
        ) : PlayingState()

        class Error(
            val errMsg: String,
            val throwable: Throwable?,
            val playLine: PlayLine,
            val curEpisode: Int,
        ) : PlayingState()

        fun playLine(): PlayLine? {
            return when(this){
                None -> null
                is Loading -> playLine
                is Playing -> playLine
                is Error -> playLine
            }
        }

        fun episode(): Int {
            return when(this){
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
    private var cartoonSummary: CartoonSummary? = null


    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(
            APP,
            DefaultRenderersFactory(APP),
            DefaultMediaSourceFactory(APP),
            DefaultTrackSelector(APP),
            DefaultLoadControl(),
            DefaultBandwidthMeter.getSingletonInstance(APP),
            DefaultAnalyticsCollector(Clock.DEFAULT)
        ).build()
    }

    suspend fun refresh(){
        val cartoonSummary = cartoonSummary?:return
        val playComponent = playComponent?:return
        val playLine =  state.playLine()?:return
        changePlay(playComponent,cartoonSummary, playLine, state.episode(), 0)
    }

    suspend fun changeLine(
        sourceKey: String,
        cartoonSummary: CartoonSummary,
        playLine: PlayLine,
        defaultEpisode: Int = 0,
        defaultProgress: Long = 0L,
    ) {
        val playComponent = SourceMaster.animSourceFlow.value.play(sourceKey) ?: return
        CartoonPlayingManager.playComponent = playComponent
        CartoonPlayingManager.cartoonSummary = cartoonSummary
        changePlay(playComponent, cartoonSummary, playLine, defaultEpisode, defaultProgress)
    }

    suspend fun tryNext(
        defaultProgress: Long = 0L,
    ): Boolean {
        val playingState = (state as? PlayingState.Playing) ?: return false
        val target = playingState.curEpisode+1
        if(target< 0 || target >= playingState.playLine.episode.size){
            return false
        }
        changeEpisode(playingState.curEpisode + 1, defaultProgress)
        return true
    }

    suspend fun changeEpisode(
        episode: Int,
        defaultProgress: Long = 0L,
    ): Boolean {
        val cartoonSummary = cartoonSummary?:return false
        val playComponent = playComponent?:return false
        val playingState = (state as? PlayingState.Playing) ?: return false
        changePlay(playComponent, cartoonSummary, playingState.playLine, episode, defaultProgress)
        return true
    }

    private suspend fun changePlay(
        playComponent: PlayComponent,
        cartoonSummary: CartoonSummary,
        playLine: PlayLine,
        episode: Int = 0,
        adviceProgress: Long = 0L,
    ) {
        if (playLine.episode.isEmpty()) {
            return
        }
        val realEpisode = if (episode < 0 || episode >= playLine.episode.size) 0 else episode

        state = PlayingState.Loading(playLine, realEpisode)
        playComponent.getPlayInfo(cartoonSummary, playLine, episode)
            .complete {
                innerPlay(it.data, adviceProgress)
                state = PlayingState.Playing(
                    it.data, playLine, episode,
                )
            }
            .error {
                error(
                    if (it.isParserError) stringRes(
                        com.heyanle.easy_i18n.R.string.source_error
                    ) else stringRes(com.heyanle.easy_i18n.R.string.loading_error),
                    it.throwable, playLine, episode
                )
            }

    }

    private fun innerPlay(playerInfo: PlayerInfo, adviceProgress: Long = 0L) {

        // 如果播放器当前状态不在播放，则肯定要刷新播放源
        if (!exoPlayer.isMedia() || lastPlayerInfo?.uri != playerInfo.uri || lastPlayerInfo?.decodeType != playerInfo.decodeType) {
            val defaultDataSourceFactory =
                DefaultDataSource.Factory(APP)
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                APP,
                defaultDataSourceFactory
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
        } else {
            // 已经在播放同一部，直接 seekTo 对应 progress
            exoPlayer.seekTo(adviceProgress)
        }
    }

    private fun error(
        errMsg: String,
        throwable: Throwable? = null,
        playLine: PlayLine,
        episode: Int,
    ) {
        state = PlayingState.Error(errMsg, throwable, playLine, episode)
    }





    private fun ExoPlayer.isMedia(): Boolean {
        return exoPlayer.playbackState == Player.STATE_BUFFERING || exoPlayer.playbackState == Player.STATE_READY
    }

}