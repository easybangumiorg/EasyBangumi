package org.easybangumi.next.libplayer.vlcj

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.libplayer.api.AbsPlayerBridge
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.MediaItem
import org.easybangumi.next.libplayer.api.VideoSize
import org.easybangumi.next.libplayer.api.action.Action
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.media.TrackType
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.awt.EventQueue
import java.nio.ByteBuffer
import javax.swing.SwingUtilities
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2025/5/27.
 */
class VlcjPlayerBridge(
    private val manager: VlcjBridgeManager,
    customFrameScope: CoroutineScope?,
) : AbsPlayerBridge(), AutoCloseable {

    private val logger = logger()

    private val mediaPlayer: MediaPlayer by lazy {
        manager.createMediaPlayer()
    }

    override val impl: Any
        get() = mediaPlayer

//    private val mediaPlayerLazy = lazy {
//        val mp = reentrantLock.withLock {
//            mediaPlayerFactory.mediaPlayers().newMediaPlayer()
//        }
//        callbackVideoSurface.attach(mp)
//        mp.events().addMediaPlayerEventListener(mediaPlayerEventListener)
//        mp
//    }
//    private val mediaPlayer: MediaPlayer by mediaPlayerLazy

    override val positionMs: Long
        get() = if (seekingTargetTime < 0) mediaPlayer.status().time() else seekingTargetTime

    // unsupported
    override val bufferedPositionMs: Long
        get() = C.TIME_UNSET

    override val durationMs: Long
        get() = mediaPlayer.status().length()

    override fun prepare(mediaItem: MediaItem) {
        //logger.debug("Preparing media: ${mediaItem.uri}")
        if (innerPlayWhenReadyFlow.value) {
            val result = mediaPlayer.media().play(mediaItem.uri)
            //logger.debug("Media play result: $result")
        } else {
            val result = mediaPlayer.media().prepare(mediaItem.uri)
            //logger.debug("Media prepare result: $result")
        }
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        mediaPlayer.controls().setPause(!playWhenReady)
        if (playWhenReady) {
            mediaPlayer.controls().play()
        }
        innerPlayWhenReadyFlow.update { playWhenReady }
    }

    @Volatile
    private var seekingTargetTime = -1L

    override fun seekTo(positionMs: Long) {
        SwingUtilities.invokeLater {
            mediaPlayer.controls().setTime(positionMs)
            seekingTargetTime = positionMs
        }
    }


    override fun setScaleType(scaleType: C.RendererScaleType) {
        innerScaleTypeFlow.update { scaleType }
    }


    override fun prepareAction(): Map<KClass<out Action>, Action> {
        return mapOf(

        )
    }

    override fun close() {
        mediaPlayer.events().removeMediaPlayerEventListener(mediaPlayerEventListener)
        mediaPlayer.release()
    }

    @Volatile
    private var imageInfo: ImageInfo? = null

    // 缓存一波
    @Volatile
    private var bufferArray: ByteArray? = null
    private fun getBufferArray(size: Int): ByteArray {
        val ba = bufferArray
        if (ba?.size == size) {
            return ba
        }

        val nb = ByteArray(size)
        bufferArray = nb
        return nb
    }

    private val bitmap = Bitmap()


    interface OnFrameListener {
        fun onFrame(bitmap: Bitmap, time: Long)
    }

    @Volatile
    private var frameListener: OnFrameListener? = null

    private val renderCallback = object: RenderCallback {
        override fun lock(mediaPlayer: MediaPlayer?) {

        }

        override fun display(
            mediaPlayer: MediaPlayer,
            nativeBuffers: Array<out ByteBuffer>,
            bufferFormat: BufferFormat,
            displayWidth: Int,
            displayHeight: Int
        ) {
            var current = System.nanoTime()
            //logger.debug("display: ${nativeBuffers.size}, $displayWidth x $displayHeight")
            val time = mediaPlayer.status().time()
            val listener = frameListener
            if (listener == null) {
                //logger.debug("No frame listener set")
                return
            }
            fun fireFrame(){
                val i = imageInfo
                if (i == null) {
                    //logger.debug("imageInfo is null, skipping frame")
                    return
                }
                val buffer = nativeBuffers.getOrNull(0)
                if (buffer == null) {
                    //logger.debug("No buffer available")
                    return
                }
                buffer.rewind()
                val array = getBufferArray(buffer.remaining())
                buffer.get(array)
                bitmap.setImageInfo(i)
                // RV32 format 单像素占 4 字节  BGR_888X
                bitmap.installPixels(i, array, bufferFormat.width * 4)
                listener.onFrame(bitmap, time)
                //logger.debug("Frame sent to listener")
            }
            // 如果没有自定义协程则运行到 awt event 中
            customFrameScope?.launch {
                fireFrame()
            } ?: SwingUtilities.invokeLater {
                fireFrame()
            }

            current = System.nanoTime() - current
            //logger.debug("displayTime ${current}")
        }

        override fun unlock(mediaPlayer: MediaPlayer?) {

        }
    }


    private val bufferFormatCallback = object: BufferFormatCallback {

        private var lastDisplayerWidth = 0
        private var lastDisplayerHeight = 0

        override fun getBufferFormat(
            sourceWidth: Int,
            sourceHeight: Int
        ): BufferFormat? {
            //logger.debug("getBufferFormat called: $sourceWidth x $sourceHeight")
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
            //logger.debug("newFormatSize: $bufferWidth x $bufferHeight, display: $displayWidth x $displayHeight")
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
            //logger.debug("allocatedBuffers: ${buffers.size}")

            buffers.firstOrNull()?.remaining()?.let {
                // 提前申请堆内存优化一下效率
                getBufferArray(it)
            }
        }
    }

    private val mediaPlayerEventListener = object: MediaPlayerEventListener {
        override fun mediaChanged(
            mediaPlayer: MediaPlayer?,
            media: MediaRef?
        ) {
            
        }

        override fun opening(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                //logger.debug("VLC MediaPlayer opening")
                innerPlayStateFlow.update { C.State.PREPARING }
            }
        }

        // vlc 如果 seek 超过多个关键帧，会先定位到对应关键帧走一遍 buffering，在定位到目标 走一遍 buffering
        // 因此哪怕 newCache 为 100 但是时间不对也需要视为在加载
        // 并且因为帧率问题最终可能不会精准定位到对应 ms，添加 1.5s 宽容度
        override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
            val time =  mediaPlayer?.status()?.time()
            SwingUtilities.invokeLater {
                logger.debug("buffering: $newCache seekingTargetTime: $seekingTargetTime time: ${mediaPlayer?.status()?.time()}")
                if (newCache < 100f) {
                    innerPlayStateFlow.update { C.State.BUFFERING }
                } else {
                    if (time == null || seekingTargetTime == -1L) {
                        innerPlayStateFlow.update { C.State.READY }
                        return@invokeLater
                    }
                    if ((time - seekingTargetTime).absoluteValue < 15000) {
                        // 暂停状态需要渲染目标位置静态帧
                        if (!innerPlayWhenReadyFlow.value) {
                            mediaPlayer?.controls()?.nextFrame()
                        }
                        seekingTargetTime = -1L
                        innerPlayStateFlow.update { C.State.READY }
                    } else {
                        innerPlayStateFlow.update { C.State.BUFFERING }
                    }

                }
            }


        }

        override fun playing(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                //logger.debug("VLC MediaPlayer playing")
                innerPlayStateFlow.update { C.State.READY }
                innerPlayWhenReadyFlow.update { true }
            }
        }

        override fun paused(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayWhenReadyFlow.update { false }
            }
        }

        override fun stopped(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.IDLE }
            }
        }

        override fun forward(mediaPlayer: MediaPlayer?) {
            
        }

        override fun backward(mediaPlayer: MediaPlayer?) {
            
        }

        override fun finished(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.ENDED }
            }
        }

        override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {

        }

        override fun positionChanged(
            mediaPlayer: MediaPlayer?,
            newPosition: Float
        ) {

        }

        override fun seekableChanged(
            mediaPlayer: MediaPlayer?,
            newSeekable: Int
        ) {

        }

        override fun pausableChanged(
            mediaPlayer: MediaPlayer?,
            newPausable: Int
        ) {

        }

        override fun titleChanged(mediaPlayer: MediaPlayer?, newTitle: Int) {

        }

        override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {
            
        }

        override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {
            
        }

        override fun videoOutput(mediaPlayer: MediaPlayer?, newCount: Int) {

        }

        override fun scrambledChanged(
            mediaPlayer: MediaPlayer?,
            newScrambled: Int
        ) {
            
        }

        override fun elementaryStreamAdded(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {
            
        }

        override fun elementaryStreamDeleted(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {
            
        }

        override fun elementaryStreamSelected(
            mediaPlayer: MediaPlayer?,
            type: TrackType?,
            id: Int
        ) {
            
        }

        override fun corked(mediaPlayer: MediaPlayer?, corked: Boolean) {
            
        }

        override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {
            
        }

        override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {
            
        }

        override fun audioDeviceChanged(
            mediaPlayer: MediaPlayer?,
            audioDevice: String?
        ) {
            
        }

        override fun chapterChanged(mediaPlayer: MediaPlayer?, newChapter: Int) {
            
        }

        override fun error(mediaPlayer: MediaPlayer?) {
            logger.error("VLC MediaPlayer error occurred")
//            innerPlayStateFlow.update { C.State.ERROR }
        }

        override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
            SwingUtilities.invokeLater {
                innerPlayStateFlow.update { C.State.READY }
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
        mediaPlayer.events().addMediaPlayerEventListener(mediaPlayerEventListener)
    }



}