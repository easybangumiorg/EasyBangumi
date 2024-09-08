package com.heyanle.easybangumi4.exo.recorded.task

import android.content.Context
import android.graphics.RectF
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

    // x 轴和 y 轴都是 [0,1]，该矩形表示裁剪范围占各个边的比例
    // y 轴正方向向下
    private val cropRect: RectF,

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
                    FrameDropEffect.createDefaultFrameDropEffect(fps.toFloat()), //fps.toFloat()),
                    // 裁剪
                    run {
                        // 1. 平移到 [-0.5,0.5]
                        var cropRealRect = cropRect.run {
                            RectF(
                                left - 0.5f,
                                top - 0.5f,
                                right - 0.5f,
                                bottom - 0.5f
                            )
                        }

                        // 2. 缩放到 [-1,1]
                        cropRealRect = cropRealRect.run {
                            RectF(
                                left * 2,
                                top * 2,
                                right * 2,
                                bottom * 2
                            )
                        }

                        // 3. 反转 y 轴方向
                        cropRealRect = cropRealRect.run {
                            RectF(
                                left,
                                - top,
                                right,
                                - bottom
                            )
                        }
                        Crop(
                            cropRealRect.left,
                            cropRealRect.right,
                            cropRealRect.bottom,
                            cropRealRect.top,
                        )
                    },
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