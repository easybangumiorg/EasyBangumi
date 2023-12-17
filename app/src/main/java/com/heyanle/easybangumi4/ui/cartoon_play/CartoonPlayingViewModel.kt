package com.heyanle.easybangumi4.ui.cartoon_play

import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.old.play.CartoonPlayingController
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.exo.MediaSourceFactory
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class CartoonPlayingViewModel(
): ViewModel() {

    val exoPlayer =  ExoPlayer.Builder(APP).build()

    private var cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState? = null
    private var playingPlayLine: PlayLine? = null
    private var playingEpisode: Episode? = null
    private var playingInfo: PlayerInfo? = null

    data class PlayingState(
        val isLoading: Boolean = true,
        val isPlaying: Boolean = false,
        val isError: Boolean = false,
        val errorMsg: String = "",
        val errorThrowable: Throwable? = null
    )
    private val _playingState = MutableStateFlow<PlayingState>(PlayingState())
    val playingState = _playingState.asStateFlow()

    private val dispatcher = CoroutineProvider.SINGLE
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var lastJob: Job? = null

    private val mediaSourceFactory: MediaSourceFactory by Injekt.injectLazy()
    private val sourceStateCase: SourceStateCase by Injekt.injectLazy()

    fun tryRefresh(){
        lastJob?.cancel()
        lastJob = scope.launch {
            cartoonPlayingState?.let {
                innerPlay(it, 0)
            }
        }

    }

    fun changePlay(
        cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState?,
        adviceProcess: Long,
    ){
        lastJob?.cancel()
        lastJob = scope.launch {
            this@CartoonPlayingViewModel.cartoonPlayingState =  cartoonPlayingState
            if(cartoonPlayingState == null){
                exoPlayer.stop()
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        isError = false
                    )
                }
            }else{
                if(playingPlayLine == cartoonPlayingState.playLine.playLine
                    && playingEpisode == cartoonPlayingState.episode
                    && _playingState.first().isPlaying
                    && exoPlayer.isMedia()){
                    if(adviceProcess >= 0){
                        exoPlayer.seekTo(adviceProcess)
                    }
                }else{
                    innerPlay(cartoonPlayingState, adviceProcess)
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

    /**
     * 调用外部播放器播放
     */
    private fun innerPlayExternal(playerInfo: PlayerInfo) {
        APP.startActivity(Intent("android.intent.action.VIEW").apply {
            setDataAndType(playerInfo.uri.toUri(), "video/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

    private suspend fun innerPlay( cartoonPlayingState: CartoonPlayViewModel.CartoonPlayState,
                           adviceProcess: Long,){

        _playingState.update {
            it.copy(
                isLoading = true,
            )
        }
        val play = sourceStateCase.awaitBundle().play(cartoonPlayingState.cartoonInfo.source)
        if(play == null){
            _playingState.update {
                it.copy(
                    isLoading = false,
                    isError = true,
                    errorMsg = stringRes(com.heyanle.easy_i18n.R.string.source_not_found)
                )
            }
            return
        }
        play.getPlayInfo(
            cartoonPlayingState.cartoonInfo.toSummary(),
            cartoonPlayingState.playLine.playLine,
            cartoonPlayingState.episode
        )
            .complete {
                yield()
                playingPlayLine =  cartoonPlayingState.playLine.playLine
                playingEpisode = cartoonPlayingState.episode
                innerPlay(it.data, adviceProcess)
            }
            .error {
                yield()
                _playingState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMsg = it.errorMsg,
                        errorThrowable = it.errorThrowable
                    )
                }
            }


    }
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private suspend fun  innerPlay(playerInfo: PlayerInfo, adviceProcess: Long){
        exoPlayer.pause()
        if(this.playingInfo != null){
            if(
                playingInfo?.uri == playerInfo.uri
                && playingInfo?.decodeType == playerInfo.decodeType
                && exoPlayer.isMedia()
                ){
                playingInfo = playerInfo
                if(adviceProcess >= 0){
                    exoPlayer.seekTo(adviceProcess)
                }
                exoPlayer.playWhenReady = true
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

        playingInfo = playerInfo
        val media = mediaSourceFactory.get(playerInfo)
        exoPlayer.setMediaSource(media, adviceProcess)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        _playingState.update {
            it.copy(
                isLoading = false,
                isPlaying = true,
                isError = false
            )
        }
    }

    fun trySaveHistory(ps: Long = -1) {}

    fun onExit(){
        exoPlayer.pause()
    }

    override fun onCleared() {
        super.onCleared()
        lastJob?.cancel()
        scope.cancel()
        exoPlayer.release()
    }


    private fun ExoPlayer.isMedia(): Boolean {
        return playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY
    }
}