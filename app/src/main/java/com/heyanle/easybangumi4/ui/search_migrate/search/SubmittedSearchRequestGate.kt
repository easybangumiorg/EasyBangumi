package com.heyanle.easybangumi4.ui.search_migrate.search

/**
 * Separates a user-submitted search event from a Composable entering composition.
 *
 * The same request is ignored while its result state is still retained. A new sequence always
 * passes, so explicitly submitting an unchanged keyword continues to refresh results.
 */
internal class SubmittedSearchRequestGate {
    private var lastHandledRequest: SearchRequest? = null

    fun shouldHandle(request: SearchRequest, hasRetainedState: Boolean): Boolean {
        if (lastHandledRequest == request && hasRetainedState) return false
        lastHandledRequest = request
        return true
    }

}
