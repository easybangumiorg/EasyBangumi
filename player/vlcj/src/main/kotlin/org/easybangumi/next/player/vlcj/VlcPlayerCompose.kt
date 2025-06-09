package org.easybangumi.next.player.vlcj

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import org.easybangumi.next.player.api.C
import org.easybangumi.next.player.api.VideoSize
import org.jetbrains.skia.Bitmap
import kotlin.math.roundToInt

/**
 * Created by heyanlin on 2025/5/29.
 */

@Composable
fun VlcjPlayerFrame(
    modifier: Modifier,
    state: VlcPlayerFrameState,
){
    state.frameCanvas(
        modifier = modifier,
    )
}

@Composable
fun rememberVlcjPlayerFrameState(
): VlcPlayerFrameState {
    return remember() {
        VlcPlayerFrameState()
    }
}

class VlcPlayerFrameState(): VlcjPlayerBridge.OnFrameListener {


    fun bindBridge(bridge: VlcjPlayerBridge) {
        if (this.bridge == bridge) return
        this.bridge?.removeFrameListener()
        this.bridge = bridge
        bridge.setFrameListener(this)
    }

    fun unbindBridge() {
        bridge?.removeFrameListener()
        bridge = null
    }

    private var bridge by mutableStateOf<VlcjPlayerBridge?>(null)
    private val frameSizeCalculator = VlcFrameSizeCalculator()
    var time by mutableStateOf(0L)
    var composeBitmap by mutableStateOf<ImageBitmap?>(null)

    override fun onFrame(bitmap: Bitmap, time: Long) {
        if (!bitmap.isNull && !bitmap.isEmpty) {
//            println(bitmap.toString() + time + " " + bitmap.isNull + " " + bitmap.isEmpty)
            composeBitmap = bitmap.asComposeImageBitmap()
            this.time = time
        }

    }


    @Composable
    fun frameCanvas(
        modifier: Modifier,
    ) {
        val bridge = bridge
        if (bridge != null) {
            val scaleType by bridge.scaleTypeFlow.collectAsState()
            val videoSize by bridge.videoSizeFlow.collectAsState()

            LaunchedEffect(scaleType, videoSize) {
                frameSizeCalculator.setScaleType(scaleType)
                frameSizeCalculator.setVideoSize(videoSize)
            }

            DisposableEffect(Unit) {
                bridge.setFrameListener(this@VlcPlayerFrameState)
                onDispose {
                    bridge.removeFrameListener()
                    composeBitmap = null
                }
            }
            val bmp = composeBitmap

            Canvas(modifier) {
                drawRect(color = Color.Black, Offset.Zero, size)
                if (bmp != null) {
                    frameSizeCalculator.setCanvasSize(size)
                    frameSizeCalculator.calculate()

//                println(frameSizeCalculator.frameOffset.toString() + " " + frameSizeCalculator.frameSize)
                    drawImage(
                        image = bmp,
                        dstOffset = frameSizeCalculator.frameOffset,
                        dstSize = frameSizeCalculator.frameSize,
                    )
                }

            }
        }

    }




}

internal class VlcFrameSizeCalculator {

    @Volatile
    private var dirty = true

    private var videoSize = IntSize.Zero
    private var canvasSize = Size.Zero
    private var scaleType: C.RendererScaleType = C.RendererScaleType.SCALE_ADAPT

    var frameOffset = IntOffset.Zero
        private set

    var frameSize = IntSize.Zero
        private set

    fun setVideoSize(size: VideoSize) {
        size.logicSize ?.let {
            if (videoSize.width != it.first || videoSize.height != it.second) {
                videoSize = IntSize(it.first, it.second)
                dirty = true
            }

        }
    }

    fun setCanvasSize(size: Size) {
        if (canvasSize != size && size.height > 0 && size.width > 0) {
            canvasSize = size
            dirty = true
        }
    }

    fun setScaleType(type: C.RendererScaleType) {
        if (scaleType != type) {
            scaleType = type
            dirty = true
        }
    }

