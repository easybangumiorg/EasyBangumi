package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.task

import android.content.Context
import android.widget.AutoCompleteTextView.OnDismissListener
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.media3.common.C
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Crop
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.transformer.Codec.DecoderFactory
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
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Created by heyanle on 2024/6/23.
 * https://github.com/heyanLE
 */
class CartoonRecordedTaskModel(
    val ctx: Context,
    val playerInfo: PlayerInfo,
    val cartoonMediaSourceFactory: CartoonMediaSourceFactory,
    val start: Long,
    val end: Long,
    val crop: Crop,
    // 1 -> gif 2 -> mp4
    val type: Int,
    val onDismissRequest: () -> Unit,
): Transformer.Listener {

    private val outputInnerFolder = File(ctx.getFilePath("recorded"))

    private val singleDispatcher = CoroutineProvider.CUSTOM_SINGLE
    private val singleScope = CoroutineScope(SupervisorJob() + singleDispatcher)
    private val scope = CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE)

    var fps = mutableIntStateOf(if (type == 1) 15 else 30)
    var quality = mutableIntStateOf(if (type == 1) 30 else 100)

    var isDoing = mutableStateOf(false)
    var status = mutableStateOf("")

    fun start(){
        if(type == 2){
            isDoing.value = true
            exportMp4(fps.value, quality.value)
        }
    }

    @OptIn(UnstableApi::class)
    private fun exportMp4(
        fps: Int,
        quality: Int,
    ){
        "$start $end".logi("CartoonRecordedTaskModel")
        val inputMediaItem = cartoonMediaSourceFactory.getMediaItem(playerInfo).buildUpon()
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    // 剪辑
                    .setStartPositionMs(start)
                    .setEndPositionMs(end)
                    .setStartsAtKeyFrame(false)
                    .build())
            .build()
        val editedMediaItem =
            EditedMediaItem.Builder(inputMediaItem)
                .setRemoveAudio(false)
                .setEffects(
                    Effects(
                        listOf(),
                        listOf(
                            // 码率，先丢帧能减轻后续处理的压力
                            FrameDropEffect.createDefaultFrameDropEffect(fps.toFloat()),
                            // 裁剪
                            crop,
                            // 压制
                            ScaleAndRotateTransformation.Builder().setScale(quality/100f, quality/100f).build()
                        )
                    ),

                ).build()
        val transformer = Transformer.Builder(ctx)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAssetLoaderFactory(ExoPlayerAssetLoader.Factory(
                ctx, DefaultDecoderFactory(ctx), false, Clock.DEFAULT, cartoonMediaSourceFactory.getClipMediaSourceFactory(playerInfo, inputMediaItem.clippingConfiguration)
            ))
            // .setMuxerFactory(InAppMuxer.Factory.Builder().build())
            .addListener(this)
            .build()
        val file = File(outputInnerFolder, "output.mp4")
        file.delete()
        transformer.start(editedMediaItem, file.absolutePath)

    }

    @UnstableApi
    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
        super.onCompleted(composition, exportResult)
        onDismissRequest()
    }

    @UnstableApi
    override fun onError(
        composition: Composition,
        exportResult: ExportResult,
        exportException: ExportException
    ) {
        super.onError(composition, exportResult, exportException)
        exportException.printStackTrace()
        onDismissRequest()
    }

    @UnstableApi
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
        onDismissRequest()
    }




}