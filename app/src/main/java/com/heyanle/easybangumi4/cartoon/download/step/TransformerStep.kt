package com.heyanle.easybangumi4.cartoon.download.step

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
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 使用 Transformer 一边下载一边转码
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
object TransformerStep : BaseStep {

    const val NAME = "transformer"

    @OptIn(UnstableApi::class)
    override fun invoke() {
        val runtime = CartoonDownloadRuntimeFactory.runtimeLocal.get()
            ?: throw IllegalStateException("runtime is null")
        runtime.state = 1
        runtime.dispatchToBus(
            -1f,
            "开始下载中",
        )
        val countDownLatch: CountDownLatch = CountDownLatch(1)
        runtime.countDownLatch = countDownLatch
        val playerInfo = runtime.playerInfo ?: throw IllegalStateException("playerInfo is null")
        val mediaSourceFactory: CartoonMediaSourceFactory = Inject.get()
        val transformer = Transformer.Builder(APP)
            .setVideoMimeType(MimeTypes.VIDEO_H265)
            .setAssetLoaderFactory(
                ExoPlayerAssetLoader.Factory(
                    APP,
                    DefaultDecoderFactory.Builder(APP).build(),
                    Clock.DEFAULT,
                    mediaSourceFactory.getMediaSourceFactory(playerInfo)
                )
            )
            .setMuxerFactory(InAppMuxer.Factory.Builder().build())
            .addListener(object: Transformer.Listener {
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
                    runtime.exportResult = exportResult
                    runtime.exportException = exportException
                    countDownLatch.countDown()
                }
            })
            .build()
        runtime.transformer = transformer

        val folder = File(APP.getCachePath("transformer"))
        folder.mkdirs()
        val t = File(folder, "${runtime.req.uuid}.mp4")
        t.delete()
        val target = UniFile.fromFile(t)?: throw IllegalStateException("cache file uri is null")
        transformer.start(
            mediaSourceFactory.getMediaItem(playerInfo),
            target.uri.toString()
        )
        runtime.cacheFolderUri = folder.toUri().toString()
        runtime.cacheDisplayName = t.name

        val holder: ProgressHolder = ProgressHolder()


        while (countDownLatch.count > 0){
            if (runtime.needCancel()) {
                transformer.cancel()
                return
            }
            runtime.transformerProgress = transformer.getProgress(holder)
            runtime.dispatchToBus(
                runtime.transformerProgress.toFloat() / 100f,
                stringRes(com.heyanle.easy_i18n.R.string.downloading)
            )
            // 每秒刷新一次进度
            countDownLatch.await(1, TimeUnit.SECONDS)
        }
        if (runtime.needCancel()) {
            transformer.cancel()
            return
        }
        runtime.stepCompletely()
    }

    @OptIn(UnstableApi::class)
    override fun cancel(runtime: CartoonDownloadRuntime) {
        runtime.transformer?.cancel()
        runtime.countDownLatch?.countDown()
    }
}