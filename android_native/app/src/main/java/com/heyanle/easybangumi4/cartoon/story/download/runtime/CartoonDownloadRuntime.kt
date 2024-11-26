package com.heyanle.easybangumi4.cartoon.story.download.runtime

import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.arialyy.aria.core.download.M3U8Entity
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.action.BaseAction
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * Created by heyanle on 2024/8/2.
 * https://github.com/heyanLE
 */
class CartoonDownloadRuntime(
    val req: CartoonDownloadReq,
) {

    // state and listener
    interface Listener {
        fun onStateChange(runtime: CartoonDownloadRuntime)
    }
    var listener: Listener? = null

    enum class State {
        WAITING, DOING, STEP_COMPLETELY, ERROR, SUCCESS, CANCEL
    }

    @Volatile
    var state: State = State.WAITING
        set(value) {
            if (field != value) {
                field = value
                listener?.onStateChange(this)
            }

        }

    @Volatile
    var error: Throwable? = null

    @Volatile
    var errorMsg: String = ""

    @Volatile
    var currentAction: BaseAction? = null

    // 该次运行是否为恢复模式，恢复模式下失败一次后会走一遍重新下载罗技
    @Volatile
    var isResume: Boolean = false


    // 运行时数据 ==================================
    // 对以下数据读写都需要获取锁
    val lock = Object()

    var dispatcherRunnable: Runnable? = null

    var syncTaskRunnable: Runnable? = null

    var transformRunnable: Runnable? = null

    var currentStepIndex: Int = 0

    // parse action
    var retryTime: Int = 0
    var parseJob: Job? = null
    var playerInfo: PlayerInfo? = null

    // aria action
    var ariaId: Long = -1L
    var ariaDownloadFilePath: String = ""
    var m3u8Entity: M3U8Entity? = null

    // transform action
    var transformerInitLatch: CountDownLatch? = null
    var transformerCompletelyLatch: CountDownLatch? = null
    var transformer: Transformer? = null

    var transformerFile: File? = null

    // 这俩变量加锁后会死锁，靠上面 transformerCompletelyLatch 控制，可不加锁访问
    @Volatile
    var exportResult: ExportResult? = null
    @Volatile
    var exportException: ExportException? = null

    // transcode action
    var transcodeRunnable: Runnable? = null


    // 解密阶段产物路径
    var decryptCacheFile: File? = null
    var decryptFile: File? = null
    // ffmpeg 转码阶段产物
    var ffmpegCacheFile: File? = null
    var ffmpegFile: File? = null

    // transcode&transform
    var filePathBeforeCopy: String = ""

    val completelyActionList = arrayListOf<BaseAction>()



    // 状态判断 =======================================
    fun needDispatch(): Boolean {
        return state == State.WAITING || state == State.STEP_COMPLETELY
    }
    fun isCanceled(): Boolean {
        return state == State.CANCEL
    }

    fun isError(): Boolean {
        return state == State.ERROR
    }

    fun isSuccess(): Boolean {
        return state == State.SUCCESS
    }


    // 状态分发

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

    fun dispatchStateToBus() {
        val info = getDownloadInfo()
        when (state) {
            State.WAITING, State.STEP_COMPLETELY -> {
                info.process.value = -1f
                info.status.value = stringRes(R.string.waiting)
                info.subStatus.value = ""
            }
            State.ERROR -> {
                info.process.value = 0f
                info.status.value = errorMsg
            }
            else -> { }
        }
    }
    fun error(error: Throwable? = null, errorMsg: String? = null){
        this.error = error
        this.errorMsg = errorMsg ?: error?.message ?: "Unknown error"
        state = State.ERROR
        dispatchStateToBus()
    }

    fun stepCompletely(
        action: BaseAction
    ) {
        completelyActionList.add(action)
        state = State.STEP_COMPLETELY
        dispatchStateToBus()
    }

    fun stepRetry(){
        state = State.WAITING
        dispatchStateToBus()
    }

    fun cancel(){
        currentAction?.onCancel(this)
        state = State.CANCEL
        dispatchStateToBus()
    }

}