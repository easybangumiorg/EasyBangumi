package com.heyanle.easybangumi4.plugin.extension.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
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
class ExtensionPushController(
    private val context: Context,
    private val extensionController: ExtensionController
) {

    interface ExtensionPushTaskContainer {
        fun dispatchLoadingMsg(msg: String)

        fun dispatchCompletely(msg: String)

        /**
         * 调用后将会主动 cancel job
         */
        fun dispatchError(msg: String)
    }

    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val container = object : ExtensionPushTaskContainer {
        override fun dispatchLoadingMsg(msg: String) {
            this@ExtensionPushController.dispatchLoadingMsg(msg)
        }

        override fun dispatchError(msg: String) {
            this@ExtensionPushController.dispatchError(msg)
        }

        override fun dispatchCompletely(msg: String) {
            this@ExtensionPushController.dispatchCompletely(msg)
        }
    }

    private val pushFromFileUrl = PushFromFileUrl(context, extensionController)
    private val pushFromCode = PushFromCode(context, extensionController)
    private val pushFromRepo = PushFromRepo(context, extensionController)
    private val taskMap = mapOf(
        pushFromFileUrl.identify() to pushFromFileUrl,
        pushFromCode.identify() to pushFromCode,
        pushFromRepo.identify() to pushFromRepo,
    )

    data class State (
        val isDoing: Boolean = false,
        val isError: Boolean = false,
        val isCompletely: Boolean = false,
        val currentJob: Job? = null,
        val currentParam: ExtensionPushTask.Param? = null,
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


    fun push(param: ExtensionPushTask.Param){
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

    private suspend fun innerInvoke(param: ExtensionPushTask.Param) {

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