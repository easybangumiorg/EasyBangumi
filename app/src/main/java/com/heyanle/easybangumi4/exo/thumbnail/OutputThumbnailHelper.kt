package com.heyanle.easybangumi4.exo.thumbnail

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.alien.gpuimage.outputs.BitmapOut
import com.alien.gpuimage.sources.OesTexturePipeline
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanle on 2024/6/16.
 * https://github.com/heyanLE
 */
class OutputThumbnailHelper(
    private val context: Context,
    private val mediaSource: MediaSource,
    private val rootFolder: File,
    private val thumbnailBuffer: ThumbnailBuffer,
    private val start: Long,
    private val end: Long,
    private val interval: Long,
) : BitmapOut(), Player.Listener {

    private val dispatcher = CoroutineProvider.newSingleDispatcher
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mainScope = MainScope()

    var onVideoSizeChange: ((VideoSize)->Unit)? = null

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build()
    }
    private val exoplayerPipeline: OesTexturePipeline by lazy {
        OesTexturePipeline()
    }

    @Volatile
    private var waitPosition = start

    @OptIn(UnstableApi::class)
    fun start(){
        // rootFolder.deleteRecursively()
        exoPlayer.addListener(this)
        exoPlayer.setVideoSurface(exoplayerPipeline.getSurface())
        exoplayerPipeline.addTarget(this)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.volume = 0f
        exoPlayer.setPlaybackSpeed(100f)
        exoPlayer.playWhenReady = false
        exoPlayer.seekTo(waitPosition)
    }

    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        "newFrameReadyAtTime $time $textureIndex".logi("OutputThumbnailHelper")
        super.newFrameReadyAtTime(time, textureIndex)
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        "onPlaybackStateChanged $playbackState".logi("OutputThumbnailHelper")
    }
    override fun dispatchCallback(time: Long, bitmap: Bitmap?) {
        mainScope.launch {
            val currentPosition = exoPlayer.currentPosition
            "dispatchCallback $currentPosition $bitmap 1".logi("OutputThumbnailHelper")
            if (currentPosition > end){
                exoPlayer.stop()
                return@launch
            }
            "dispatchCallback $currentPosition $bitmap 2".logi("OutputThumbnailHelper")
            waitPosition += interval
            exoPlayer.seekTo(waitPosition)
            "dispatchCallback $currentPosition $bitmap $waitPosition 3 ".logi("OutputThumbnailHelper")
            bitmap ?: return@launch
            // 优化一波
            if (thumbnailBuffer.getThumbnail(currentPosition, interval) != null){
                return@launch
            }
            scope.launch {
                "dispatchCallback $currentPosition $bitmap 3".logi("OutputThumbnailHelper")
                val bmp = bitmap.copy(bitmap.config, false)
                "dispatchCallback $time $bitmap 4".logi("OutputThumbnailHelper")
                rootFolder.mkdirs()
                val file = TagFile(rootFolder, "${currentPosition}.jpg")
                file.delete()
                file.createNewFile()
                file.deleteOnExit()
                file.tag = "dddd"
                file.outputStream().use {
                    bmp.compress(Bitmap.CompressFormat.JPEG, 10, it)
                }
                bmp.recycle()
                thumbnailBuffer.addThumbnail(currentPosition, file)
            }
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        exoplayerPipeline.setFormat(videoSize.width, videoSize.height, 0)
        onVideoSizeChange?.invoke(videoSize)
    }

    override fun release(){
        super.release()
        exoPlayer.removeListener(this)
        exoPlayer.release()
        exoplayerPipeline.release()
    }


}