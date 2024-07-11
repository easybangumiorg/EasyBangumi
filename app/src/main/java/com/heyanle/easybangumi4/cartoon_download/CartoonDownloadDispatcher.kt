package com.heyanle.easybangumi4.cartoon_download

import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadRuntime
import com.heyanle.easybangumi4.cartoon_local.CartoonLocalController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
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
        0,
        cartoonDownloadPreference.downloadMaxCountPref.get(),
        4,
        TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>(),
        Executors.defaultThreadFactory()
    )

    private val runtimeMap = HashMap<CartoonDownloadReq, CartoonDownloadRuntime>()



    fun addTask(
        item: CartoonDownloadReq
    ) {
        scope.launch {
            runtimeMap[item]?.run {
                state = 5
                dispatchStateToBus()
                executor.remove(runnable)
            }
            val runtime = cartoonDownloadRuntimeFactory.newRuntime(item)
            runtime.dispatchStateToBus()
            runtimeMap[item] = runtime
            executor.execute(runtime.runnable)
            cartoonLocalController.putLocalEpisode(
                item.toLocalItemId, setOf(item.toEpisode)
            )
        }
    }



    fun removeTask(
        item: CartoonDownloadReq
    ){
        scope.launch {
            runtimeMap[item]?.run {
                state = 5
                dispatchStateToBus()
                executor.remove(runnable)
            }
            runtimeMap.remove(item)
        }
    }

}