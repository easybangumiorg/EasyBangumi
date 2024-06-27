package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Log
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.SpeedChangeEffect
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMuxer
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import com.alien.gpuimage.outputs.BitmapView
import com.alien.gpuimage.sources.ExoplayerPipeline
import com.heyanle.easy_transformer.gif.FramePixelCodec
import com.heyanle.easy_transformer.gif.GifMuxer
import com.heyanle.easy_transformer.gif.GifTransformer
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

    //NDC 坐标
    private val crop: Crop,
    private val fps: Int,
    private val quality: Int,
    private val speed: Float,
) : AbsRecordedTask(), Transformer.Listener {

    // transformer 转码 ===========================
    private val inputMediaItem = mediaItem.buildUpon().apply {
        setClippingConfiguration(
            MediaItem.ClippingConfiguration.Builder()
                // 剪辑
                .setStartPositionMs(startPosition)
                .setEndPositionMs(endPosition)
                .setStartsAtKeyFrame(false)
                .build()
        )
    }.build()
    private val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
        .setRemoveAudio(true)
        .setEffects(
            Effects(
                listOf(),
                listOf(
                    // 变速
                    SpeedChangeEffect(speed),
                    // 码率（抽帧）
                    FrameDropEffect.createDefaultFrameDropEffect(fps.toFloat()), //fps.toFloat()),
                    // 裁剪
                    crop,
                    // 压制
                    ScaleAndRotateTransformation.Builder().setScale(quality/100f, quality/100f).build()
                )
            ),

            ).build()
    private val transformer = Transformer.Builder(ctx)
        .setAssetLoaderFactory(
            ExoPlayerAssetLoader.Factory(
                ctx, DefaultDecoderFactory.Builder(ctx).build(),  Clock.DEFAULT, mediaSourceFactory)
        )
        .setEncoderFactory(FramePixelCodec.Factory())
        .setMuxerFactory(
            GifMuxer.Factory()
        )
        .setVideoMimeType(MimeTypes.VIDEO_H264)
        .setRemoveAudio(true)
        .addListener(this)
        .build()

    // 转码挂起点
    private var transformCon: Continuation<Pair<ExportResult, ExportException?>>? = null

    // 转码状态回调 ===========================

    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
        super.onCompleted(composition, exportResult)
        transformCon?.safeResume(exportResult to null)
    }

    override fun onError(
        composition: Composition,
        exportResult: ExportResult,
        exportException: ExportException
    ) {
        super.onError(composition, exportResult, exportException)
        transformCon?.safeResume(exportResult to exportException)
    }

    override fun onFallbackApplied(
        composition: Composition,
        originalTransformationRequest: TransformationRequest,
        fallbackTransformationRequest: TransformationRequest
    ) {
        super.onFallbackApplied(
            composition,
            originalTransformationRequest,
            fallbackTransformationRequest
        )
        "onFallbackApplied".logi("Mp4RecordedTask")
    }

    // 外部接口 ===========================
    override fun start() {
        if (state.value.status != 0){
            return
        }

        scope.launch {
            try {
                innerTransform()
            }catch (e: Exception){
                e.printStackTrace()
                dispatchError(e, e.message)
            }
        }
    }

    override fun stop() {
        transformer.cancel()
    }

    // 开始处理 ！
    private suspend fun innerTransform(){
        // 虽然 Transformer 支持进度但是这里需要开多一个线程去轮询，所以暂时不处理进度
        val outputFile = File(outputFolder, outputName)
        dispatchProcess(-1)
        val res = suspendCoroutine<Pair<ExportResult, ExportException?>> {
            transformCon = it
            outputFolder.mkdirs()

            outputFile.delete()
            outputFile.createNewFile()
            mainScope.launch {
                transformer.start(editedMediaItem, outputFile.absolutePath)
            }

        }
        val error = res.second
        if (error != null){
            dispatchError(error, error.message)
        }else{
            dispatchCompletely(outputFile)
        }
    }

}