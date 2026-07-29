package com.heyanle.easybangumi4.ui.cartoon_play

import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertEquals
import org.junit.Test

class DanmakuSortedEpisodePositionTest {

    @Test
    fun positionComesFromLatestSelectedSortProjection() {
        val first = Episode("first", "Z", order = 1)
        val second = Episode("second", "A", order = 99)
        val line = PlayLine("line", "线路", arrayListOf(first, second))
        val byOrder = PlayLineWrapper(line, comparator = compareBy { it.order })
        val byLabel = PlayLineWrapper(line, comparator = compareBy { it.label })

        assertEquals(
            2,
            resolveDanmakuSortedEpisodePosition(listOf(byOrder), "line", "second"),
        )
        assertEquals(
            1,
            resolveDanmakuSortedEpisodePosition(listOf(byLabel), "line", "second"),
        )
    }

    @Test
    fun reversedProjectionChangesPositionWithoutReadingEpisodeMetadata() {
        val first = Episode("first", "任意", order = 500)
        val second = Episode("second", "任意", order = -100)
        val line = PlayLine("line", "线路", arrayListOf(first, second))
        val ascending = PlayLineWrapper(line, comparator = compareBy { it.id })
        val descending = PlayLineWrapper(
            playLine = line,
            isReverse = true,
            comparator = compareBy { it.id },
        )

        assertEquals(
            1,
            resolveDanmakuSortedEpisodePosition(listOf(ascending), "line", "first"),
        )
        assertEquals(
            2,
            resolveDanmakuSortedEpisodePosition(listOf(descending), "line", "first"),
        )
    }

    @Test
    fun missingLineOrEpisodeProducesInvalidPosition() {
        val episode = Episode("episode", "选集", order = 1)
        val line = PlayLineWrapper(
            PlayLine("line", "线路", arrayListOf(episode)),
            comparator = compareBy { it.order },
        )

        assertEquals(0, resolveDanmakuSortedEpisodePosition(listOf(line), "missing", "episode"))
        assertEquals(0, resolveDanmakuSortedEpisodePosition(listOf(line), "line", "missing"))
    }
}
