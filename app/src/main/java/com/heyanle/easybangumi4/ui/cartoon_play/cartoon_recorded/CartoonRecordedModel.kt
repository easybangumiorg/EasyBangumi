package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.content.Context
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.texture.EasyTextureView
import loli.ball.easyplayer2.utils.MeasureHelper
import java.io.File

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
): Player.Listener, TextureView.SurfaceTextureListener {

    companion object {
        const val horizontalPaddingDp = 18
        const val verticalPaddingDp = 6
    }

    val horizontalPaddingPx = dip2px(ctx, horizontalPaddingDp.toFloat())
    val verticalPaddingPx = dip2px(ctx, verticalPaddingDp.toFloat())

    // State =========================

    // 录制配置
    sealed class ConfigType {
        data class Gif(
            val fps: Int = 15,
            // 是否是压制模式，适配 QQ 微信
            val suppress: Boolean = true,
        ) : ConfigType()

        data class Mp4(
            val fps: Int = 30,
        ) : ConfigType()
    }
    data class Configuration(
        val gifType : ConfigType = ConfigType.Gif(),
        val mp4Type : ConfigType = ConfigType.Mp4(),

        // 1 -> gif 2 -> mp4
        val currentType: Int = 1,
    ){
        val currentConfig: ConfigType
            get() = when(currentType){
                1 -> gifType
                2 -> mp4Type
                else -> gifType
            }
    }


    data class CropState(
        val clipLeft: Int = 0,
        val clipTop: Int = 0,
        val clipRight: Int = 0,
        val clipBottom: Int = 0,

        val videoSize: VideoSize = VideoSize(0, 0),
    )



    private val _configuration = MutableStateFlow<Configuration>(Configuration())
    val configuration = _configuration.asStateFlow()

    private val _cropState = MutableStateFlow(CropState())
    val cropState = _cropState.asStateFlow()


    val clipVideoModel = ClipVideoModel(
        ctx, exoPlayer, mediaItem, mediaSourceFactory, scope, thumbnailBuffer, start, end, currentPosition
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
            _cropState.collectLatest {

            }
        }
    }

    // 业务接口
    fun changeConfigType(type: Int){
        _configuration.update {
            it.copy(
                currentType = type
            )
        }
    }
    fun changeGifConfig(fps: Int, suppress: Boolean){
        _configuration.update {
            it.copy(
                gifType = ConfigType.Gif(fps, suppress)
            )
        }
    }
    fun changeMp4Config(fps: Int){
        _configuration.update {
            it.copy(
                mp4Type = ConfigType.Mp4(fps)
            )
        }
    }


    // Compose Listener

    fun onLaunch(){
        textureView.attachPlayer(exoPlayer)
    }

    fun onDispose(){
        exoPlayer.removeListener(this)
        textureView.detachPlayer(exoPlayer)
    }


    // 右下角点触摸事件消费
    // 这里要添加一些固定比例的吸附点
    fun onRightBottomChange(
        x: Int,
        y: Int,
    ){}


    // Player Listener

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        if (videoSize.width > 0 && videoSize.height > 0) {
            _cropState.update {
                it.copy(
                    videoSize = videoSize
                )
            }
        }
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