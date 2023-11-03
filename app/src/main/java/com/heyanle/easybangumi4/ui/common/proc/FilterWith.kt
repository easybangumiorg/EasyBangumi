package com.heyanle.easybangumi4.ui.common.proc

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 筛选
 * Created by heyanlin on 2023/11/3.
 */
class FilterWith<T>(
    val label: String,
    val filter: (T) -> Boolean,
)

class FilterState<T> (
    val list: List<FilterWith<T>>,
    val statusMap: MutableStateFlow<Map<FilterWith<T>, Int>>,
){

    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
        const val STATUS_EXCLUDE = 2
    }
}