package com.heyanle.easybangumi4.cartoon.story.download.action

import androidx.annotation.OptIn
import androidx.core.text.util.LocalePreferences
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
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by heyanle on 2024/8/3.
 * https://github.com/heyanLE
 */
@OptIn(UnstableApi::class)
class TransformerAction(
    private val cartoonDownloadPreference: CartoonDownloadPreference,
    private val mediaSourceFactory: CartoonMediaSourceFactory
) : BaseAction {

    companion object {
        const val NAME = "TransformerAction"
    }

    private val cacheFolder = File(APP.getCachePath("transformer"))
    private val mainScope = MainScope()

    private val dispatchScope = CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE)
    private val executor = ThreadPoolExecutor(
        0, cartoonDownloadPreference.transformMaxCountPref.get().toInt(),
        10L, TimeUnit.SECONDS,
        SynchronousQueue(),
    )
    private val taskList = ArrayDeque<CartoonDownloadRuntime>()

    inner class TransformerRunnable(
        private val runtime: CartoonDownloadRuntime
    ) : Runnable {
        override fun run() {
            if (runtime.isCanceled() || runtime.isError()) {
                tryDispatch()
                return
            }
            synchronized(runtime.lock) {
                innerInvoke(runtime)
            }
            tryDispatch()
        }
    }

    override suspend fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean {
        // 文件最终是改名，只要存在就一定已完成
        val realTarget = File(cacheFolder, "${cartoonDownloadReq.uuid}.mp4")
        return realTarget.exists() && realTarget.isFile && realTarget.canRead() && realTarget.length() > 0
    }

    override suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean {
        return false
    }

    override fun push(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        val realTarget = File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.mp4")
        if ( realTarget.exists() && realTarget.isFile && realTarget.canRead() && realTarget.length() > 0) {
            cartoonDownloadRuntime.filePathBeforeCopy = realTarget.absolutePath
            cartoonDownloadRuntime.stepCompletely(this)
            return
        }

        mainScope.launch {
            cartoonDownloadRuntime.dispatchToBus(
                -1f,
                stringRes(com.heyanle.easy_i18n.R.string.waiting_transformer)
            )
        }
        dispatchScope.launch {
            taskList.add(cartoonDownloadRuntime)
            tryDispatch()
        }
    }

    private fun tryDispatch() {
        dispatchScope.launch {
            if (taskList.isEmpty()) {
                return@launch
            }
            val runtime = taskList.firstOrNull() ?: return@launch
            val runnable = TransformerRunnable(runtime)
            runtime.transformRunnable = runnable
            try {
                executor.execute(runnable)
                taskList.removeFirstOrNull()
            } catch (e: RejectedExecutionException) {
                e.printStackTrace()
            }
        }

    }


    private fun innerInvoke(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        try {
            val initLatch = CountDownLatch(1)
            val completelyLatch = CountDownLatch(1)
            cartoonDownloadRuntime.transformerInitLatch = initLatch
            cartoonDownloadRuntime.transformerCompletelyLatch = completelyLatch

            val playerInfo = cartoonDownloadRuntime.playerInfo ?: throw IllegalStateException("playerInfo is null")
            val mediaItem = mediaSourceFactory.getMediaItem(playerInfo)
            val sourceFactory = mediaSourceFactory.getMediaSourceFactory(playerInfo)
            val encodeType = cartoonDownloadPreference.downloadEncode.get()

            val transformer = Transformer.Builder(APP)
                .setVideoMimeType(
                    if (encodeType == CartoonDownloadPreference.DownloadEncode.H264) MimeTypes.VIDEO_H264
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
                        cartoonDownloadRuntime.exportResult = exportResult
                        cartoonDownloadRuntime.exportException = null
                        cartoonDownloadRuntime.transformerCompletelyLatch?.countDown()
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        super.onError(composition, exportResult, exportException)
                        cartoonDownloadRuntime.exportResult = exportResult
                        cartoonDownloadRuntime.exportException = exportException
                        cartoonDownloadRuntime.transformerCompletelyLatch?.countDown()
                    }
                })
                .build()
            cartoonDownloadRuntime.transformer = transformer


            val realTarget = File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.mp4")
            cacheFolder.mkdirs()
            realTarget.delete()
            realTarget.createNewFile()

            cartoonDownloadRuntime.transformerFile = realTarget

            val holder: ProgressHolder = ProgressHolder()
            mainScope.launch {
                cartoonDownloadRuntime.dispatchToBus(
                    -1f,
                    stringRes(com.heyanle.easy_i18n.R.string.waiting_transformer)
                )
                transformer.start(
                    mediaItem,
                    realTarget.absolutePath
                )
                initLatch.countDown()
            }

            initLatch.await()
            while (completelyLatch.count > 0) {
                if (cartoonDownloadRuntime.isCanceled()) {
                    mainScope.launch {
                        transformer.cancel()
                    }
                    completelyLatch.countDown()
                    return
                }
                mainScope.launch {
                    val proState = transformer.getProgress(holder)
                    val progress = if (proState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        holder.progress
                    } else {
                        -1
                    }
                    cartoonDownloadRuntime.dispatchToBus(
                        progress.toFloat() / 100f,
                        stringRes(com.heyanle.easy_i18n.R.string.downloading),
                        if(progress >= 0) "${progress.toInt()}%" else ""
                    )
                }

                completelyLatch.await(1, TimeUnit.SECONDS)
            }

            if (cartoonDownloadRuntime.exportException != null) {
                mainScope.launch {
                    transformer.cancel()
                }
                cartoonDownloadRuntime.error(
                    cartoonDownloadRuntime.exportException,
                    cartoonDownloadRuntime.exportException?.message
                )
            } else {
                cartoonDownloadRuntime.filePathBeforeCopy = realTarget.absolutePath
                cartoonDownloadRuntime.stepCompletely(this)
            }

        } catch (e: Throwable) {
            cartoonDownloadRuntime.error(
                e,
                e.message
            )
        }
    }

    override fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime) {
        cartoonDownloadRuntime.transformRunnable?.let {
            executor.remove(it)
        }
        dispatchScope.launch {
            taskList.remove(cartoonDownloadRuntime)
        }
        mainScope.launch {
            cartoonDownloadRuntime.transformer?.cancel()
        }
    }


}