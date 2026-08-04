package com.heyanle.easybangumi4.ui.search_migrate.search

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmittedSearchRequestGateTest {

    @Test
    fun retainedRequest_isNotHandledAgainWhenPageReturnsToComposition() {
        val gate = SubmittedSearchRequestGate()
        val request = SearchRequest(keyword = "test", sequence = 1)

        assertTrue(gate.shouldHandle(request, hasRetainedState = false))
        assertFalse(gate.shouldHandle(request, hasRetainedState = true))
    }

    @Test
    fun sameKeywordWithNewSequence_isHandledAsExplicitRefresh() {
        val gate = SubmittedSearchRequestGate()

        assertTrue(
            gate.shouldHandle(
                SearchRequest(keyword = "test", sequence = 1),
                hasRetainedState = false,
            ),
        )
        assertTrue(
            gate.shouldHandle(
                SearchRequest(keyword = "test", sequence = 2),
                hasRetainedState = true,
            ),
        )
    }

    @Test
    fun missingRetainedState_isRebuiltEvenForSameRequest() {
        val gate = SubmittedSearchRequestGate()
        val request = SearchRequest(keyword = "test", sequence = 1)

        assertTrue(gate.shouldHandle(request, hasRetainedState = false))
        assertTrue(gate.shouldHandle(request, hasRetainedState = false))
    }

}
