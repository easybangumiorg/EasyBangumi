package com.heyanle.easybangumi4.cartoon.story.download.action

import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.story.download.runtime.CartoonDownloadRuntime

/**
 * Created by heyanle on 2024/8/2.
 * https://github.com/heyanLE
 */
interface BaseAction {
    fun canResume(cartoonDownloadReq: CartoonDownloadReq): Boolean
    fun toggle(cartoonDownloadRuntime: CartoonDownloadRuntime): Boolean
    fun push(cartoonDownloadRuntime: CartoonDownloadRuntime)
    fun onCancel(cartoonDownloadRuntime: CartoonDownloadRuntime)

}