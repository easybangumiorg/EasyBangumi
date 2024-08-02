package com.heyanle.easybangumi4.cartoon.story.download_v1.step

import androidx.annotation.WorkerThread
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download_v1.runtime.CartoonDownloadRuntime

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
interface BaseStep {


    @WorkerThread
    fun invoke()

    fun canRestart(req: CartoonDownloadReq): Boolean = false

    fun tryToggle(runtime: CartoonDownloadRuntime) : Boolean = false


    fun cancel(runtime: CartoonDownloadRuntime)

}