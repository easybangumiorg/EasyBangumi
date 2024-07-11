package com.heyanle.easybangumi4.cartoon_download.entity

import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.util.concurrent.CountDownLatch

/**
 * 运行时的下载，只服务于运行时
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntime(
    val req: CartoonDownloadReq,
) {

    companion object {

    }

    var runnable: Runnable? = null

    val lock = Object()

    var canCancel = true

    // 所有异步锁收归到这里
    @Volatile
    var countDownLatch: CountDownLatch? = null

    // for parse step
    var parseResult: SourceResult<PlayerInfo>? = null


    // for transformer step
    var transformer: Transformer? = null
    var transformerProgress: Int = 0
    var exportResult: ExportResult? = null
    var exportException: ExportException? = null


    // 0 -> step_waiting
    // 1 -> step_doing
    // 2 -> step_completely
    // 3 -> error
    // 4 -> success
    // 5 -> cancel
    @Volatile
    var state: Int = 0

    @Volatile
    var error: Throwable? = null

    @Volatile
    var errorMsg: String = ""

    @Volatile
    var stepIndex: Int = 0

    @Volatile
    var playerInfo: PlayerInfo? = null

    @Volatile
    var cacheFolderUri: String? = null

    @Volatile
    var cacheDisplayName: String? = null

    @Volatile
    var targetFolderUri: String? = null

    @Volatile
    var targetDisplayName: String? = null

    fun getDownloadInfo(): DownloadingBus.DownloadingInfo {
        val bus: DownloadingBus = Inject.get()
        return bus.getInfo(DownloadingBus.DownloadScene.CARTOON, req.uuid)
    }

    fun dispatchToBus(process: Float, status: String, subStatus: String = ""){
        val info = getDownloadInfo()
        info.process.value = process
        info.status.value = status
        info.subStatus.value = subStatus
    }

    fun error(error: Throwable? = null, errorMsg: String? = null) {
        this.error = error
        this.errorMsg = errorMsg ?: error?.message ?: ""
        this.state = 3
        dispatchStateToBus()
    }

    fun stepCompletely() {
        this.state = 2
        dispatchStateToBus()
    }

    fun dispatchStateToBus() {
        val info = getDownloadInfo()
        when (state) {
            0, 2 -> {
                info.process.value = -1f
                info.status.value = stringRes(com.heyanle.easy_i18n.R.string.waiting)
                info.subStatus.value = ""
            }
            3 -> {
                info.process.value = 0f
                info.status.value = errorMsg
            }
        }
    }

    fun needCancel(): Boolean {
        return state == 5 || state == 3
    }

}