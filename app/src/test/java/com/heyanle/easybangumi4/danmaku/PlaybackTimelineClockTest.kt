package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimelineClockTest {
    @Test
    fun playingClockInterpolatesInMediaTimeAtPlaybackSpeed() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(10_000, 2f, true, realtimeMillis = 1_000)
        assertEquals(10_000, clock.positionAt(1_000))
        assertEquals(12_000, clock.positionAt(2_000))
    }

    @Test
    fun pausedAndBufferingClockRemainAtPlayerPosition() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(4_200, 4f, false, realtimeMillis = 100)
        assertEquals(4_200, clock.positionAt(10_000))
    }

    @Test
    fun pausingAcceptsThePlayersExactPositionAfterInterpolation() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(1_000, 2f, true, realtimeMillis = 0)
        assertEquals(1_500, clock.positionAt(250))
        clock.synchronize(1_480, 2f, false, realtimeMillis = 250)
        assertEquals(1_480, clock.positionAt(10_000))
    }

    @Test
    fun speedChangeKeepsPositionContinuousAndUsesNewSlope() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(1_000, 1f, true, realtimeMillis = 0)
        assertEquals(2_000, clock.positionAt(1_000))
        clock.synchronize(2_000, 2f, true, realtimeMillis = 1_000)
        assertEquals(2_000, clock.positionAt(1_000))
        assertEquals(4_000, clock.positionAt(2_000))
    }

    @Test
    fun negativeCorrectionNeverRunsBackwardAndThenConvergesToPlayerTime() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(0, 1f, true, realtimeMillis = 0)
        assertEquals(240, clock.positionAt(240))
        clock.synchronize(230, 1f, true, realtimeMillis = 250)
        assertEquals(240, clock.positionAt(250))
        assertEquals(480, clock.positionAt(500))
        clock.synchronize(490, 1f, true, realtimeMillis = 500)
        assertEquals(490, clock.positionAt(500))
    }

    @Test
    fun explicitSeekCanMoveBackwardAndThenResumeFromNewPosition() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(20_000, 1f, true, realtimeMillis = 0)
        assertEquals(21_000, clock.positionAt(1_000))
        clock.synchronize(5_000, 1f, true, realtimeMillis = 1_000, allowBackward = true)
        assertEquals(5_000, clock.positionAt(1_000))
        assertEquals(6_000, clock.positionAt(2_000))
    }

    @Test
    fun invalidSpeedsUseNeutralAndSupportedSpeedsAreBounded() {
        assertEquals(1f, Float.NaN.normalizedPlaybackSpeed(), 0f)
        assertEquals(0.25f, 0.01f.normalizedPlaybackSpeed(), 0f)
        assertEquals(4f, 20f.normalizedPlaybackSpeed(), 0f)
    }

    @Test
    fun syncedAndUnsyncedMotionHaveTheExpectedWallClockSpeed() {
        val clock = PlaybackTimelineClock()
        clock.synchronize(0, 2f, true, realtimeMillis = 0)

        val syncedStyle = DanmakuDisplayConfig.DEFAULT.toDfmStyle(
            scaledDensity = 1f,
            density = 1f,
            playbackSpeed = 2f,
        )
        val unsyncedStyle = DanmakuDisplayConfig.DEFAULT.copy(
            syncScrollSpeedWithPlayback = false,
        ).toDfmStyle(
            scaledDensity = 1f,
            density = 1f,
            playbackSpeed = 2f,
        )
        val synced = ScrollDanmakuMotion(0, 1_000f, syncedStyle.scrollPixelsPerMediaSecond)
        val unsynced = ScrollDanmakuMotion(0, 1_000f, unsyncedStyle.scrollPixelsPerMediaSecond)
        val mediaTimeAfterOneWallSecond = clock.positionAt(1_000)

        assertEquals(800f, synced.leftAt(mediaTimeAfterOneWallSecond), 0f)
        assertEquals(900f, unsynced.leftAt(mediaTimeAfterOneWallSecond), 0f)
    }
}
