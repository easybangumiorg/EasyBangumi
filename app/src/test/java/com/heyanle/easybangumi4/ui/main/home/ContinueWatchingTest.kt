package com.heyanle.easybangumi4.ui.main.home

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import org.junit.Assert.*
import org.junit.Test

class ContinueWatchingTest {
    @Test fun latestUnfinishedHistoryWinsRegardlessOfQueryOrder() {
        val older = item("old", 10)
        val newest = item("new", 20)
        val finished = item("finished", 30).copy(lastProcessTime = 60_000)
        assertEquals(newest, selectContinueWatching(listOf(finished, older, newest), false, null))
    }
    @Test fun privateModeNeverExposesHistory() {
        assertNull(selectContinueWatching(listOf(item("one", 10)), true, null))
    }
    @Test fun dismissDoesNotImmediatelyFallBackToAnOlderHistory() {
        val latest = item("new", 20)
        assertNull(selectContinueWatching(listOf(latest, item("old", 10)), false, latest.continueWatchingKey()))
        val resumed = latest.copy(lastHistoryTime = 30)
        assertEquals(resumed, selectContinueWatching(listOf(resumed), false, latest.continueWatchingKey()))
    }
    @Test fun unknownDurationCanResumeButZeroProgressCannot() {
        assertNotNull(selectContinueWatching(listOf(item("one", 10).copy(lastTotalTile = 0)), false, null))
        assertNull(selectContinueWatching(listOf(item("one", 10).copy(lastProcessTime = 0)), false, null))
    }
    private fun item(id: String, watched: Long) = CartoonInfo(
        id = id, source = "source", name = id, coverUrl = "", intro = "", url = "",
        lastHistoryTime = watched, lastProcessTime = 15_000, lastTotalTile = 60_000,
    )
}
