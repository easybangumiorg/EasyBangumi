package com.heyanle.easybangumi4.cartoon.story.download.step

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.exoplayer.offline.DownloadManager
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntimeFactory
import com.heyanle.easybangumi4.exo.download.ExoDownloadController
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.inject.core.Inject
import java.lang.Exception
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */

@UnstableApi
object DownloadStep : BaseStep, DownloadManager.Listener {

    const val NAME = "download"

    private val exoDownloadController: ExoDownloadController by Inject.injectLazy()
    private val downloadIndex: DownloadIndex by lazy {
        exoDownloadController.downloadManager.downloadIndex
    }

    override fun canRestart(req: CartoonDownloadReq): Boolean {
        val download = downloadIndex.getDownload(req.uuid)
        return download != null && (
                download.state == Download.STATE_DOWNLOADING ||
                        download.state == Download.STATE_QUEUED ||
                        download.state == Download.STATE_COMPLETED
                )
    }

    @UnstableApi
    override fun invoke() {
        val runtime = CartoonDownloadRuntimeFactory.runtimeLocal.get()
            ?: throw IllegalStateException("runtime is null")
        "${runtime.req.toEpisodeTitle} invoke".logi("DownloadStep")

        var download = downloadIndex.getDownload(runtime.req.uuid)

        if (download == null) {

            runtime.state = 1
            runtime.dispatchToBus(
                -1f,
                "新建下载任务中",
            )

            val initCountDownLatch: CountDownLatch = CountDownLatch(1)
            val playerInfo = runtime.playerInfo ?: throw IllegalStateException("playerInfo is null")

            exoDownloadController.newDownloadTask(
                runtime.req.uuid,
                playerInfo
            ) { req, e ->
                if (req == null) {
                    runtime.error(e, "新建下载任务失败")
                    initCountDownLatch.countDown()
                } else {
                    runtime.downloadRequest = req
                    initCountDownLatch.countDown()
                }
            }

            initCountDownLatch.await()
        } else {
            runtime.downloadRequest = download.request
        }


        if (runtime.needCancel()) {
            cancel(runtime)
            return
        }
        val countDownLatch = CountDownLatch(1)
        runtime.countDownLatch = countDownLatch
        while (countDownLatch.count > 0) {
            download = downloadIndex.getDownload(runtime.req.uuid)
                ?: throw IllegalStateException("download is null")
            when (download.state) {
                Download.STATE_QUEUED -> {
                    val isPause = download.stopReason != Download.STOP_REASON_NONE
                    runtime.dispatchToBus(
                        -1f,
                        if (isPause) "暂停中" else "等待中",
                    )
                }

                Download.STATE_DOWNLOADING -> {

                    var speed = ""
                    val current = System.currentTimeMillis()
                    val currentSize = download.bytesDownloaded
                    if (runtime.lastDownloadTime > 0L) {
                        val time = current - runtime.lastDownloadTime
                        val size = currentSize - runtime.lastDownloadSize
                        speed = getSpeedString(size * 1000 / time)
                    }

                    runtime.lastDownloadSize = currentSize
                    runtime.lastDownloadTime = current
                    runtime.dispatchToBus(
                        download.percentDownloaded.toFloat(),
                        "下载中",
                        speed
                    )
                }

                Download.STATE_COMPLETED -> {
                    runtime.dispatchToBus(
                        1f,
                        "下载完成",
                    )
                    countDownLatch.countDown()
                }

                Download.STATE_REMOVING -> {
                    runtime.error(null, "移动中")
                    countDownLatch.countDown()
                }

                Download.STATE_RESTARTING -> {
                    runtime.dispatchToBus(
                        download.percentDownloaded.toFloat(),
                        "重新下载中",
                        ""
                    )
                }

                Download.STATE_STOPPED -> {
                    runtime.dispatchToBus(
                        download.percentDownloaded.toFloat(),
                        "已停止",
                        ""
                    )
                }

                Download.STATE_FAILED -> {
                    runtime.error(null, "下载失败")
                    countDownLatch.countDown()
                }


            }
            // 每秒刷新一次进度
            countDownLatch.await(1, TimeUnit.SECONDS)
        }

        if (download?.state == Download.STATE_COMPLETED) {
            runtime.stepCompletely()
        }

    }

    @UnstableApi
    override fun cancel(runtime: CartoonDownloadRuntime) {
        //runtime.cancel()
        runtime.countDownLatch?.countDown()
        exoDownloadController.downloadManager.removeDownload(runtime.req.uuid)
    }

    @UnstableApi
    override fun tryToggle(runtime: CartoonDownloadRuntime): Boolean {
        val download = downloadIndex.getDownload(runtime.req.uuid) ?: return false
        if (download.state == Download.STATE_DOWNLOADING) {
            exoDownloadController.downloadManager.setStopReason(
                runtime.req.uuid,
                1
            )

        } else if (download.state == Download.STATE_QUEUED ) {
            exoDownloadController.downloadManager.setStopReason(
                runtime.req.uuid,
                Download.STOP_REASON_NONE
            )
            return true
        }
        return super.tryToggle(runtime)
    }


    private fun getSpeedString(speed: Long): String {
        return when {
            speed < 1024 -> "$speed B/s"
            speed < 1024 * 1024 -> "${speed / 1024} KB/s"
            else -> "${speed / 1024 / 1024} MB/s"
        }
    }

    override fun onDownloadChanged(
        downloadManager: DownloadManager,
        download: Download,
        finalException: Exception?
    ) {
        super.onDownloadChanged(downloadManager, download, finalException)
    }

    override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
        super.onDownloadRemoved(downloadManager, download)
    }
}