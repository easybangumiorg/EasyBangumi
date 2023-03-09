package com.heyanle.easybangumi4.ui.common.player

import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.easybangumi4.ui.common.player.surface.EasySurfaceView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by HeYanLe on 2023/3/8 22:45.
 * https://github.com/heyanLE
 */
class ControlViewModel(
    val exoPlayer: ExoPlayer
): ViewModel(), Player.Listener {

    companion object {
        const val CONTROL_HIDE_DELAY = 4000L
        const val POSITION_LOOP_DELAY = 1000L

        const val ratioWidth = 16F
        const val ratioHeight = 9F
    }

    // 普通状态  长按加速中 锁定中 左右滑动中 上下滑动中 结束

    enum class ControlState {
        Normal, Locked, HorizontalScroll, Ended
    }

    var controlState by mutableStateOf(ControlState.Normal)

    var isNormalLockedControlShow by mutableStateOf(true)

    var horizontalScrollPosition by mutableStateOf(0L)

    var isFullScreen by mutableStateOf(false)

    var isLoading by mutableStateOf(false)

    var playWhenReady by mutableStateOf(exoPlayer.playWhenReady)

    var position by mutableStateOf(0L)
    var bufferPosition by mutableStateOf(0L)

    var during by mutableStateOf(0L)

    var title by mutableStateOf("")

    private var lastHideJob: Job? = null
    private var loopJob: Job? = null

    private var lastVideoSize: VideoSize? = null

    private var surfaceViewRef: WeakReference<EasySurfaceView>? = null



    fun onLockedChange(locked: Boolean){
        viewModelScope.launch {
            if(locked){
                controlState = ControlState.Locked
                showControlWithHideDelay()
            }else{
                controlState = ControlState.Normal
                showControlWithHideDelay()
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
            showControlWithHideDelay()
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
        if(controlState == ControlState.Normal){
            controlState = ControlState.HorizontalScroll
        }
        horizontalScrollPosition = position

    }

    fun onActionUP(){
        if(controlState == ControlState.HorizontalScroll){
            exoPlayer.seekTo(horizontalScrollPosition)
        }
        controlState = ControlState.Normal
        showControlWithHideDelay()

    }



    fun onHideClick(){
        showControlWithHideDelay()
    }

    fun onLaunch(){

    }

    fun onDisposed(){
        surfaceViewRef?.get()?.let {
            exoPlayer.clearVideoSurfaceView(it)
        }
        surfaceViewRef = null

    }

    fun onSurfaceView(surfaceView: EasySurfaceView){
        surfaceViewRef = WeakReference(surfaceView)
        exoPlayer.setVideoSurfaceView(surfaceView)
        lastVideoSize?.let {
            surfaceView.setVideoSize(it.width, it.height)
        }


    }


    private fun showControlWithHideDelay(){
        lastHideJob?.cancel()
        isNormalLockedControlShow = true
        lastHideJob = viewModelScope.launch {
            delay(CONTROL_HIDE_DELAY)
            if(this.isActive && lastHideJob != null && lastHideJob?.isActive == true){
                isNormalLockedControlShow = false
            }
        }
    }



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
                delay(POSITION_LOOP_DELAY)
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
                isLoading = false
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

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        surfaceViewRef?.get()?.let {
            it.setVideoSize(videoSize.width, videoSize.height)
        }
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

class ControlViewModelFactory(
    private val exoPlayer: ExoPlayer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlViewModel::class.java))
            return ControlViewModel(exoPlayer) as T
        throw RuntimeException("unknown class :" + modelClass.name)
    }
}