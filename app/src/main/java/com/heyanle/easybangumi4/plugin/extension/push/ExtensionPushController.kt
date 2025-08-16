package com.heyanle.easybangumi4.plugin.extension.push

import android.content.Context
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.IExtensionController
import com.heyanle.easybangumi4.setting.SettingMMKVPreferences
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
class ExtensionPushController(
    private val context: Context,
    private val extensionController: IExtensionController,
    private val mmkvSettingMMKVPreferences: SettingMMKVPreferences,
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

    private val cacheFolder = context.getCachePath("extension_js_push")

    private val pushFromFileUrl = PushFromFileUrl(cacheFolder, extensionController)
    private val pushFromCode = PushFromCode(cacheFolder, extensionController)
    private val taskMap: Map<String, ExtensionPushTask> by lazy {
        val map = mutableMapOf<String, ExtensionPushTask>()
        map[pushFromFileUrl.identify()] = pushFromFileUrl
        map[pushFromCode.identify()] = pushFromCode

        if (!mmkvSettingMMKVPreferences.extensionV2Temp && extensionController is ExtensionController) {
            val pushFromRepo = PushFromRepo(
                cacheFolder,
                extensionController as ExtensionController
            )
            map[pushFromRepo.identify()] = pushFromRepo
        }
        map
    }

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