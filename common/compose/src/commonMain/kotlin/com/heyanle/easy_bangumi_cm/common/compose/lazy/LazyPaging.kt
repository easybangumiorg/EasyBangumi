package com.heyanle.easy_bangumi_cm.common.compose.lazy

import androidx.compose.foundation.lazy.LazyListScope
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

/**
 * Created by heyanlin on 2025/2/28.
 */

fun <T : Any> LazyListScope.pagingCommon(
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true
) {

    pagingItems.loadState.prepend
    when (pagingItems.loadState.append) {
        is LoadState.Loading -> {
            item {

            }
        }
        else -> {}
    }



}