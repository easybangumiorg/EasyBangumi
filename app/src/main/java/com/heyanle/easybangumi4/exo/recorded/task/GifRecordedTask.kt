package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Log
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.ExoPlayer
import com.alien.gpuimage.outputs.BitmapView
import com.alien.gpuimage.sources.ExoplayerPipeline
import com.heyanle.easybangumi4.exo.ClippingConfigMediaSourceFactory
import com.heyanle.easybangumi4.utils.AnimatedGifEncoder
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.safeResume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.Exception
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

    private val targetWidth: Int,
    private val targetHeight: Int,

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
    private val exoplayerPipeline: ExoplayerPipeline = ExoplayerPipeline().apply {
        setFormat(
            targetWidth,
            targetHeight,
            0
        )

        addTarget(bitmapView)

    }
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(ctx).build().apply {
        addListener(this@GifRecordedTask)

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
        volume = 0f
    }


    private val singleDispatcher = CoroutineProvider.CUSTOM_SINGLE
    private val priorityQueue = PriorityQueue<Pair<Long, File>>(
        compareBy { it.first }
    )

    @Volatile
    private var lastFrameTime = 0L
    private var checkJob: Job? = null

    // 挂起点
    private var exoPlayCon: Continuation<Exception?>? = null

    private val startTime = AtomicLong(-1L)


    override fun onViewSwapToScreen(bitmap: Bitmap?, time: Long?) {
        "${bitmap} ${time}".logi("GifRecordedTask")
        // 缓存帧图
        bitmap ?: return
        time ?: return
        // 这里 time 只有相对信息，没有绝对信息，这里需要计算
        startTime.compareAndSet(0, time)
        lastFrameTime = time
        scope.launch(singleDispatcher) {
            try {
                val current = time - startTime.get() + startPosition
                val cop = bitmap.copy(bitmap.config, false)
                cacheFolder.mkdirs()
                val file = File(cacheFolder, "${current}.jpg")
                file.delete()
                file.createNewFile()
                // file.deleteOnExit()
                cop.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())
                cop.recycle()
                file.absolutePath.logi("GifRecordedTask")
                priorityQueue.add(current to file)
                dispatchProcess((((current - startPosition)/(endPosition - startPosition)) * 50).toInt())
                if (current > endPosition){
                    mainScope.launch {
                        exoPlayCon?.safeResume(null)
                        exoPlayer.stop()
                    }

                }
            }catch (e: Exception){
                e.printStackTrace()
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

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        "${videoSize.width} ${videoSize.height}".logi("GifRecordedTask")
        // exoplayerPipeline.setFormat(videoSize.width, videoSize.height, 0)
    }




    override fun start() {
        if (state.value.status != 0 || startTime.get() != -1L){
            return
        }
        scope.launch {
            priorityQueue.clear()
            // 取帧图
            val getFrame = suspendCoroutine<Exception?> {
                mainScope.launch {
                    exoPlayer.prepare()
                    exoPlayer.play()
                    dispatchProcess(0)
                    startTime.set(0)
                    exoPlayCon = it

                    // 加一个检查 job
                    checkJob?.cancel()
                    checkJob = scope.launch {
                        var lastFrameTemp = lastFrameTime
                        while (isActive){
                            delay(2000)
                            if (lastFrameTime == lastFrameTemp){
                                exoPlayCon?.safeResume(CancellationException())
                                mainScope.launch {
                                    exoPlayer.stop()
                                }
                            }
                            lastFrameTemp = lastFrameTime

                        }
                    }
                }

            }


            checkJob?.cancel()
            if (getFrame != null){
                dispatchError(getFrame, getFrame.message)
                return@launch
            }
            mainScope.launch {
                exoPlayer.stop()
            }

            dispatchProcess(50)
            // 打包 gif

            outputFolder.mkdirs()
            val target = File(outputFolder, outputName)

            ByteArrayOutputStream().use {  baos ->
                val gif = AnimatedGifEncoder()
                gif.start(baos)
                gif.setDelay(1000/fps)
                gif.setRepeat(0)
                for (pair in priorityQueue) {
                    if (!pair.second.exists()){
                        continue
                    }
                    val bitmap = BitmapFactory.decodeFile(pair.second.absolutePath)
                    gif.addFrame(bitmap)
                    bitmap.recycle()
                }
                dispatchProcess(75)
                target.createNewFile()

                target.outputStream().use {
                    baos.writeTo(it)
                    baos.flush()
                }
                dispatchCompletely(target)
            }

        }
    }

    override fun stop() {
        checkJob?.cancel()
        exoPlayer.stop()
        exoPlayCon?.safeResume(CancellationException())

    }

}