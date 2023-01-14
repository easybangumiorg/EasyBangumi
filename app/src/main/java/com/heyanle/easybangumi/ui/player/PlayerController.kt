package com.heyanle.easybangumi.ui.player

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.collection.LruCache
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.DefaultAnalyticsCollector
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.PlayerView
import com.heyanle.easybangumi.BangumiApp
import com.heyanle.easybangumi.utils.OverlayHelper
import com.heyanle.easybangumi.utils.dip2px
import com.heyanle.eplayer_core.player.IPlayerEngine
import com.heyanle.eplayer_core.player.IPlayerEngineFactory
import com.heyanle.eplayer_core.render.IRender
import com.heyanle.eplayer_core.render.IRenderFactory
import com.heyanle.eplayer_core.render.SurfaceViewRender
import com.heyanle.lib_anim.entity.BangumiSummary
import com.heyanle.okkv2.core.okkv
import kotlinx.coroutines.MainScope


@UnstableApi
/**
 * Created by HeYanLe on 2023/1/12 20:45.
 * https://github.com/heyanLE
 */
object PlayerController {

    val scope = MainScope()

    var autoTinyEnableOkkv by okkv<Boolean>("AUTO_TINY_ENABLE", def = true)
    var tinyWidthDpOkkv by okkv<Int>("TINY_WIDTH_DP", 250)

    val ratioWidth = 16F
    val ratioHeight = 9F

    val animPlayViewModelCache: LruCache<BangumiSummary, AnimPlayViewModel> = LruCache(3)

    var lastPlayerStatus: AnimPlayViewModel.PlayerStatus.Play? = null

    val windowManager by lazy {
        OverlayHelper.getWindowManager(BangumiApp.INSTANCE)
    }

    val canAddToCompose = mutableStateOf(false)

    fun getAnimPlayViewModel(bangumiSummary: BangumiSummary): AnimPlayViewModel{
        val cache = animPlayViewModelCache[bangumiSummary]
        if(cache == null){
            val n = AnimPlayViewModel(bangumiSummary)
            animPlayViewModelCache.put(bangumiSummary, n)
            return n
        }
        return cache
    }


    private var layoutParams: WindowManager.LayoutParams? = null

    var exoPlayer: ExoPlayer = ExoPlayer.Builder(
        BangumiApp.INSTANCE,
        DefaultRenderersFactory(BangumiApp.INSTANCE),
        DefaultMediaSourceFactory(BangumiApp.INSTANCE),
        DefaultTrackSelector(BangumiApp.INSTANCE),
        DefaultLoadControl(),
        DefaultBandwidthMeter.getSingletonInstance(BangumiApp.INSTANCE),
        DefaultAnalyticsCollector(Clock.DEFAULT)
    ).build()


    val exoPlayerView : PlayerView by lazy {
        PlayerView(BangumiApp.INSTANCE).apply {
            player = exoPlayer
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }


    val playerContainer: FrameLayout by lazy {
        FrameLayout(BangumiApp.INSTANCE)
    }

    fun newPlayer(newPlay: AnimPlayViewModel.PlayerStatus.Play){
        if(lastPlayerStatus?.url != newPlay.url){
            val defaultDataSourceFactory = DefaultDataSource.Factory(BangumiApp.INSTANCE)
            val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                BangumiApp.INSTANCE,
                defaultDataSourceFactory
            )
            val source = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(newPlay.url))
            exoPlayer.setMediaSource(source)
            Log.d("PlayerController", newPlay.url)
            exoPlayer.prepare()
            exoPlayer.play()
        }
        lastPlayerStatus = newPlay
    }

    fun onScreenLaunch(){
        exoPlayer.pause()
        kotlin.runCatching {
            windowManager.removeView(exoPlayerView)
        }.onFailure {
            it.printStackTrace()
        }
        playerContainer.addView(exoPlayerView, ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        canAddToCompose.value = true
    }

    fun onScreenDispose(){
        canAddToCompose.value = false
        if(autoTinyEnableOkkv && OverlayHelper.drawOverlayEnable(BangumiApp.INSTANCE)){
            exoPlayer.pause()
            playerContainer.removeView(exoPlayerView)
            refreshLayoutParams()
            exoPlayerView.layoutParams = layoutParams
            windowManager.addView(exoPlayerView, layoutParams)
            exoPlayer.setVideoSurfaceView(exoPlayerView.videoSurfaceView as SurfaceView)
        }
    }




    private fun refreshLayoutParams(){
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) layoutParams?.type =
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else layoutParams?.type =
            WindowManager.LayoutParams.TYPE_PHONE
        layoutParams?.format = PixelFormat.RGBA_8888
        layoutParams?.flags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        layoutParams?.width = tinyWidthDpOkkv.dip2px()
        layoutParams?.height = ((tinyWidthDpOkkv.dip2px() / ratioWidth) * ratioHeight).toInt()
        layoutParams?.gravity = Gravity.CENTER or Gravity.TOP
    }


}