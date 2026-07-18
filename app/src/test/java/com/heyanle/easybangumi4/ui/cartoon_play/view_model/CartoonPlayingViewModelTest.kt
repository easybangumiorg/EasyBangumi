package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertFalse
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
}
