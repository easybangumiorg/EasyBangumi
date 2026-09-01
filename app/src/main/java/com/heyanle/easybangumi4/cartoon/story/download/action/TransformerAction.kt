package com.heyanle.easybangumi4.cartoon.story.download.action

import androidx.annotation.OptIn
import android.os.Handler
import android.os.HandlerThread
import androidx.core.text.util.LocalePreferences
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMp4Muxer
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
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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

    // media3 Transformer 的 ExoPlayerAssetLoader 需要一个带 Looper 的线程构建 ExoPlayer。
    // 原实现用 MainScope（主线程）执行 start/cancel，转码期间会占用主线程；改为独立 HandlerThread。
    private val transformerThread = HandlerThread("TransformerMain").apply { start() }
    private val transformerScope = CoroutineScope(SupervisorJob() + Handler(transformerThread.looper).asCoroutineDispatcher())

    private val dispatchScope = CoroutineScope(SupervisorJob() + CoroutineProvider.SINGLE)
    private val executor = ThreadPoolExecutor(
        0, cartoonDownloadPreference.transformMaxCountPref.get().toInt().coerceAtLeast(1),
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

            val realTarget = File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.mp4")
            val tempTarget = File(cacheFolder, "${cartoonDownloadRuntime.req.uuid}.temp.mp4")
            cacheFolder.mkdirs()
            realTarget.delete()
            tempTarget.delete()

            cartoonDownloadRuntime.transformerFile = tempTarget
            cartoonDownloadRuntime.transformerStartError = null

            // Transformer confines all public calls to the application Looper used at creation.
            // Calling start/getProgress/cancel from different worker threads throws
            // "Transformer is accessed on the wrong thread" on Media3 1.9+.
            val progress = AtomicInteger(-1)
            transformerScope.launch {
                cartoonDownloadRuntime.dispatchToBus(
                    -1f,
                    stringRes(com.heyanle.easy_i18n.R.string.waiting_transformer)
                )
                try {
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
                                sourceFactory,
                            )
                        )
                        .setEncoderFactory(
                            DefaultEncoderFactory.Builder(APP)
                                .setEnableFallback(true)
                                .build(),
                        )
                        .setMuxerFactory(InAppMp4Muxer.Factory())
                        .setMaxDelayBetweenMuxerSamplesMs(500000)
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                cartoonDownloadRuntime.exportResult = exportResult
                                cartoonDownloadRuntime.exportException = null
                                completelyLatch.countDown()
                            }

                            override fun onError(
                                composition: Composition,
                                exportResult: ExportResult,
                                exportException: ExportException,
                            ) {
                                cartoonDownloadRuntime.exportResult = exportResult
                                cartoonDownloadRuntime.exportException = exportException
                                completelyLatch.countDown()
                            }
                        })
                        .build()
                    cartoonDownloadRuntime.transformer = transformer
                    transformer.start(
                        mediaItem,
                        tempTarget.absolutePath
                    )
                } catch (error: Throwable) {
                    cartoonDownloadRuntime.transformerStartError = error
                    completelyLatch.countDown()
                } finally {
                    // start 抛错时也必须释放工作线程，避免占死完整模式并发槽。
                    initLatch.countDown()
                }
            }

            initLatch.await()
            cartoonDownloadRuntime.transformerStartError?.let { throw it }
            while (completelyLatch.count > 0) {
                if (cartoonDownloadRuntime.isCanceled()) {
                    transformerScope.launch {
                        cartoonDownloadRuntime.transformer?.cancel()
                    }
                    completelyLatch.countDown()
                    return
                }
                transformerScope.launch {
                    val holder = ProgressHolder()
                    val state = cartoonDownloadRuntime.transformer?.getProgress(holder)
                    progress.set(
                        if (state == Transformer.PROGRESS_STATE_AVAILABLE) holder.progress else -1,
                    )
                }
                mainScope.launch {
                    val latestProgress = progress.get()
                    cartoonDownloadRuntime.dispatchToBus(
                        latestProgress.toFloat() / 100f,
                        stringRes(com.heyanle.easy_i18n.R.string.downloading),
                        if (latestProgress >= 0) "$latestProgress%" else "",
                    )
                }

                completelyLatch.await(1, TimeUnit.SECONDS)
            }

            if (cartoonDownloadRuntime.exportException != null) {
                transformerScope.launch {
                    cartoonDownloadRuntime.transformer?.cancel()
                }
                cartoonDownloadRuntime.error(
                    cartoonDownloadRuntime.exportException,
                    cartoonDownloadRuntime.exportException?.message
                )
            } else {
                if (!tempTarget.isFile ||
                    !tempTarget.canRead() ||
                    tempTarget.length() <= 0 ||
                    !tempTarget.renameTo(realTarget)
                ) {
                    throw IllegalStateException("Transformer output finalize failed")
                }
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
        transformerScope.launch {
            try {
                cartoonDownloadRuntime.transformer?.cancel()
            } finally {
                // Transformer 完成取消后再删除输出，避免与 muxer 写入竞争。
                cartoonDownloadRuntime.transformerFile?.delete()
            }
        }
    }


}
