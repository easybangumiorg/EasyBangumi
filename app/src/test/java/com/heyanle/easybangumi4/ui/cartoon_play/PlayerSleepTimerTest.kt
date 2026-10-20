package com.heyanle.easybangumi4.ui.cartoon_play

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerSleepTimerTest {
    @Test
    fun countdownPausesOnceAndBlocksAnEndEventUntilManualResume() = runTest {
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) { pauseCount++ }

        timer.select(PlayerSleepTimerOption.FIFTEEN_MINUTES)
        advanceTimeBy(14 * 60_000L)
        runCurrent()
        assertEquals(60_000L, timer.state.value.remainingMs)
        assertEquals(0, pauseCount)

        advanceTimeBy(60_000L)
        runCurrent()
        assertEquals(1, pauseCount)
        assertTrue(timer.state.value.stoppedByTimer)
        assertTrue(timer.onEpisodeEnded())
        assertEquals(1, pauseCount)

        timer.onPlaybackStarted()
        assertFalse(timer.onEpisodeEnded())
        assertFalse(timer.state.value.stoppedByTimer)
    }

    @Test
    fun replacingAnActiveTimerCancelsItsOriginalDeadline() = runTest {
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.FIFTEEN_MINUTES)
        advanceTimeBy(10 * 60_000L)

        timer.select(PlayerSleepTimerOption.THIRTY_MINUTES)
        advanceTimeBy(5 * 60_000L)
        runCurrent()
        assertEquals(0, pauseCount)
        assertEquals(25 * 60_000L, timer.state.value.remainingMs)

        advanceTimeBy(25 * 60_000L)
        runCurrent()
        assertEquals(1, pauseCount)
    }

    @Test
    fun clearingOnPageExitPreventsAnyLaterPause() = runTest {
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.FIFTEEN_MINUTES)
        runCurrent()

        timer.clear()
        advanceTimeBy(60 * 60_000L)
        runCurrent()

        assertEquals(0, pauseCount)
        assertEquals(PlayerSleepTimerState(), timer.state.value)
        assertFalse(timer.onEpisodeEnded())
    }

    @Test
    fun endOfEpisodePausesAndConsumesAutomaticNextOnce() = runTest {
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.END_OF_EPISODE)
        advanceTimeBy(60 * 60_000L)
        assertEquals(0, pauseCount)

        assertTrue(timer.onEpisodeEnded())
        assertEquals(1, pauseCount)
        assertTrue(timer.onEpisodeEnded())
        assertEquals(1, pauseCount)
    }

    @Test
    fun timedModeAllowsNextEpisodeWhileTimeRemains() = runTest {
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) {}
        timer.select(PlayerSleepTimerOption.SIXTY_MINUTES)
        advanceTimeBy(20 * 60_000L)
        runCurrent()

        assertFalse(timer.onEpisodeEnded())
        assertEquals(40 * 60_000L, timer.state.value.remainingMs)
        timer.clear()
    }

    @Test
    fun elapsedClockJumpExpiresOnNextTickInsteadOfExtendingTimer() = runTest {
        var elapsedRealtime = 0L
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { elapsedRealtime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.FIFTEEN_MINUTES)
        runCurrent()

        elapsedRealtime = 20 * 60_000L
        advanceTimeBy(1_000L)
        runCurrent()

        assertEquals(1, pauseCount)
        assertTrue(timer.state.value.stoppedByTimer)
    }

    @Test
    fun episodeEndingAtDeadlineStopsBeforeTheNextScheduledTick() = runTest {
        var elapsedRealtime = 0L
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { elapsedRealtime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.FIFTEEN_MINUTES)
        runCurrent()

        elapsedRealtime = 15 * 60_000L
        assertTrue(timer.onEpisodeEnded())
        advanceTimeBy(1_000L)
        runCurrent()

        assertEquals(1, pauseCount)
    }

    @Test
    fun selectingOffCancelsEpisodeEndStop() = runTest {
        var pauseCount = 0
        val timer = PlayerSleepTimer(this, { testScheduler.currentTime }) { pauseCount++ }
        timer.select(PlayerSleepTimerOption.END_OF_EPISODE)
        timer.select(PlayerSleepTimerOption.OFF)

        assertFalse(timer.onEpisodeEnded())
        assertEquals(0, pauseCount)
        assertEquals(PlayerSleepTimerState(), timer.state.value)
    }
}
