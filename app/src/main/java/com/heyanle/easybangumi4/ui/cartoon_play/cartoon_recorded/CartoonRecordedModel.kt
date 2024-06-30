package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import android.content.Context
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.exo.thumbnail.ThumbnailBuffer
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video.ClipVideoModel
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.task.CartoonRecordedTaskModel
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.texture.EasyTextureView
import loli.ball.easyplayer2.utils.MeasureHelper

/**
 * Created by heyanlin on 2024/6/21.
 */
@UnstableApi
class CartoonRecordedModel(
    val ctx: Context,
    val exoPlayer: ExoPlayer,
    val playerInfo: PlayerInfo,
    val cartoonMediaSourceFactory: CartoonMediaSourceFactory,
    val scope: CoroutineScope,

    val thumbnailBuffer: ThumbnailBuffer,

    val start: Long,
    val end: Long,

    val currentPosition: Long,
) : Player.Listener, TextureView.SurfaceTextureListener {

    val mediaItem = cartoonMediaSourceFactory.getMediaItem(playerInfo)
    val mediaSourceFactory = cartoonMediaSourceFactory.getMediaSourceFactory(playerInfo)

    // State =========================

    // 1 -> gif 2 -> mp4
    var recordType by mutableIntStateOf(1)

    val isGif: Boolean get() = recordType == 1
    val isMp4: Boolean get() = recordType == 2

    // 裁切框矩形，和 以 renderContainer 作为坐标系
    var cropRect: Rect by mutableStateOf(Rect.Zero)

    // 防抖处理 以 renderContainer 作为坐标系
    data class RenderState(
        val renderSize: IntSize = IntSize.Zero,
        val renderPosition: Offset = Offset.Zero,
    ) {
        val renderRect: Rect by lazy {
            Rect(renderPosition, renderSize.toSize())
        }
    }

    private val _renderState = MutableStateFlow(RenderState())
    val renderState = _renderState.asStateFlow()


    // 0 -> idle 1 -> moving
    // 2 -> top left  3 -> top right 4 -> bottom right 5 -> bottom left
    // 6 -> left 7 -> top 8 -> right 9 -> bottom
    var focusMode by mutableIntStateOf(0)


    val clipVideoModel = ClipVideoModel(
        ctx,
        exoPlayer,
        playerInfo,
        cartoonMediaSourceFactory,
        scope,
        thumbnailBuffer,
        start,
        end,
        currentPosition
    )

    var cartoonRecordedTaskModel = mutableStateOf<CartoonRecordedTaskModel?>(null)


    // 渲染器
    val textureView: EasyTextureView = EasyTextureView(ctx)
        .apply {
            setScaleType(MeasureHelper.SCREEN_SCALE_FOR_HEIGHT)
            setExtSurfaceTextureListener(this@CartoonRecordedModel)
        }


    init {

        // 和播放器绑定
        exoPlayer.addListener(this)

        scope.launch {
            renderState.collect {
                cropRect = it.renderRect
            }
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


    fun onRenderPlace(
        offset: Offset,
        intSize: IntSize
    ) {
        "$offset $intSize".logi("CartoonRecordedModel")
        if (renderState.value.renderPosition == offset && renderState.value.renderSize == intSize) {
            return
        }
        _renderState.update {
            it.copy(
                renderPosition = offset,
                renderSize = intSize
            )
        }
    }


    // 触摸事件消费

    private var dragStartOffset: Offset = Offset.Zero
    private var dragStartCropRect: Rect = cropRect

    fun onDragStart(
        offset: Offset,
    ) {
        val diff = 100F
        val rectDiff = 10f


        val diffTopLeft = offset - cropRect.topLeft
        val diffTopRight = offset - cropRect.topRight
        val diffBottomRight = offset - cropRect.bottomRight
        val diffBottomLeft = offset - cropRect.bottomLeft

        val distanceTopLeft = diffTopLeft.getDistance()
        val distanceTopRight = diffTopRight.getDistance()
        val distanceBottomRight = diffBottomRight.getDistance()
        val distanceBottomLeft = diffBottomLeft.getDistance()

        val diffTopCenter = offset - cropRect.topCenter
        val diffRightCenter = offset - cropRect.centerRight
        val diffBottomCenter = offset - cropRect.bottomCenter
        val diffLeftCenter = offset - cropRect.centerLeft

        val distanceTopCenter = diffTopCenter.getDistance()
        val distanceRightCenter = diffRightCenter.getDistance()
        val distanceBottomCenter = diffBottomCenter.getDistance()
        val distanceLeftCenter = diffLeftCenter.getDistance()

        focusMode = when {
            distanceTopLeft < diff -> 2
            distanceTopRight < diff -> 3
            distanceBottomRight < diff -> 4
            distanceBottomLeft < diff -> 5
            distanceLeftCenter < diff -> 6
            distanceTopCenter < diff -> 7
            distanceRightCenter < diff -> 8
            distanceBottomCenter < diff -> 9
            cropRect.copy(
                cropRect.left - rectDiff,
                cropRect.top - rectDiff,
                cropRect.right + rectDiff,
                cropRect.bottom + rectDiff
            ).contains(offset) -> 1

            else -> 0
        }
        dragStartOffset = offset.copy()
        dragStartCropRect = cropRect.copy()
        "onDragStart $dragStartOffset $dragStartCropRect".logi("CartoonRecordedModel")
    }

    fun onDrag(
        position: Offset,
        dragAmount: Offset,
    ) {

        var newRect = dragStartCropRect
        val render = renderState.value.renderRect

        val off = position - dragStartOffset
        newRect = when (focusMode) {
            1 -> newRect.translate(off)
            2 -> newRect.copy(
                left = newRect.left + off.x,
                top = newRect.top + off.y
            )

            3 -> newRect.copy(
                right = newRect.right + off.x,
                top = newRect.top + off.y
            )

            4 -> newRect.copy(
                right = newRect.right + off.x,
                bottom = newRect.bottom + off.y
            )

            5 -> newRect.copy(
                left = newRect.left + off.x,
                bottom = newRect.bottom + off.y
            )

            6 -> newRect.copy(
                left = newRect.left + off.x
            )

            7 -> newRect.copy(
                top = newRect.top + off.y
            )

            8 -> newRect.copy(
                right = newRect.right + off.x
            )

            9 -> newRect.copy(
                bottom = newRect.bottom + off.y
            )

            else -> newRect


        }
        "onDrag $position $dragStartOffset $off $newRect $render".logi("CartoonRecordedModel")

        // left over
        if (newRect.left < render.left) {
            newRect = newRect.copy(
                left = render.left,
                right = render.width.coerceAtMost(newRect.width) + render.left
            )
        }

        // right over
        if (newRect.right > render.right) {
            newRect = newRect.copy(
                right = render.right,
                left = render.right - render.width.coerceAtMost(newRect.width)
            )
        }

        // bottom over
        if (newRect.bottom > render.bottom) {
            newRect = newRect.copy(
                bottom = render.bottom,
                top = render.bottom - render.height.coerceAtMost(newRect.height)
            )
        }

        // top over
        if (newRect.top < render.top) {
            newRect = newRect.copy(
                top = render.top,
                bottom = render.height.coerceAtMost(newRect.height) + render.top
            )
        }

        // 只要左上角和右下角在渲染区域内
        if (
            render.containsWithRightBottom(newRect.topLeft) && render.containsWithRightBottom(
                newRect.bottomRight
            ) && !newRect.isEmpty
        ) {
            cropRect = newRect
        }


    }

    fun onDragEnd() {
        focusMode = 0
    }

    fun onSave() {
        clipVideoModel.outputThumbnailHelper.release()
        exoPlayer.playWhenReady = false
        val render = renderState.value.renderRect.copy()
        val crop = cropRect.copy()

        // 将 render 变换到 X [0, 1] Y [0, 1] 的矩形中，然后对 crop 进行同样的变换生成 CropEffect

        val cropReft = crop.let {
            // render 回到原点
            val ori = it.translate(-render.center)

            // render 缩放到 X [-1, 1] Y [-1, 1]
            val tran = ori.copy(
                left = ori.left * 1 / render.width,
                right = ori.right * 1 / render.width,
                bottom = ori.bottom * 1 / render.height,
                top = ori.top * 1 / render.height
            )

            val res = tran.translate(Offset(0.5f, 0.5f))

            res
        }
        "${ cropReft.left},${ cropReft.right},${cropReft.bottom},${cropReft.top}".logi("CartoonRecordedTaskModel")

        cartoonRecordedTaskModel.value = CartoonRecordedTaskModel(
            ctx,
            playerInfo,
            cartoonMediaSourceFactory,
            clipVideoModel.selectionStart,
            clipVideoModel.selectionEnd,
            RectF(
                cropReft.left,
                cropReft.top,
                cropReft.right,
                cropReft.bottom,

            ),
            recordType
        ) {
            cartoonRecordedTaskModel.value = null
        }

    }


    // Player Listener

    private var lastVideoSize: VideoSize = VideoSize(0, 0)

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        textureView.setVideoSize(videoSize.width, videoSize.height)
        lastVideoSize = videoSize
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

fun Rect.containsWithRightBottom(offset: Offset): Boolean {
    return offset.x >= left && offset.x <= right && offset.y >= top && offset.y <= bottom
}