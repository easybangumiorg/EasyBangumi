package com.heyanle.easy_transformer.transform

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.UiThread
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.transformer.AssetLoader
import androidx.media3.transformer.Composition.HDR_MODE_KEEP_HDR
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.SampleConsumer
import androidx.media3.transformer.TransformerUtil
import com.alien.gpuimage.filter.HookFilter
import com.alien.gpuimage.outputs.Output
import com.alien.gpuimage.sources.OesTexturePipeline

/**
 * 将 ExoPlayer 可用的 MediaSource 转换为 gpuImage 可用的输入源
 * 不支持音轨控制，默认去除音量
 * Created by heyanle on 2024/6/29.
 * https://github.com/heyanLE
 */
@UnstableApi
class EasyTransform(
    private val ctx: Context,
    private val mediaItem: MediaItem,
    private val mediaSourceFactory: MediaSource.Factory,
    private val output: Output
) : AssetLoader.Listener, HookFilter.OnFrameCompletelyListener, PixelCopyConsumer.ConsumerListener  {

    interface OnTransformListener{
        fun onTransformCompletely()
        fun onTransformFailed(exportException: ExportException)
    }

    @Volatile
    private var pendingCount = 0

    var listener: OnTransformListener? = null
    private val hookFilter = HookFilter().apply {
        addTarget(output)
        onFrameCompletely = this@EasyTransform
    }
    private val oesTexturePipeline = OesTexturePipeline().apply {
        addTarget(hookFilter)
    }
    private val pixelCopyConsumer: PixelCopyConsumer by lazy {
        PixelCopyConsumer(oesTexturePipeline.getSurface(), handler).apply {
            listener = this@EasyTransform
        }
    }
    private val thread: HandlerThread by lazy {
        HandlerThread("EasyTransform")
    }
    private val handler by lazy {
        Handler(thread.looper)
    }

    private val assetLoader: AssetLoader by lazy {
        ExoPlayerAssetLoader.Factory(
            ctx,
            DefaultDecoderFactory.Builder(ctx).build(),
            Clock.DEFAULT,
            mediaSourceFactory
        ).createAssetLoader(
            EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(true)
                .build(),
            thread.looper,
            this,
            AssetLoader.CompositionSettings(
                HDR_MODE_KEEP_HDR,
                false,
            )
        )
    }

    fun start() {
        thread.start()
        handler.post {
            assetLoader.start()
        }
    }

    fun getProcess(holder: ProgressHolder): Int {
        return assetLoader.getProgress(holder)
    }


    override fun onDurationUs(durationUs: Long) {

    }

    override fun onTrackCount(trackCount: Int) {}

    override fun onTrackAdded(inputFormat: Format, supportedOutputTypes: Int): Boolean {
        val assetLoaderCanOutputDecoded =
            supportedOutputTypes and AssetLoader.SUPPORTED_OUTPUT_TYPE_DECODED != 0
        if (!assetLoaderCanOutputDecoded) {
            throw IllegalArgumentException()
        }
        return true
    }

    override fun onOutputFormat(format: Format): SampleConsumer {
        oesTexturePipeline.setFormat(format.width, format.height, 0)
        return pixelCopyConsumer
    }

    override fun onError(exportException: ExportException) {
        listener?.onTransformFailed(exportException)
        exportException.printStackTrace()
        assetLoader.release()
        thread.quit()
    }


    @UiThread
    fun cancel() {
        handler.post {
            assetLoader.release()
            thread.quit()
        }
    }

    override fun onFrameStart(time: Long, textureIndex: Int) {
        Log.i("EasyTransform", "onFrameStart: $pendingCount ")
        pendingCount ++
        pixelCopyConsumer.releaseOutput(time)
    }

    override fun onFrameCompletely(time: Long, textureIndex: Int) {
        Log.i("EasyTransform", "onFrameCompletely: $pendingCount $isEnd")
        pendingCount --
        if (isEnd && pendingCount <= 0){
            dispatchCompletely()
        }

    }

    override fun onOutputRelease(presentationTimeUs: Long) {

    }

    @Volatile
    var isEnd = false
    override fun onSignalEnd() {
        Log.i("EasyTransform", "onSignalEnd: $pendingCount ")
        isEnd = true
//        if (pendingCount <= 0){
//            listener?.onTransformCompletely()
//        }
    }

    override fun onLastFrameRelease() {
        Log.i("EasyTransform", "onLastFrameRelease: $pendingCount ")
        if (pendingCount <= 0){
            dispatchCompletely()
        }
    }

    private var isTransformCompletely = false
    private fun dispatchCompletely() {
        if (isTransformCompletely){
            return
        }
        isTransformCompletely = true
        listener?.onTransformCompletely()
    }
}