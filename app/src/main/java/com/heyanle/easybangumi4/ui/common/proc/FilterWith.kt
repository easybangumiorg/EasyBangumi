package com.heyanle.easybangumi4.ui.common.proc

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 筛选
 * Created by heyanlin on 2023/11/3.
 */
class FilterWith<T>(
    val label: String,
    val filter: (T) -> Boolean,
) {

    companion object {
        const val STATUS_OFF = 0        // 关闭
        const val STATUS_ON = 1         // 开启
        const val STATUS_REVERSE = 2    // 反向
    }

    var status = MutableStateFlow(STATUS_OFF)
}