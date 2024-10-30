package com.heyanle.easybangumi4.plugin.extension.push

import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 添加 JS 拓展任务管理
 * Created by heyanlin on 2024/10/29.
 */
class ExtensionPushController(
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
    }

    data class State (
        val isDoing: Boolean = false,
        val isError: Boolean = false,
        val isCompletely: Boolean = false,
        val currentJob: Job? = null,
        val loadingMsg: String = "",
        val errorMsg: String = "",
        val completelyMsg: String = "",
    )
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun cleanError() {
        _state.update {
            it.currentJob?.cancel()
            it.copy(
                isDoing = false,
                isError = false,
                currentJob = null,
                loadingMsg = ""
            )
        }
    }

    private suspend fun startPushFromFileUrl(
        input: String,
    ): Job {

    }

    private suspend fun startPushFromCode(
        fileName: String,
        code: String,
    ): Job {

    }

    private suspend fun startPushFromRepo(
        repoUrl: String,
    ): Job {

    }

    private fun dispatchLoadingMsg(msg: String){
        _state.update {
            it.copy(
                isDoing = true,
                isError = false,
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
                errorMsg = msg
            )
        }
    }

}