    // 懒加载
    private val pair16r9: Pair<Int, Int> by lazy {
        Pair(16, 9)
    }
    private val pair4r3: Pair<Int, Int> by lazy {
        Pair(4, 3)
    }


    fun calculate() {
        if (!dirty) return
        when (scaleType) {
            // 原尺寸居中输出，不考虑遮挡
            C.RendererScaleType.SCALE_SOURCE -> {
                val xx = canvasSize.width - videoSize.width
                val yy = canvasSize.height - videoSize.height
                frameOffset = IntOffset((xx / 2f).roundToInt(), (yy / 2f).roundToInt())
                frameSize = IntSize(videoSize.width, videoSize.height)
            }


            C.RendererScaleType.SCALE_16_9 -> {
                centerAdapt(pair16r9)
            }
            C.RendererScaleType.SCALE_4_3 -> {
                centerAdapt(pair4r3)
            }
            C.RendererScaleType.SCALE_ADAPT -> {
//                println("${videoSize}")
                centerAdapt(videoSize.width to videoSize.height)
            }


            C.RendererScaleType.SCALE_MATCH_PARENT -> {
                frameOffset = IntOffset(0, 0)
                frameSize = IntSize(canvasSize.width.roundToInt(), canvasSize.height.roundToInt())

            }
            C.RendererScaleType.SCALE_CENTER_CROP -> {
                // 平铺，从中心裁切，保证占满屏幕
                // 和 centerAdapt 相反
                if (canvasSize.width * videoSize.height > canvasSize.height * videoSize.width) {
                    // 宽度为准
                    val width = canvasSize.width
                    val height =( width * videoSize.height / videoSize.width).roundToInt()
                    frameOffset = IntOffset(0, ((canvasSize.height - height) / 2f).roundToInt())
                    frameSize = IntSize(width.roundToInt(), height)
                } else {
                    // 高度为准
                    val height = canvasSize.height
                    val width = height * videoSize.width / videoSize.height
                    frameOffset = IntOffset(((canvasSize.width - width) / 2f).roundToInt(), 0)
                    frameSize = IntSize(width.roundToInt(), height.roundToInt())
                }

            }
            C.RendererScaleType.SCALE_FOR_HEIGHT -> {
                // 以高度为准
                val height = canvasSize.height
                val width = height * videoSize.width / videoSize.height
                frameOffset = IntOffset(((canvasSize.width - width) / 2f).roundToInt(), 0)
                frameSize = IntSize(width.roundToInt(), height.roundToInt())

            }
            C.RendererScaleType.SCALE_FOR_WIDTH -> {
                // 以宽度为准
                val width = canvasSize.width
                val height = width * videoSize.height / videoSize.width
                frameOffset = IntOffset(0, ((canvasSize.height - height) / 2f).roundToInt())
                frameSize = IntSize(width.roundToInt(), height.roundToInt())
            }
        }



    }

    // 居中适应屏幕，保持宽高比
    private fun centerAdapt(
        radio: Pair<Int, Int>,
    ) {
        if (radio.first <= 0 || radio.second <= 0) {
            frameSize = canvasSize.toIntSize()
            frameOffset = IntOffset.Zero
        } else {
//            println(videoSize.toString() + " " + canvasSize.toString() + " " + radio.toString())
            if (canvasSize.width * radio.second <= canvasSize.height * radio.first) {
                // 宽度为准
                val width = canvasSize.width
                val height = width * radio.second / radio.first
                frameOffset = IntOffset(0, ((canvasSize.height - height) / 2f).roundToInt())
                frameSize = IntSize(width.roundToInt(), height.roundToInt())
            } else {
                // 高度为准
                val height = canvasSize.height
                val width = height * radio.first / radio.second
                frameOffset = IntOffset(((canvasSize.width - width) / 2f).roundToInt(), 0)
                frameSize = IntSize(width.roundToInt(), height.roundToInt())
            }
        }

    }

}

