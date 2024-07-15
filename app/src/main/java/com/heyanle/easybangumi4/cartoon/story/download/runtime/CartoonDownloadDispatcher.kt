package com.heyanle.easybangumi4.cartoon.story.download.runtime

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon.story.download.CartoonDownloadPreference
import com.heyanle.easybangumi4.cartoon.story.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 下载任务调度器，这里不支持持久化，只服务于运行时
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadDispatcher(
    private val cartoonDownloadPreference: CartoonDownloadPreference,
    private val cartoonDownloadRuntimeFactory: CartoonDownloadRuntimeFactory,
    private val cartoonDownloadReqController: CartoonDownloadReqController,
    private val cartoonLocalController: CartoonLocalController,
): CartoonDownloadRuntime.Listener {

    // 调度统一给主线程调度
    private val scope = MainScope()
    private val executor = ThreadPoolExecutor(
        cartoonDownloadPreference.downloadMaxCountPref.get(),
        cartoonDownloadPreference.downloadMaxCountPref.get(),
        50,
        TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>(),
        Executors.defaultThreadFactory()
    )

    private val _runtimeMap = MutableStateFlow<Map<String, CartoonDownloadRuntime>>(emptyMap())
    val runtimeMap = _runtimeMap.asStateFlow()



    fun addTask(
        item: CartoonDownloadReq
    ) {
        scope.launch {
            innerAddTask(listOf(item))
        }
    }

    fun addTask(
        itemList: Collection<CartoonDownloadReq>
    ) {
        scope.launch {
           innerAddTask(itemList)
        }
    }

    private fun innerAddTask(
        itemList: Collection<CartoonDownloadReq>
    ) {
        val needRemoveUUID = arrayListOf<String>()
        val needPutRuntime = arrayListOf<Pair<String,CartoonDownloadRuntime>>()
        itemList.forEach { item ->
            // 这里只有主线程，不用考虑并发
            val current = _runtimeMap.value
            current[item.uuid]?.run {
                cancel()
                executor.remove(runnable)
            }
            var targetRuntime: CartoonDownloadRuntime? = null
            val runtime = cartoonDownloadRuntimeFactory.newRuntime(item)
            if (runtime.dispatcher()) {
                executor.execute(runtime.runnable)
                targetRuntime = runtime
            }

            if (targetRuntime == null) {
                needRemoveUUID.add(item.uuid)
            } else {
                targetRuntime.listener = this
                needPutRuntime.add(item.uuid to targetRuntime)
            }
        }

        _runtimeMap.update {
            it.toMutableMap().apply {
                needRemoveUUID.forEach {
                    remove(it)
                }
                needPutRuntime.forEach {
                    put(it.first, it.second)
                }
            }
        }
    }

    fun removeTaskWithItemId(
        itemId: String
    ) {
        scope.launch {
            val current = _runtimeMap.value
            val needRemoveTask = current.filter {
                it.value.req.toLocalItemId == itemId
            }.map { it.value.req }
            innerRemoveTask(needRemoveTask)
        }


    }

    fun removeTaskWithItemId(
        localItemId: Collection<String>
    ) {
        scope.launch {
            val current = _runtimeMap.value
            val d = localItemId.toSet()
            val needRemoveTask = current.filter {
                d.contains(it.value.req.toLocalItemId)
            }.map { it.value.req }
            innerRemoveTask(needRemoveTask)
        }
    }



    fun removeTask(
        item: CartoonDownloadReq
    ){
        scope.launch {
            innerRemoveTask(listOf(item))
        }

    }

    fun removeTask(
        itemList: Collection<CartoonDownloadReq>
    ){
        scope.launch {
            innerRemoveTask(itemList)
        }

    }

    private fun innerRemoveTask(
        itemList: Collection<CartoonDownloadReq>
    ) {
        val current = _runtimeMap.value
        itemList.forEach { item ->
            current[item.uuid]?.run {
                cancel()
                executor.remove(runnable)
            }
            _runtimeMap.update {
                it.toMutableMap().apply {
                    remove(item.uuid)
                }
            }
        }
    }

    override fun onStateChange(runtime: CartoonDownloadRuntime) {
        runtime.state.logi("CartoonDownloadDispatcher")
        when (runtime.state) {
            CartoonDownloadRuntime.STATE_SUCCESS -> {
                removeTask(runtime.req)
                cartoonDownloadReqController.removeDownloadItem(runtime.req.uuid)
                cartoonLocalController.refresh()
            }
            CartoonDownloadRuntime.STATE_CANCEL -> {
                removeTask(runtime.req)
            }

        }
    }

}