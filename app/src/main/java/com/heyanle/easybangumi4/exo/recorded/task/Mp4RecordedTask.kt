package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.SpeedChangeEffect
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
import com.heyanle.easybangumi4.exo.ClippingConfigMediaSourceFactory
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.safeResume
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

/**
 * 视频剪辑任务
 * 使用 Transformer 实现，支持任何 MediaSource 包括 HLS
 * 1. 剪辑
 * 2. 裁剪
 * 3. 抽帧
 * 4. 压缩
 * 5. 变速
 * Created by heyanlin on 2024/6/24.
 */
@OptIn(UnstableApi::class)
class Mp4RecordedTask(
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
): Transformer.Listener, AbsRecordedTask() {

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
        .setRemoveAudio(false)
        .setEffects(
            Effects(
                listOf(),
                listOf(
                    // 变速
                    SpeedChangeEffect(speed),
                    // 码率（抽帧）
                    FrameDropEffect.createDefaultFrameDropEffect(5f), //fps.toFloat()),
                    // 裁剪
                    crop,
                    // 压制
                    ScaleAndRotateTransformation.Builder().setScale(quality/100f, quality/100f).build()
                )
            ),

            ).build()
    private val transformer = Transformer.Builder(ctx)
        .setVideoMimeType(MimeTypes.VIDEO_H265)
        .setAssetLoaderFactory(
            ExoPlayerAssetLoader.Factory(
            ctx, DefaultDecoderFactory.Builder(ctx).build(),  Clock.DEFAULT, mediaSourceFactory)
        )
        .setMuxerFactory(InAppMuxer.Factory.Builder().build())
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