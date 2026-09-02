package com.heyanle.easybangumi4.v2.ui.history

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryProgressTest {

    @Test
    fun copyHistoryStoresBothActualPositionAndDuration() {
        val episode = Episode("episode-3", "第 3 集", 3)
        val line = PlayLine("line-a", "线路 A", arrayListOf(episode))

        val history = cartoonInfo().copyHistory(
            playLineIndex = 0,
            playLine = line,
            episode = episode,
            process = 90_000L,
            totalTime = 1_200_000L,
        )

        assertEquals(90_000L, history.lastProcessTime)
        assertEquals(1_200_000L, history.lastTotalTile)
        assertEquals("episode-3", history.lastEpisodeId)
    }

    @Test
    fun historyPresentationShowsActualPositionAndTotalDuration() {
        assertEquals("01:30 / 20:00", historyProgressText(90_000L, 1_200_000L))
        assertEquals(0.075f, historyProgressFraction(90_000L, 1_200_000L), 0.0001f)
        assertEquals(1f, historyProgressFraction(1_300_000L, 1_200_000L), 0.0001f)
    }

    @Test
    fun legacyHistoryWithoutDurationStillShowsRealPosition() {
        assertEquals("01:30", historyProgressText(90_000L, 0L))
        assertEquals(0f, historyProgressFraction(90_000L, 0L), 0.0001f)
    }

    private fun cartoonInfo() = CartoonInfo(
        id = "cartoon-id",
        source = "source-id",
        name = "测试番剧",
        coverUrl = "",
        intro = "",
        url = "",
    )
}
