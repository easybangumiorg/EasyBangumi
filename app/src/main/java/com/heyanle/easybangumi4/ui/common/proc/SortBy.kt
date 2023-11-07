package com.heyanle.easybangumi4.ui.common.proc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/**
 * 排序项目
 * Created by heyanlin on 2023/11/3.
 */
class SortBy<T>(
    val id: String,
    val label: String,
    val comparator: Comparator<T>,
)

data class SortState<T>(
    val sortList: List<SortBy<T>>,
    val current: StateFlow<String>,
    val isReverse: StateFlow<Boolean>,
){
    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
        const val STATUS_REVERSE = 2
    }
}
