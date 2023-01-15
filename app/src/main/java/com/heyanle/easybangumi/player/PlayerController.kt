package com.heyanle.easybangumi.player

import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.analytics.DefaultAnalyticsCollector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Clock
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.utils.OverlayHelper
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.okkv2.core.okkv

/**
 * 1. 管理 Player 对象
 * Created by HeYanLe on 2023/1/15 18:43.
 * https://github.com/heyanLE
 */
object PlayerController {

    val ratioWidth = 16F
    val ratioHeight = 9F

    var exoPlayer: ExoPlayer = ExoPlayer.Builder(
        BangumiApp.INSTANCE,
        DefaultRenderersFactory(BangumiApp.INSTANCE),
        DefaultMediaSourceFactory(BangumiApp.INSTANCE),
        DefaultTrackSelector(BangumiApp.INSTANCE),
        DefaultLoadControl(),
        DefaultBandwidthMeter.getSingletonInstance(BangumiApp.INSTANCE),
        DefaultAnalyticsCollector(Clock.DEFAULT)
    ).build()




}