package com.heyanle.easybangumi4.cartoon.download.runtime

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon.download.CartoonDownloadPreference
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
    private val cartoonLocalController: CartoonLocalController,
) {

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
            _runtimeMap.update {
                it.toMutableMap().apply {
                    if (targetRuntime == null){
                        remove(item.uuid)
                    } else {
                        put(item.uuid, targetRuntime)
                    }
                }
            }
        }
    }



    fun removeTask(
        item: CartoonDownloadReq
    ){
        val current = _runtimeMap.value
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