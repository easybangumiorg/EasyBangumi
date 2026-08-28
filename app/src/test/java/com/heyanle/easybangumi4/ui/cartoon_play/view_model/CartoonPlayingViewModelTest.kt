package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartoonPlayingViewModelTest {

    private val line = PlayLine("0", "播放线路1", arrayListOf())
    private val episode = Episode("1", "第1集", 0)

    @Test
    fun differentCartoonsWithSameLineAndEpisodeMustNotReusePlayback() {
        assertFalse(
            CartoonPlayingViewModel.isSamePlaybackTarget(
                previousSummary = CartoonSummary("cartoon-a", "kazumi.baimao"),
                previousPlayLine = line,
                previousEpisode = episode,
                nextSummary = CartoonSummary("cartoon-b", "kazumi.baimao"),
                nextPlayLine = line,
                nextEpisode = episode,
            )
        )
    }

    @Test
    fun sameCartoonLineAndEpisodeCanReusePlayback() {
        assertTrue(
            CartoonPlayingViewModel.isSamePlaybackTarget(
                previousSummary = CartoonSummary("cartoon-a", "kazumi.baimao"),
                previousPlayLine = line,
                previousEpisode = episode,
                nextSummary = CartoonSummary("cartoon-a", "kazumi.baimao"),
                nextPlayLine = line,
                nextEpisode = episode,
            )
        )
    }

    @Test
    fun surfaceLossKeepsExistingPlayerWhenMediaIsStillAttached() {
        assertEquals(
            CartoonPlayingViewModel.ContinuationAction.KEEP_PLAYER,
            CartoonPlayingViewModel.continuationAction(
                hasResolvedTarget = true,
                playerHasMedia = true,
            ),
        )
    }

    @Test
    fun surfaceLossReloadsResolvedUriWithoutResolvingSourceAgain() {
        assertEquals(
            CartoonPlayingViewModel.ContinuationAction.LOAD_RESOLVED_MEDIA,
            CartoonPlayingViewModel.continuationAction(
                hasResolvedTarget = true,
                playerHasMedia = false,
            ),
        )
    }

    @Test
    fun unresolvedTargetStillUsesSourceResolver() {
        assertEquals(
            CartoonPlayingViewModel.ContinuationAction.RESOLVE_SOURCE,
            CartoonPlayingViewModel.continuationAction(
                hasResolvedTarget = false,
                playerHasMedia = false,
            ),
        )
    }

    @Test
    fun samePlaybackTargetConsumesPageResumePosition() {
        val checkpoint = CartoonPlayingViewModel.PlaybackResumeCheckpoint()
        val summary = CartoonSummary("cartoon-a", "kazumi.baimao")
        checkpoint.capture(
            summary,
            line,
            episode,
            positionMs = 42_000L,
            playWhenReady = true,
        )

        assertEquals(
            CartoonPlayingViewModel.PlaybackResumeCheckpoint.ResumeDirective(
                positionMs = 42_000L,
                playWhenReady = true,
            ),
            checkpoint.consume(summary, line, episode, explicitPositionMs = -1L),
        )
        assertEquals(
            CartoonPlayingViewModel.PlaybackResumeCheckpoint.ResumeDirective(
                positionMs = -1L,
                playWhenReady = true,
            ),
            checkpoint.consume(summary, line, episode, explicitPositionMs = -1L),
        )
    }

    @Test
    fun explicitPositionOverridesPageResumePosition() {
        val checkpoint = CartoonPlayingViewModel.PlaybackResumeCheckpoint()
        val summary = CartoonSummary("cartoon-a", "kazumi.baimao")
        checkpoint.capture(
            summary,
            line,
            episode,
            positionMs = 42_000L,
            playWhenReady = true,
        )

        assertEquals(
            CartoonPlayingViewModel.PlaybackResumeCheckpoint.ResumeDirective(
                positionMs = 10_000L,
                playWhenReady = true,
            ),
            checkpoint.consume(summary, line, episode, explicitPositionMs = 10_000L),
        )
    }

    @Test
    fun pageResumePositionDoesNotLeakToAnotherEpisode() {
        val checkpoint = CartoonPlayingViewModel.PlaybackResumeCheckpoint()
        val summary = CartoonSummary("cartoon-a", "kazumi.baimao")
        checkpoint.capture(
            summary,
            line,
            episode,
            positionMs = 42_000L,
            playWhenReady = true,
        )

        assertEquals(
            CartoonPlayingViewModel.PlaybackResumeCheckpoint.ResumeDirective(
                positionMs = -1L,
                playWhenReady = true,
            ),
            checkpoint.consume(
                summary = summary,
                playLine = line,
                episode = Episode("2", "第2集", 1),
                explicitPositionMs = -1L,
            ),
        )
    }

    @Test
    fun pausedPlaybackRemainsPausedAfterPageReturn() {
        val checkpoint = CartoonPlayingViewModel.PlaybackResumeCheckpoint()
        val summary = CartoonSummary("cartoon-a", "kazumi.baimao")
        checkpoint.capture(
            summary,
            line,
            episode,
            positionMs = 42_000L,
            playWhenReady = false,
        )

        assertEquals(
            CartoonPlayingViewModel.PlaybackResumeCheckpoint.ResumeDirective(
                positionMs = 42_000L,
                playWhenReady = false,
            ),
            checkpoint.consume(summary, line, episode, explicitPositionMs = -1L),
        )
    }
}
