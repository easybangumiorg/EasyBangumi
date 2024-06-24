package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import com.alien.gpuimage.outputs.BitmapView
import com.alien.gpuimage.sources.ExoplayerPipeline
import com.heyanle.easybangumi4.exo.ClippingConfigMediaSourceFactory
import com.heyanle.easybangumi4.utils.AnimatedGifEncoder
import com.heyanle.easybangumi4.utils.safeResume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanlin on 2024/6/24.
 */
@OptIn(UnstableApi::class)
class GifRecordedTask(
    private val ctx: Context,
    private val mediaItem: MediaItem,
    private val mediaSourceFactory: ClippingConfigMediaSourceFactory,

    private val outputFolder: File,
    private val outputName: String,

    private val startPosition: Long,
    private val endPosition: Long,

    //NDC 坐标
    private val crop: Crop,
    private val fps: Int,
    private val quality: Int,
    private val speed: Float,
) : AbsRecordedTask(), Player.Listener, BitmapView.BitmapViewCallback {

    private val cacheFolder = File(outputFolder, "${outputName}-temp")
        .apply {
            mkdirs()
            deleteOnExit()
        }

    // exoPlayer（变速等需求直接在 ExoPlayer 里实现） -> exoplayerPipeline（ OES 纹理转普通纹理 ） -> bitmapView -> jpg 文件
    private val bitmapView = BitmapView().apply {
        callback = this@GifRecordedTask
    }
    private val exoplayerPipeline: ExoplayerPipeline by lazy {
        ExoplayerPipeline().apply {
            addTarget(bitmapView)
        }
    }
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(ctx).build().apply {
            setVideoSurface(exoplayerPipeline.getSurface())
            setVideoEffects(
                listOf(
                    // 码率（抽帧）
                    FrameDropEffect.createDefaultFrameDropEffect(fps.toFloat()),
                    // 裁剪
                    crop,
                    // 压制
                    ScaleAndRotateTransformation.Builder().setScale(quality/100f, quality/100f).build()
                )
            )
            // 这里变速直接修改 exoPlayer
            setPlaybackSpeed(speed)

            // 剪辑在 ClippingConfigMediaSourceFactory 中实现
            setMediaSource(mediaSourceFactory.createMediaSource(mediaItem))

            playWhenReady = true
        }
    }

    @Volatile
    private var lastFrameTime = 0L
    private var checkJob: Job? = null

    // 挂起点
    private var exoPlayCon: Continuation<Exception?>? = null

    private val startTime = AtomicLong(-1L)


    override fun onViewSwapToScreen(bitmap: Bitmap?, time: Long?) {
        // 缓存帧图
        bitmap ?: return
        time ?: return
        // 这里 time 只有相对信息，没有绝对信息，这里需要计算
        startTime.compareAndSet(0, time)
        lastFrameTime = time
        scope.launch {
            val current = time - startTime.get() + startPosition
            val cop = bitmap.copy(bitmap.config, false)
            cacheFolder.mkdirs()
            val file = File(cacheFolder, "${current}.jpg")
            file.delete()
            file.createNewFile()
            file.deleteOnExit()
            cop.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())
            dispatchProcess((((current - startPosition)/(endPosition - startPosition)) * 50).toInt())
            if (current > endPosition){
                exoPlayer.stop()
                exoPlayCon?.safeResume(null)
            }
        }
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED){
           exoPlayCon?.safeResume(null)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        exoPlayCon?.safeResume(error)
    }




    override fun start() {
        if (state.value.status != 0 || startTime.get() != -1L){
            return
        }
        scope.launch {
            // 取帧图
            val getFrame = suspendCoroutine<Exception?> {

                // 加一个检查 job
                checkJob?.cancel()
                checkJob = scope.launch {
                    var lastFrameTemp = lastFrameTime
                    while (isActive){
                        if (lastFrameTime == lastFrameTemp){
                            exoPlayer.stop()
                        }
                        lastFrameTemp = lastFrameTime
                        delay(2000)
                    }
                }

                exoPlayer.prepare()
                dispatchProcess(0)
                startTime.set(0)
                exoPlayCon = it
            }
            exoPlayer.stop()
            if (getFrame != null){
                dispatchError(getFrame, getFrame.message)
            }

            dispatchProcess(50)
            // 打包 gif

            val target = File(outputFolder, outputName)
            val jpgFolder = cacheFolder

            val baos = ByteArrayOutputStream()
            val gif = AnimatedGifEncoder()
            gif.start(baos)
            gif.setDelay(1000/fps)
            gif.setRepeat(0)
            jpgFolder.listFiles()?.sortedBy { it.nameWithoutExtension.toInt() }?.forEach {

            }


        }
    }

    override fun stop() {
        exoPlayer.stop()
        exoPlayCon?.safeResume(CancellationException())

    }

}