package com.heyanle.easybangumi4.ui.cartoon_play

import com.heyanle.easybangumi4.plugin.api.entity.Episode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadEpisodeSelectionTest {

    private val episodes = listOf(
        Episode("episode-1", "第 1 集", 1),
        Episode("episode-2", "第 2 集", 2),
        Episode("episode-3", "第 3 集", 3),
    )

    @Test
    fun selectionStartsEmptyAndResolvesOnlyExplicitChoices() {
        val selection = DownloadEpisodeSelection("line")
            .toggle("episode-2")

        assertEquals(listOf("episode-2"), selection.resolve(episodes).map(Episode::id))
    }

    @Test
    fun toggleAllSelectsThenClearsTheCurrentEpisodeSet() {
        val selected = DownloadEpisodeSelection("line").toggleAll(episodes)
        assertEquals(episodes.map(Episode::id).toSet(), selected.episodeIds)

        val cleared = selected.toggleAll(episodes)
        assertTrue(cleared.episodeIds.isEmpty())
    }

    @Test
    fun resolveIgnoresStaleIdsAndKeepsCurrentEpisodeOrder() {
        val selection = DownloadEpisodeSelection(
            lineId = "line",
            episodeIds = setOf("removed", "episode-3", "episode-1"),
        )

        assertEquals(
            listOf("episode-1", "episode-3"),
            selection.resolve(episodes).map(Episode::id),
        )
    }
}
