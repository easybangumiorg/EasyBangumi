package com.heyanle.easybangumi4.ui.search_migrate.search.overview

/**
 * Small, Paging-independent projection used to decide the full-pane state of overview search.
 *
 * Keeping this reducer free of Compose and LazyPagingItems makes the aggregate "All" rules and
 * source-specific rules deterministic and unit-testable.
 */
internal data class OverviewSourceLoadSnapshot(
    val sourceKey: String,
    val itemCount: Int,
    val refresh: OverviewPageLoadState,
    val append: OverviewPageLoadState,
)

internal sealed interface OverviewPageLoadState {
    data object Idle : OverviewPageLoadState
    data object Loading : OverviewPageLoadState
    data class Error(val throwable: Throwable) : OverviewPageLoadState
}

internal sealed interface OverviewContentState {
    data object Content : OverviewContentState
    data object Loading : OverviewContentState
    data object Empty : OverviewContentState
    data class Error(val throwable: Throwable) : OverviewContentState
}

internal fun resolveOverviewContentState(
    sources: List<OverviewSourceLoadSnapshot>,
    selectedSourceKey: String?,
    hasVerificationItem: Boolean,
): OverviewContentState {
    val relevantSources = selectedSourceKey?.let { key ->
        sources.filter { it.sourceKey == key }
    } ?: sources

    if (hasVerificationItem || relevantSources.any { it.itemCount > 0 }) {
        return OverviewContentState.Content
    }
    if (relevantSources.any { it.isInitialLoading }) {
        return OverviewContentState.Loading
    }
    if (selectedSourceKey != null) {
        val error = relevantSources.firstNotNullOfOrNull { it.initialError }
        if (error != null) return OverviewContentState.Error(error)
    } else {
        val error = relevantSources.firstNotNullOfOrNull { it.initialError }
        if (error != null) return OverviewContentState.Error(error)
    }
    return OverviewContentState.Empty
}

internal val OverviewSourceLoadSnapshot.isFirstPageLoading: Boolean
    get() = refresh is OverviewPageLoadState.Loading

private val OverviewSourceLoadSnapshot.isInitialLoading: Boolean
    get() = itemCount == 0 && (
        refresh is OverviewPageLoadState.Loading ||
            append is OverviewPageLoadState.Loading
        )

private val OverviewSourceLoadSnapshot.initialError: Throwable?
    get() = if (itemCount == 0) {
        (refresh as? OverviewPageLoadState.Error)?.throwable
            ?: (append as? OverviewPageLoadState.Error)?.throwable
    } else {
        null
    }
