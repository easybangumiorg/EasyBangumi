package com.heyanle.easybangumi4.plugin.source.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.utils.getCachePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 添加 JS 拓展任务管理
 * Created by heyanlin on 2024/10/29.
 */
class SourcePushController(
    private val context: Context,
    private val sourceController: SourceController,
) {

    interface SourcePushTaskContainer {
        fun dispatchLoadingMsg(msg: String)

        fun dispatchCompletely(msg: String)

        /**
         * 调用后将会主动 cancel job
         */
        fun dispatchError(msg: String)
    }

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val container = object : SourcePushTaskContainer {
        override fun dispatchLoadingMsg(msg: String) {
            this@SourcePushController.dispatchLoadingMsg(msg)
        }

        override fun dispatchError(msg: String) {
            this@SourcePushController.dispatchError(msg)
        }

        override fun dispatchCompletely(msg: String) {
            this@SourcePushController.dispatchCompletely(msg)
        }
    }

    private val cacheFolder = context.getCachePath("source_v3_push")

    private val pushFromFileUrl = PushFromFileUrl(cacheFolder, sourceController)
    private val pushFromCode = PushFromCode(cacheFolder, sourceController)
    private val taskMap: Map<String, SourcePushTask> by lazy {
        val map = mutableMapOf<String, SourcePushTask>()
        map[pushFromFileUrl.identify()] = pushFromFileUrl
        map[pushFromCode.identify()] = pushFromCode
        map
    }

    data class State (
        val isDoing: Boolean = false,
        val isError: Boolean = false,
        val isCompletely: Boolean = false,
        val currentJob: Job? = null,
        val currentParam: SourcePushTask.Param? = null,
        val loadingMsg: String = "",
        val errorMsg: String = "",
        val completelyMsg: String = "",
    )
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun cleanErrorOrCompletely() {
        _state.update {
            it.currentJob?.cancel()
            it.copy(
                isDoing = false,
                isError = false,
                isCompletely = false,
                currentJob = null,
            )
        }
    }


    fun push(param: SourcePushTask.Param){
        val job = scope.launch { innerInvoke(param) }
        while (true) {
            val current = _state.value
            val n = current.copy(
                isDoing = true,
                isError = false,
                isCompletely = false,
                currentJob = job,
                currentParam = param
            )
            if (_state.compareAndSet(current, n)) {
                current.currentJob?.cancel()
                break
            }
        }
    }

    private suspend fun innerInvoke(param: SourcePushTask.Param) {
        val task = taskMap[param.identify] ?: return
        scope.launch {
            task.invoke(this, param, container)
        }
    }

    fun cancelCurrent(){
        _state.update {
            it.currentJob?.cancel()
            it.copy(
                isDoing = false,
                isError = false,
                isCompletely = false,
                currentJob = null,
            )
        }
    }



    private fun dispatchLoadingMsg(msg: String){
        _state.update {
            it.copy(
                isDoing = true,
                isError = false,
                isCompletely = false,
                loadingMsg = msg
            )
        }
    }

    private fun dispatchError(msg: String) {
        _state.update {
            it.currentJob?.cancel()
            it.copy(
                isDoing = false,
                isError = true,
                isCompletely = false,
                errorMsg = msg
            )
        }
    }

    private fun dispatchCompletely(msg: String) {
        _state.update {
            it.currentJob?.cancel()
            it.copy(
                isDoing = false,
                isError = false,
                isCompletely = true,
                completelyMsg = msg
            )
        }
    }

}


