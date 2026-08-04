package com.heyanle.easybangumi4.ui.search_migrate.search.overview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class OverviewSearchStateTest {

    @Test
    fun allWithoutResults_isLoadingWhileAnySourceLoadsFirstPage() {
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", refresh = OverviewPageLoadState.Idle),
                source("b", refresh = OverviewPageLoadState.Loading),
            ),
            selectedSourceKey = null,
            hasVerificationItem = false,
        )

        assertEquals(OverviewContentState.Loading, state)
    }

    @Test
    fun allWithoutResults_isEmptyAfterSourcesFinish() {
        val state = resolveOverviewContentState(
            sources = listOf(source("a"), source("b")),
            selectedSourceKey = null,
            hasVerificationItem = false,
        )

        assertEquals(OverviewContentState.Empty, state)
    }

    @Test
    fun allWithPartialResults_keepsContentWhileAnotherSourceLoads() {
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", itemCount = 3),
                source("b", refresh = OverviewPageLoadState.Loading),
            ),
            selectedSourceKey = null,
            hasVerificationItem = false,
        )

        assertEquals(OverviewContentState.Content, state)
    }

    @Test
    fun allWithoutResults_showsErrorWhenEverySourceFails() {
        val firstError = IllegalStateException("first")
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", refresh = OverviewPageLoadState.Error(firstError)),
                source("b", refresh = OverviewPageLoadState.Error(IllegalArgumentException())),
            ),
            selectedSourceKey = null,
            hasVerificationItem = false,
        )

        assertSame(firstError, (state as OverviewContentState.Error).throwable)
    }

    @Test
    fun allWithoutResults_doesNotHidePartialSourceFailureAsEmpty() {
        val error = IllegalStateException("one source failed")
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", refresh = OverviewPageLoadState.Error(error)),
                source("b"),
            ),
            selectedSourceKey = null,
            hasVerificationItem = false,
        )

        assertSame(error, (state as OverviewContentState.Error).throwable)
    }

    @Test
    fun selectedSourceWithoutResults_showsItsError() {
        val error = IllegalStateException("source failed")
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", itemCount = 2),
                source("b", refresh = OverviewPageLoadState.Error(error)),
            ),
            selectedSourceKey = "b",
            hasVerificationItem = false,
        )

        assertSame(error, (state as OverviewContentState.Error).throwable)
    }

    @Test
    fun selectedSourceWithItems_keepsContentDuringAppendLoading() {
        val state = resolveOverviewContentState(
            sources = listOf(
                source(
                    key = "a",
                    itemCount = 10,
                    append = OverviewPageLoadState.Loading,
                ),
            ),
            selectedSourceKey = "a",
            hasVerificationItem = false,
        )

        assertEquals(OverviewContentState.Content, state)
    }

    @Test
    fun verificationCard_countsAsContent() {
        val state = resolveOverviewContentState(
            sources = listOf(
                source("a", refresh = OverviewPageLoadState.Error(IllegalStateException())),
            ),
            selectedSourceKey = "a",
            hasVerificationItem = true,
        )

        assertEquals(OverviewContentState.Content, state)
    }

    private fun source(
        key: String,
        itemCount: Int = 0,
        refresh: OverviewPageLoadState = OverviewPageLoadState.Idle,
        append: OverviewPageLoadState = OverviewPageLoadState.Idle,
    ) = OverviewSourceLoadSnapshot(
        sourceKey = key,
        itemCount = itemCount,
        refresh = refresh,
        append = append,
    )
}
