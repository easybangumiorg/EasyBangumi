package com.heyanle.easybangumi.player

import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.google.android.exoplayer2.video.VideoSize
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.utils.OverlayHelper
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.eplayer_core.constant.EasyPlayStatus
import com.heyanle.eplayer_core.player.IPlayer
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 1. 管理 Player 对象
 * Created by HeYanLe on 2023/1/15 18:43.
 * https://github.com/heyanLE
 */
object PlayerController {

    val ratioWidth = 16F
    val ratioHeight = 9F

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(
            BangumiApp.INSTANCE,
            DefaultRenderersFactory(BangumiApp.INSTANCE),
            DefaultMediaSourceFactory(BangumiApp.INSTANCE),
            DefaultTrackSelector(BangumiApp.INSTANCE),
            DefaultLoadControl(),
            DefaultBandwidthMeter.getSingletonInstance(BangumiApp.INSTANCE),
            DefaultAnalyticsCollector(Clock.DEFAULT)
        ).build()
    }

    private val stateController = ExoPlayerStatusController(exoPlayer)
    val playerControllerStatus: LiveData<Int> = stateController.playerControllerStatus
    val videoSizeStatus: LiveData<VideoSize> = stateController.videoSizeStatus

    fun setMediaSource(source: MediaSource) {
        exoPlayer.setMediaSource(source)
        stateController.dispatchIdle()
    }

    fun prepare() {
        exoPlayer.prepare()
        stateController.dispatchPreparing()
    }


}