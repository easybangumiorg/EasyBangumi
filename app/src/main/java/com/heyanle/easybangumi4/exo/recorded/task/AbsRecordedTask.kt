package com.heyanle.easybangumi4.exo.recorded.task

import com.heyanle.easybangumi4.utils.CoroutineProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Created by heyanlin on 2024/6/24.
 */
abstract class AbsRecordedTask : RecordedTask {

    // 协程 ===========================
    protected val dispatcher = Dispatchers.IO
    protected val scope = CoroutineScope(SupervisorJob() + dispatcher)
    protected val mainScope = MainScope()

    // 状态管理 ===========================
    protected val _state = MutableStateFlow(RecordedTask.TaskState())
    override val state = _state.asStateFlow()

    protected fun dispatchError(e: Exception, errorMsg: String?) {
        _state.update {
            it.copy(
                status = 3,
                errorException = e,
                error = errorMsg ?: e.message ?: ""
            )
        }
    }

    protected fun dispatchProcess(process: Int, label: String? = null) {
        _state.update {
            it.copy(
                status = 1,
                process = process,
                statusLabel = label ?: it.statusLabel
            )
        }
    }

    protected fun dispatchCompletely() {
        _state.update {
            it.copy(
                status = 2
            )
        }
    }
}