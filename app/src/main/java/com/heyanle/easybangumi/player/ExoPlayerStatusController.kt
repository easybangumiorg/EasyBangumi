package com.heyanle.easybangumi.player

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.eplayer_core.constant.EasyPlayStatus

/**
 * Created by HeYanLe on 2023/1/23 16:53.
 * https://github.com/heyanLE
 */
class ExoPlayerStatusController(
    private val exoPlayer: ExoPlayer
) : Player.Listener {

    init {
        exoPlayer.addListener(this)
    }


    private val _playerControllerStatus = MutableLiveData(EasyPlayStatus.STATE_IDLE)
    val playerControllerStatus: LiveData<Int> = _playerControllerStatus.distinctUntilChanged()
    private val _videoSizeStatus = MutableLiveData<VideoSize>()
    val videoSizeStatus: LiveData<VideoSize> = _videoSizeStatus.distinctUntilChanged()

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        _videoSizeStatus.postValue(videoSize)
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        dispatchStatus()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        dispatchStatus()
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        dispatchStatus()
    }

    fun dispatchPreparing() {
        _playerControllerStatus.postValue(EasyPlayStatus.STATE_PREPARING)
    }

    fun dispatchIdle() {
        _playerControllerStatus.postValue(EasyPlayStatus.STATE_IDLE)
    }

    private fun dispatchStatus() {
        if (exoPlayer.playerError != null) {
            exoPlayer.playerError?.printStackTrace()
            _playerControllerStatus.postValue(EasyPlayStatus.STATE_ERROR)
            return
        }
        when (exoPlayer.playbackState) {
            Player.STATE_IDLE -> {
                _playerControllerStatus.postValue(EasyPlayStatus.STATE_IDLE)
            }
            Player.STATE_BUFFERING -> {

                _playerControllerStatus.postValue(EasyPlayStatus.STATE_BUFFERING)
            }
            Player.STATE_READY -> {
                _playerControllerStatus.postValue(EasyPlayStatus.STATE_BUFFERED)
                if (exoPlayer.playWhenReady) {
                    _playerControllerStatus.postValue(EasyPlayStatus.STATE_PLAYING)
                } else {
                    _playerControllerStatus.postValue(EasyPlayStatus.STATE_PAUSED)
                }

            }
            Player.STATE_ENDED -> {
                _playerControllerStatus.postValue(EasyPlayStatus.STATE_PLAYBACK_COMPLETED)
            }
        }

    }

    fun release() {
        exoPlayer.removeListener(this)
    }


}