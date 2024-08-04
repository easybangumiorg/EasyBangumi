package com.heyanle.easybangumi4.exo.recorded.task

import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/**
 * Created by heyanlin on 2024/6/24.
 */
abstract class AbsRecordedTask : RecordedTask {

    // 协程 ===========================
    protected val dispatcher = Dispatchers.IO
    protected val singleDispatcher = CoroutineProvider.newSingleDispatcher
    protected val scope = CoroutineScope(SupervisorJob() + dispatcher)
    protected val singleScope = CoroutineScope(SupervisorJob() + singleDispatcher)
    protected val mainScope = MainScope()

    // 状态管理 ===========================
    protected val _state = MutableStateFlow(RecordedTask.TaskState())
    override val state = _state.asStateFlow()

    interface Listener {
        fun onProcess(process: Int, label: String?)
        fun onError(e: Exception, errorMsg: String?)
        fun onCompletely(file: File)
    }
    var listener: Listener? = null

    protected fun dispatchError(e: Exception, errorMsg: String?) {
        _state.update {
            it.copy(
                status = 3,
                errorException = e,
                error = errorMsg ?: e.message ?: ""
            )
        }
        listener?.onError(e, errorMsg)
    }

    protected fun dispatchProcess(process: Int, label: String? = null) {
        _state.update {
            it.copy(
                status = 1,
                process = process,
                statusLabel = label ?: it.statusLabel
            )
        }
        listener?.onProcess(process, label)
    }

    protected fun dispatchCompletely(file: File) {
        _state.update {
            it.copy(
                status = 2
            )
        }
        listener?.onCompletely(
            file
        )
    }

    fun release() {
        runCatching {
            mainScope.cancel()
        }
        runCatching {
            scope.cancel()
        }
        runCatching {
            singleScope.cancel()
        }
    }
}