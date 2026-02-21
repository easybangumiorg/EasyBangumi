package org.easybangumi.next.libplayer.vlcj.bitmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.AbsPlayerBridge
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.api.VideoSize
import org.easybangumi.next.libplayer.api.action.Action
import org.easybangumi.next.libplayer.api.action.SpeedAction
import org.easybangumi.next.libplayer.vlcj.BaseVlcjPlayerBridge
import org.easybangumi.next.libplayer.vlcj.VlcjBridgeManager
import org.easybangumi.next.libplayer.vlcj.action.VlcjSpeedAction
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2025/5/27.
 */
class VlcjPlayerBitmapBridge(
    private val manager: VlcjBridgeManager,
    customFrameScope: CoroutineScope?,
) : BaseVlcjPlayerBridge(manager), AutoCloseable {




    @Volatile
    private var imageInfo: ImageInfo? = null

    // Double buffer: write in display(), read in firePendingFrame(); next frame writes the other slot
    @Volatile
    private var buffer0: ByteArray? = null
    @Volatile
    private var buffer1: ByteArray? = null
    @Volatile
    private var writeIndex = 0

    private fun getBufferForWrite(size: Int): ByteArray {
        val idx = writeIndex
        val buf = if (idx == 0) buffer0 else buffer1
        if (buf?.size != size) {
            val newBuf = ByteArray(size)
            if (idx == 0) buffer0 = newBuf else buffer1 = newBuf
            return newBuf
        }
        return buf
    }

    @Volatile
    private var pendingReadBuffer: ByteArray? = null

    private val bitmap = Bitmap()


    interface OnFrameListener {
        fun onFrame(bitmap: Bitmap, time: Long)
    }

    @Volatile
    private var frameListener: OnFrameListener? = null

    // Reuse single Runnable to avoid per-frame allocation in display()
    @Volatile
    private var pendingFrameTime = -1L

    private val sharedFrameRunnable = Runnable { firePendingFrame() }

    private fun firePendingFrame() {
        val t = pendingFrameTime
        if (t < 0) return
        val array = pendingReadBuffer ?: return
        pendingFrameTime = -1L
        pendingReadBuffer = null
        val listener = frameListener ?: return
        val i = imageInfo ?: return
        bitmap.setImageInfo(i)
        bitmap.installPixels(i, array, i.width * 4)
        listener.onFrame(bitmap, t)
    }

    private val renderCallback = object : RenderCallback {
        override fun lock(mediaPlayer: MediaPlayer?) {}

        override fun display(
            mediaPlayer: MediaPlayer,
            nativeBuffers: Array<out ByteBuffer>,
            bufferFormat: BufferFormat,
            displayWidth: Int,
            displayHeight: Int
        ) {
            val listener = frameListener ?: return
            val buffer = nativeBuffers.getOrNull(0) ?: return
            val i = imageInfo ?: return
            buffer.rewind()
            val size = buffer.remaining()
            val array = getBufferForWrite(size)
            buffer.get(array)
            pendingReadBuffer = array
            pendingFrameTime = mediaPlayer.status().time()
            writeIndex = 1 - writeIndex
            customFrameScope?.launch { sharedFrameRunnable.run() } ?: SwingUtilities.invokeLater(sharedFrameRunnable)
        }

        override fun unlock(mediaPlayer: MediaPlayer?) {}
    }


    private val bufferFormatCallback = object: BufferFormatCallback {

        private var lastDisplayerWidth = 0
        private var lastDisplayerHeight = 0

        override fun getBufferFormat(
            sourceWidth: Int,
            sourceHeight: Int
        ): BufferFormat? {
            logger.debug("getBufferFormat called: $sourceWidth x $sourceHeight")
            imageInfo = ImageInfo(
                sourceWidth,
                sourceHeight,
                // 忽略 alpha 通道后实际上是 BGR_888X
                ColorType.BGRA_8888,
                ColorAlphaType.OPAQUE,
            )
            //logger.debug("imageInfo set: $imageInfo")
            return RV32BufferFormat(sourceWidth, sourceHeight)
        }

        override fun newFormatSize(
            bufferWidth: Int,
            bufferHeight: Int,
            displayWidth: Int,
            displayHeight: Int
        ) {
            logger.debug("newFormatSize: $bufferWidth x $bufferHeight, display: $displayWidth x $displayHeight")
            if (lastDisplayerWidth != displayWidth || lastDisplayerHeight != displayHeight) {
                lastDisplayerWidth = displayWidth
                lastDisplayerHeight = displayHeight
                innerVideoSizeFlow.update {
                    VideoSize(
                        width = displayWidth,
                        height = displayHeight,
                    )
                }
            }
        }

        override fun allocatedBuffers(buffers: Array<out ByteBuffer>) {
            buffers.firstOrNull()?.remaining()?.let { size ->
                getBufferForWrite(size)
                writeIndex = 1 - writeIndex
                getBufferForWrite(size)
            }
        }
    }

    val callbackVideoSurface = CallbackVideoSurface(
        bufferFormatCallback,
        renderCallback,
        true,
        VideoSurfaceAdapters.getVideoSurfaceAdapter()
    )

    fun setFrameListener(frameListener: OnFrameListener) {
        this.frameListener = frameListener
    }

    fun removeFrameListener() {
        this.frameListener = null
    }

    init {
        callbackVideoSurface.attach(mediaPlayer)
    }



}