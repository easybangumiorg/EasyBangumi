package com.heyanle.easybangumi4.cartoon.story.download.action

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime

/**
 * Created by heyanle on 2024/8/2.
 * https://github.com/heyanLE
 */
interface BaseAction {

    // 对于同步任务，Dispatcher 会在工作线程池执行 push，直接在里面执行即可
    // 对于异步任务，Dispatcher 会在调度线程池执行 push，push 需要同步返回
    fun isAsyncAction(): Boolean = true
    suspend fun canResume(
        cartoonDownloadReq: CartoonDownloadReq
    ): Boolean
    suspend fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean

    // 当整个任务完成时调用，可以用于清理中间产物
    fun onTaskCompletely(cartoonDownloadRuntime: CartoonDownloadRuntime) {}
    fun push(cartoonDownloadRuntime: CartoonDownloadRuntime)
    fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime)

}