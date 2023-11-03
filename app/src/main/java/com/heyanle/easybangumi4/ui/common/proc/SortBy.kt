package com.heyanle.easybangumi4.ui.common.proc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow


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
    val current: MutableStateFlow<SortBy<T>>,
    val isReverse: MutableStateFlow<Boolean>,
)
