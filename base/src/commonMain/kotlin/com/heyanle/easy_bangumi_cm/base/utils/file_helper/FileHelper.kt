package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import com.heyanle.easy_bangumi_cm.base.data.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Created by heyanlin on 2024/12/11.
 */
interface FileHelper<T> {

    val flow: Flow<DataState<T>>

    fun set(t: T)
}

