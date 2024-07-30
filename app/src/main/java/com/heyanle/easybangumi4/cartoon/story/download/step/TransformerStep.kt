package com.heyanle.easybangumi4.cartoon.story.download.step

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMuxer
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.Transformer.PROGRESS_STATE_AVAILABLE
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 使用 Transformer 一边下载一边转码
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
object TransformerStep : BaseStep {

    private val scope = MainScope()

    const val NAME = "transformer"

    @OptIn(UnstableApi::class)
    override fun invoke() {
        val runtime = CartoonDownloadRuntimeFactory.runtimeLocal.get()
            ?: throw IllegalStateException("runtime is null")
        runtime.state = 1
        runtime.dispatchToBus(
            -1f,
            "开始转码中",
        )
        val countDownLatch: CountDownLatch = CountDownLatch(1)
        runtime.countDownLatch = countDownLatch

        val playerInfo = runtime.playerInfo
        val req = runtime.downloadRequest

        val mediaSourceFactory: CartoonMediaSourceFactory = Inject.get()

        // 有点脏，先这样，后续看怎么优化
        val mediaItem =
            req?.toMediaItem()
                ?: if (playerInfo != null) mediaSourceFactory.getMediaItem(
                    playerInfo
                ) else throw IllegalStateException("req and playerInfo is null")
        val sourceFactory =
            if (playerInfo != null) mediaSourceFactory.getMediaSourceFactoryWithDownload(playerInfo)
            else if (req != null) {
                val info = PlayerInfo(
                    uri = req.uri.toString(),
                    decodeType = req.data[0].toInt(),
                )
                mediaSourceFactory.getMediaSourceFactoryWithDownload(info)
            } else throw IllegalStateException("req and playerInfo is null")


        val transformer = Transformer.Builder(APP)
            .setVideoMimeType(
                if (runtime.decodeType == CartoonDownloadPreference.DownloadEncode.H264) MimeTypes.VIDEO_H264
                else MimeTypes.VIDEO_H265
            )
            .setAssetLoaderFactory(
                ExoPlayerAssetLoader.Factory(
                    APP,
                    DefaultDecoderFactory.Builder(APP).build(),
                    Clock.DEFAULT,
                    sourceFactory
                )
            )
            .setMuxerFactory(InAppMuxer.Factory.Builder().build())
            .setMaxDelayBetweenMuxerSamplesMs(500000)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    super.onCompleted(composition, exportResult)
                    runtime.exportResult = exportResult
                    runtime.exportException = null
                    countDownLatch.countDown()
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    super.onError(composition, exportResult, exportException)
                    runtime.error(exportException, exportException.message)
                    exportException.printStackTrace()
                    countDownLatch.countDown()
                }
            })
            .build()
        runtime.transformer = transformer

        val folder = File(APP.getCachePath("transformer"))
        folder.mkdirs()
        val t = File(folder, "${runtime.req.uuid}.mp4")
        t.delete()
        t.createNewFile()
        t.deleteOnExit()

        runtime.cacheFolderUri = folder.toUri().toString()
        runtime.cacheDisplayName = t.name

        val holder: ProgressHolder = ProgressHolder()
        scope.launch {
            transformer.start(
                mediaItem,
                t.absolutePath,
            )
        }

        while (countDownLatch.count > 0) {

            if (runtime.needCancel()) {
                scope.launch {
                    transformer.cancel()
                }
                runtime.error(runtime.exportException, runtime.exportException?.message)
                return
            }
            scope.launch {
                val proState = transformer.getProgress(holder)
                if (proState == PROGRESS_STATE_AVAILABLE) {
                    runtime.transformerProgress = holder.progress
                } else {
                    runtime.transformerProgress = -1
                }
                runtime.transformerProgress.logi("TransformerStep")
                runtime.dispatchToBus(
                    runtime.transformerProgress.toFloat() / 100f,
                    "转码中",
                    subStatus = if (runtime.transformerProgress < 0) "" else "${holder.progress}%"
                )
            }
            // 每秒刷新一次进度
            countDownLatch.await(1, TimeUnit.SECONDS)
        }
        scope.launch {
            try {
                transformer.cancel()
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
        if (runtime.needCancel()) {
            runtime.error(runtime.exportException, runtime.exportException?.message)
            return
        }

        runtime.stepCompletely()
    }

    @OptIn(UnstableApi::class)
    override fun cancel(runtime: CartoonDownloadRuntime) {
        scope.launch {
            runtime.transformer?.cancel()
            runtime.countDownLatch?.countDown()
        }

    }

    private fun getSpeedString(speed: Long): String {
        return when {
            speed < 1024 -> "$speed B/s"
            speed < 1024 * 1024 -> "${speed / 1024} KB/s"
            else -> "${speed / 1024 / 1024} MB/s"
        }
    }
}