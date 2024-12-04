package com.heyanle.easy_bangumi_cm.shared.base.data

/**
 * Created by heyanlin on 2024/12/4.
 */
data class PageData<T>(
    val pageSize: Int,
    val index: Int = 0,
    val hasMore: Boolean = false,
    val data: List<T> = emptyList(),
    val refreshLoadingState: LoadingState = LoadingState.None,
    val loadMoreLoadingState: LoadingState = LoadingState.None,
) {

    sealed class LoadingState {
        data object None: LoadingState()
        data object Loading: LoadingState()
        data class Error(val errorMsg: String, val throwable: Throwable?): LoadingState()
    }

    fun isRefreshing() = refreshLoadingState is LoadingState.Loading
    fun isLoadingMore() = loadMoreLoadingState is LoadingState.Loading

    fun nextPage(n: List<T>, hasMore: Boolean): PageData<T> {
        return copy(
            index = index + 1,
            data = data + n,
            hasMore = hasMore,
            loadMoreLoadingState = LoadingState.None
        )
    }
}