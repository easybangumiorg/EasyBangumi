package com.heyanle.easybangumi4.ui.common.player

import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/3/8 22:45.
 * https://github.com/heyanLE
 */
class ControlViewModel(
    private val exoPlayer: ExoPlayer
): ViewModel(), Player.Listener {

    // 普通状态  长按加速中 锁定中 左右滑动中 上下滑动中 结束

    enum class ControlState {
        Normal, Locked, HorizontalScroll, VerticalDrag, LongTouched, Ended
    }

    var controlState by mutableStateOf(ControlState.Normal)

    var isNormalShow by mutableStateOf(true)

    var horizontalScrollPosition by mutableStateOf(0L)

    enum class VerticalDrag {
        None, BRIGHTNESS, VOLUME
    }

    var verticalScrollType by mutableStateOf(VerticalDrag.None)
    var verticalDragPercent by mutableStateOf(0F)

    var isLockedShow by mutableStateOf(false)


    var isFullScreen by mutableStateOf(false)

    var isLoading by mutableStateOf(false)

    var playWhenReady by mutableStateOf(exoPlayer.playWhenReady)

    var position by mutableStateOf(0L)
    var bufferPosition by mutableStateOf(0L)

    var during by mutableStateOf(0L)

    private var loopJob: Job? = null



    fun onLockedChange(locked: Boolean){
        viewModelScope.launch {
            if(locked){
                controlState = ControlState.Locked
                isLockedShow = true
            }else{
                controlState = ControlState.Normal
                isNormalShow = true
            }
        }
    }

    fun onFullScreen(fullScreen: Boolean){
        viewModelScope.launch {
            if(isFullScreen == fullScreen){
                return@launch
            }
            isFullScreen = fullScreen
            controlState = ControlState.Normal
            isNormalShow = true
        }
    }

    @UiThread
    fun onPlayPause(isPlay: Boolean){
        exoPlayer.playWhenReady = isPlay
    }

    @UiThread
    fun onPrepare(){
        isLoading = true
    }

    @UiThread
    fun onPositionChange(position: Long){
        horizontalScrollPosition = position
        if(controlState != ControlState.HorizontalScroll){
            controlState = ControlState.HorizontalScroll
        }

    }

    fun onPositionChangeFinished(){
        controlState = ControlState.Normal
        isNormalShow = true
        exoPlayer.seekTo(horizontalScrollPosition)
    }

    fun onHideClick(){}

    init {
        exoPlayer.addListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(this)
    }

    private fun getLoopJob(): Job {
        return viewModelScope.launch {
            while (isActive){
                syncTimeIfNeed()
                delay(1000)
            }
        }
    }

    private fun starLoop(){
        viewModelScope.launch {
            if(loopJob == null || loopJob?.isActive != true){
                loopJob?.cancel()
                loopJob = getLoopJob()
            }
        }
    }

    private fun stopLoop(){
        viewModelScope.launch {
            loopJob?.cancel()
            loopJob = null
        }
    }



    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when(playbackState){
            Player.STATE_READY -> {
                isLoading = true
                syncTimeIfNeed()
                starLoop()
            }
            Player.STATE_IDLE -> {
                isLoading = false
                stopLoop()
            }
            Player.STATE_BUFFERING -> {
                isLoading = true
                syncTimeIfNeed()
                starLoop()
            }
            Player.STATE_ENDED -> {
                isLoading = false
                stopLoop()
            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        this.playWhenReady = playWhenReady
    }

    private fun syncTimeIfNeed(){
        if(during != exoPlayer.duration){
            during = exoPlayer.duration
        }
        if(position != exoPlayer.currentPosition){
            position = exoPlayer.currentPosition
        }
        if(bufferPosition != exoPlayer.bufferedPosition){
            bufferPosition = exoPlayer.bufferedPosition
        }
    }

}