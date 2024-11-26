package com.heyanle.easybangumi4.cartoon.story.download.runtime

import android.content.Intent
import android.os.Build
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.action.BaseAction
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.story.download.service.DownloadingService
import com.heyanle.easybangumi4.cartoon.story.local.CartoonLocalController
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.scaleHelper
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.api.get
import com.heyanle.inject.api.getOrNull
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 下载任务调度器，服务于运行时
 * Created by heyanle on 2024/8/2.
 * https://github.com/heyanLE
 */
class CartoonDownloadDispatcher(
    private val cartoonDownloadReqController: CartoonDownloadReqController,
    private val cartoonLocalController: CartoonLocalController,
) : CartoonDownloadRuntime.Listener {


    private val mainScope = MainScope()
    private val dispatchExecutor = CoroutineProvider.newSingleExecutor

    // 一些比较轻量级的任务可以直接在这里执行
    private val workerExecutor = CoroutineProvider.newSingleExecutor


    private val _runtimeMap = MutableStateFlow<Map<String, CartoonDownloadRuntime>>(emptyMap())
    val runtimeMap = _runtimeMap.asStateFlow()

    init {
        mainScope.launch {
            _runtimeMap.map {
                it.any { it.value.state == CartoonDownloadRuntime.State.DOING ||
                        it.value.state == CartoonDownloadRuntime.State.WAITING ||
                        it.value.state == CartoonDownloadRuntime.State.STEP_COMPLETELY }
            }.distinctUntilChanged().collectLatest {
                if (it) {
                    try {
                        val intent = Intent(APP, DownloadingService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            APP.startForegroundService(intent)
                        } else {
                            APP.startService(intent)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                } else {
                    try {
                        APP.stopService(Intent(APP, DownloadingService::class.java))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private inner class DispatchRuntime(
        private val runtime: CartoonDownloadRuntime,
    ) : Runnable {
        override fun run() {
            synchronized(runtime.lock) {
                if (
                    runtime.needDispatch() &&
                    !runtime.isCanceled()
                ) {
                    if (runtime.state == CartoonDownloadRuntime.State.STEP_COMPLETELY) {
                        runtime.currentStepIndex++
                    }
                    val index = runtime.currentStepIndex
                    if (index >= runtime.req.stepChain.size) {
                        runtime.state = CartoonDownloadRuntime.State.SUCCESS
                        return
                    }
                    val name = runtime.req.stepChain.getOrNull(index)
                    val action = name?.let {
                        Inject.get<BaseAction>(it)
                    }
                    if (action != null) {

                        runtime.currentAction = action
                        runtime.state = CartoonDownloadRuntime.State.DOING

                        try {
                            if (action.isAsyncAction()) {
                                action.push(runtime)
                            } else {
                                val syncRunnable = Runnable {
                                    synchronized(runtime.lock) {
                                        action.push(runtime)
                                        runtime.lock.notify()
                                    }
                                }
                                runtime.syncTaskRunnable = syncRunnable
                                workerExecutor.execute(syncRunnable)
                            }
                        } catch (e: Throwable) {
                            runtime.error(
                                e,
                                e.message
                            )
                        }

                    } else {
                        runtime.error(
                            errorMsg = "Action not found: $name"
                        )
                    }
                } else {
                    runtime.error(
                        errorMsg = "Can not dispatch"
                    )
                }
                runtime.lock.notify()
            }
        }
    }

    fun newRequest(list: Collection<CartoonDownloadReq>) {
        mainScope.launch {
            val runtimeList = list.map {
                innerNewRequest(it)
            }
            dispatchNewRuntime(runtimeList)
        }
    }

    fun tryResume(reqList: Collection<CartoonDownloadReq>) {
        mainScope.launch {
            val runtimeList = reqList.map {
                innerResume(it)
            }
            dispatchNewRuntime(runtimeList)
        }

    }


    fun remove(reqList: Collection<CartoonDownloadReq>) {
        mainScope.launch {
            innerRemoveTask(reqList)
        }
    }

    fun removeWithItemId(itemId: Collection<String>) {
        mainScope.launch {
            val current = _runtimeMap.value
            val d = itemId.toSet()
            val needRemoveTask = current.filter {
                d.contains(it.value.req.toLocalItemId)
            }.map { it.value.req }
            innerRemoveTask(needRemoveTask)
        }
    }

    private fun innerNewRequest(req: CartoonDownloadReq): CartoonDownloadRuntime {
        val runtime = CartoonDownloadRuntime(req)
        runtime.listener = this
        runtime.dispatcherRunnable = DispatchRuntime(runtime)
        dispatchExecutor.execute(runtime.dispatcherRunnable)
        return runtime
    }

    private fun innerRemoveTask(
        itemList: Collection<CartoonDownloadReq>
    ) {
        val current = _runtimeMap.value
        itemList.forEach { item ->
            current[item.uuid]?.run {
                listener = null
                cancel()
                listener = this@CartoonDownloadDispatcher
                dispatchExecutor.remove(dispatcherRunnable)
                workerExecutor.remove(syncTaskRunnable)
            }
            _runtimeMap.update {
                it.toMutableMap().apply {
                    remove(item.uuid)
                }
            }
        }
    }


    private suspend fun innerResume(req: CartoonDownloadReq): CartoonDownloadRuntime {
        // 倒序检查所有 Action 是否可恢复
        val stepList = req.stepChain.reversed()
        var resumeActionName: String? = null
        for (step in stepList) {
            val action = step?.let {
                Inject.get<BaseAction>(it)
            }
            if (action != null) {
                val canResume = action.canResume(req)
                if (canResume) {
                    resumeActionName = step
                    break
                }
            }
        }
        val newCurrentIndex = req.stepChain.indexOf(resumeActionName)
        // 不可恢复走新建下载任务逻辑
        if (resumeActionName == null || newCurrentIndex !in req.stepChain.indices) {
            return innerNewRequest(req)
        }
        val runtime = CartoonDownloadRuntime(req)
        runtime.dispatcherRunnable = DispatchRuntime(runtime)

        runtime.currentStepIndex = newCurrentIndex
        runtime.isResume = true
        // 这里必须先设置 state，否则会触发回调调度
        runtime.listener = null
        runtime.state = CartoonDownloadRuntime.State.WAITING
        runtime.listener = this@CartoonDownloadDispatcher
        mainScope.launch {
            runtime.dispatchToBus(
                -1f,
                stringRes(com.heyanle.easy_i18n.R.string.resuming_download),
            )
        }
        dispatchExecutor.execute(runtime.dispatcherRunnable)
        return runtime
    }

    private fun dispatchNewRuntime(itemList: List<CartoonDownloadRuntime>) {
        _runtimeMap.update {
            it.toMutableMap().apply {
                itemList.forEach {
                    get(it.req.uuid)?.let {
                        it.cancel()
                        dispatchExecutor.remove(it.dispatcherRunnable)
                    }
                    put(it.req.uuid, it)
                }
            }
        }
    }


    override fun onStateChange(runtime: CartoonDownloadRuntime) {
        val runnable = runtime.dispatcherRunnable
        when (runtime.state) {
            CartoonDownloadRuntime.State.SUCCESS -> {
                runtime.completelyActionList.reversed().forEach {
                    try {
                        it.onTaskCompletely(runtime)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }

                }
                innerRemoveTask(listOf(runtime.req))
                cartoonDownloadReqController.removeDownloadItem(runtime.req.uuid)
                cartoonLocalController.refresh()
            }

            CartoonDownloadRuntime.State.ERROR -> {
                if (runtime.isResume) {
                    runtime.isResume = false
                    runtime.currentStepIndex = 0

                    // 这里设置 state 后会触发回调调度
                    runtime.state = CartoonDownloadRuntime.State.WAITING
                }
            }

            CartoonDownloadRuntime.State.CANCEL -> {
                innerRemoveTask(listOf(runtime.req))
            }

            CartoonDownloadRuntime.State.WAITING, CartoonDownloadRuntime.State.STEP_COMPLETELY -> {
                if (runnable != null) {
                    dispatchExecutor.execute(runnable)
                }
            }

            else -> {}
        }
    }


}