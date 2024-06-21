package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.content.Context
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.exo.thumbnail.OutputThumbnailHelper
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video.ClipVideoModel
import com.heyanle.easybangumi4.utils.dip2px
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.texture.EasyTextureView
import loli.ball.easyplayer2.utils.MeasureHelper
import java.io.File
import kotlin.math.abs

/**
 * Created by heyanlin on 2024/6/21.
 */
@UnstableApi
class CartoonRecordedModel(
    val ctx: Context,
    val exoPlayer: ExoPlayer,
    val mediaItem: MediaItem,
    val mediaSourceFactory: MediaSource.Factory,

    val scope: CoroutineScope,

    val thumbnailBuffer: ThumbnailBuffer,

    val start: Long,
    val end: Long,

    val currentPosition: Long,
) : Player.Listener, TextureView.SurfaceTextureListener {

    // State =========================

    // 1 -> gif 2 -> mp4
    var recordType by mutableIntStateOf(1)

    val isGif: Boolean get() = recordType == 1
    val isMp4: Boolean get() = recordType == 2

    var touchDownX = 0F
    var touchDownY = 0f

    var touchDownLeft = 0f
    var touchDownTop = 0f
    var touchDownRight = 0f
    var touchDownBottom = 0f

    var clipLeft: Float by mutableFloatStateOf(0F)
    var clipTop: Float by mutableFloatStateOf(0f)
    var clipRight: Float by mutableFloatStateOf(0f)
    var clipBottom: Float by mutableFloatStateOf(0F)

    val clipWidth: Float get() = clipRight - clipLeft
    val clipHeight: Float get() = clipBottom - clipTop

    val measureHelper = MeasureHelper()

    var videoSize: VideoSize by mutableStateOf(VideoSize(0,0))
    var renderContainerWidth by mutableIntStateOf(0)
    var renderContainerHeight by mutableIntStateOf(0)

    var renderWidth by mutableStateOf(0)
    var renderHeight by mutableStateOf(0)

    // 0 -> idle 1 -> moving 2 -> scaling
    var focusMode by mutableIntStateOf(0)



    val clipVideoModel = ClipVideoModel(
        ctx,
        exoPlayer,
        mediaItem,
        mediaSourceFactory,
        scope,
        thumbnailBuffer,
        start,
        end,
        currentPosition
    )


    // 渲染器
    val textureView: EasyTextureView = EasyTextureView(ctx)
        .apply {
            setScaleType(MeasureHelper.SCREEN_SCALE_ADAPT)
            setExtSurfaceTextureListener(this@CartoonRecordedModel)
        }


    init {

        // 和播放器绑定
        exoPlayer.addListener(this)

        scope.launch {

        }
    }

    // 业务接口
    fun changeConfigType(type: Int) {
        recordType = type
    }


    // Compose Listener

    fun onLaunch() {
        textureView.attachPlayer(exoPlayer)
    }

    fun onDispose() {
        exoPlayer.removeListener(this)
        textureView.detachPlayer(exoPlayer)
    }

    fun onRenderSizeContainerChange(
        width: Int,
        height: Int
    ){
        renderContainerWidth = width
        renderContainerHeight = height
    }




    // 右下角点触摸事件消费

    fun onDragStart(
        x: Float, y: Float
    ) {
        val diff = 100F
        val point = clipRight to clipBottom
        val dxy = x - point.first to y - point.second
        if (abs(dxy.first) < diff && abs(dxy.second) < diff) {
            focusMode = 2

        } else if (
            x > clipLeft - 10 && x < clipRight + 10 &&
            y > clipTop - 10 && y < clipBottom + 10
        ) {
            focusMode = 1

        }
        touchDownX = x
        touchDownY = y

        touchDownBottom = clipBottom
        touchDownLeft = clipLeft
        touchDownRight = clipRight
        touchDownTop = clipTop
    }

    fun onDrag(
        x: Float,
        y: Float,
    ) {
        when(focusMode){
            1 -> {
                val dx = x - touchDownX
                val dy = y - touchDownY
                clipLeft = touchDownLeft + dx
                clipTop = touchDownTop + dy
                clipRight = touchDownRight + dx
                clipBottom = touchDownBottom + dy
            }
            2 -> {
                clipRight = x
                clipBottom = y
            }
        }
    }

    fun onDragEnd() {
        focusMode = 0
    }


    // Player Listener

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        if (videoSize.width > 0 && videoSize.height > 0) {
            this.videoSize = videoSize
        }
        textureView.setVideoSize(videoSize.width, videoSize.height)
    }

    // TextureView Listener
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        clipVideoModel.checkPosition()
    }
}