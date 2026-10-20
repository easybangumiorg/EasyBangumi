package com.heyanle.easybangumi4.player.mpv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MpvPlaybackIntentTest {
    @Test
    fun timerPauseBeforeSurfaceAttachmentLoadsPaused() {
        val intent = MpvPlaybackIntent()
        intent.queueLoad("https://example.test/episode.m3u8", playWhenReady = true)

        intent.requestPlayback(false)

        assertFalse(intent.playWhenReady)
        assertEquals(
            MpvPlaybackIntent.Load("https://example.test/episode.m3u8", playWhenReady = false),
            intent.takePendingLoad(),
        )
        assertFalse(intent.hasPendingLoad)
        assertNull(intent.takePendingLoad())
    }

    @Test
    fun lateNativeUnpauseCannotUndoTheNewerTimerPause() {
        val intent = MpvPlaybackIntent()
        intent.requestPlayback(true)
        intent.onNativePauseChanged(false)
        assertTrue(intent.isPlayingAllowed)

        intent.requestPlayback(false)
        assertFalse(intent.isPlayingAllowed)
        intent.onNativePauseChanged(false)

        assertFalse(intent.playWhenReady)
        assertFalse(intent.isPlayingAllowed)
    }

    @Test
    fun explicitResumeBeforeSurfaceAttachmentRestoresPlayback() {
        val intent = MpvPlaybackIntent()
        intent.queueLoad("episode", playWhenReady = true)
        intent.requestPlayback(false)
        intent.requestPlayback(true)

        assertTrue(intent.playWhenReady)
        assertEquals(MpvPlaybackIntent.Load("episode", true), intent.takePendingLoad())
        intent.onNativePauseChanged(false)
        assertTrue(intent.isPlayingAllowed)
    }

    @Test
    fun lateNativePauseDoesNotReplaceANewerResumeRequest() {
        val intent = MpvPlaybackIntent()
        intent.requestPlayback(false)
        intent.requestPlayback(true)
        intent.onNativePauseChanged(true)

        assertTrue(intent.playWhenReady)
        assertFalse(intent.isPlayingAllowed)
        intent.onNativePauseChanged(false)
        assertTrue(intent.isPlayingAllowed)
    }

    @Test
    fun replacingAndCancellingAPendingLoadCannotReviveTheOldMedia() {
        val intent = MpvPlaybackIntent()
        intent.queueLoad("old-episode", playWhenReady = true)
        intent.queueLoad("new-episode", playWhenReady = false)
        assertEquals(MpvPlaybackIntent.Load("new-episode", false), intent.takePendingLoad())

        intent.queueLoad("cancelled-episode", playWhenReady = true)
        intent.cancelPendingLoad()
        intent.requestPlayback(false)
        assertNull(intent.takePendingLoad())
        assertFalse(intent.playWhenReady)
    }
}
