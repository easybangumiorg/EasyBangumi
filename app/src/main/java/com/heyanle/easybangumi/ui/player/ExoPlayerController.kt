package com.heyanle.easybangumi.ui.player

import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import com.heyanle.easybangumi.BangumiApp

@UnstableApi
/**
 * Created by HeYanLe on 2023/1/12 23:50.
 * https://github.com/heyanLE
 */
object ExoPlayerController {

    private var exoPlayer: ExoPlayer = ExoPlayer.Builder(
        BangumiApp.INSTANCE,
        DefaultRenderersFactory(BangumiApp.INSTANCE),
        DefaultMediaSourceFactory(BangumiApp.INSTANCE),
        DefaultTrackSelector(BangumiApp.INSTANCE),
        DefaultLoadControl(),
        DefaultBandwidthMeter.getSingletonInstance(BangumiApp.INSTANCE),
        DefaultAnalyticsCollector(Clock.DEFAULT)
    ).build()




}