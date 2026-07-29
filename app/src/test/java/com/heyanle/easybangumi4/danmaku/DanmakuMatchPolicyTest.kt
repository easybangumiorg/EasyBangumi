package com.heyanle.easybangumi4.danmaku

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuMatchPolicyTest {

    @Test
    fun similarityExactlyPointEightDoesNotMatchBangumi() = runBlocking {
        val candidate = bangumi(title = "abcdf")

        val result = DanmakuMatchPolicy.matchBangumi("abcde") {
            DanmakuResult.Success(listOf(candidate))
        } as DanmakuResult.Success

        assertEquals(0.80, DanmakuMatchPolicy.titleSimilarity("abcde", "abcdf"), 0.0)
        assertNull(result.value.matchedBangumi)
        assertEquals(listOf(candidate), result.value.candidates)
    }

    @Test
    fun onlyFirstBangumiCandidateCanMatchAutomatically() = runBlocking {
        val result = DanmakuMatchPolicy.matchBangumi("目标番剧") {
            DanmakuResult.Success(
                listOf(
                    bangumi(id = 1L, title = "完全不同"),
                    bangumi(id = 2L, title = "目标番剧"),
                ),
            )
        } as DanmakuResult.Success

        assertNull(result.value.matchedBangumi)
    }

    @Test
    fun normalizedTitleAboveThresholdMatchesFirstBangumi() = runBlocking {
        val expected = bangumi(title = "ＡＢＣ：第二季")

        val result = DanmakuMatchPolicy.matchBangumi("abc 第二季") {
            DanmakuResult.Success(listOf(expected), fromCache = true)
        } as DanmakuResult.Success

        assertEquals(expected, result.value.matchedBangumi)
        assertTrue(result.fromCache)
    }

    @Test
    fun episodeMatchUsesOnlyCurrentSortedPosition() {
        val episodes = listOf(episode(101L), episode(102L), episode(103L))

        val result = DanmakuMatchPolicy.matchEpisode(
            request = DanmakuEpisodeMatchRequest(sortedEpisodePosition = 2),
            episodes = episodes,
        )

        assertEquals(episodes[1], result.matchedEpisodeOrNull())
    }

    @Test
    fun sameLocalEpisodeAtDifferentSortPositionsMapsToDifferentRemoteEpisodes() {
        val episodes = listOf(episode(101L), episode(102L))

        val first = DanmakuMatchPolicy.matchEpisode(DanmakuEpisodeMatchRequest(1), episodes)
        val second = DanmakuMatchPolicy.matchEpisode(DanmakuEpisodeMatchRequest(2), episodes)

        assertEquals(101L, first.matchedEpisodeOrNull()?.remoteEpisodeId)
        assertEquals(102L, second.matchedEpisodeOrNull()?.remoteEpisodeId)
    }

    @Test
    fun invalidOrOutOfBoundsPositionNeverFallsBackToEpisodeMetadata() {
        val misleadingEpisode = episode(101L).copy(
            episodeNumber = "99",
            episodeTitle = "第99集",
        )

        assertNull(
            DanmakuMatchPolicy.matchEpisode(
                DanmakuEpisodeMatchRequest(0),
                listOf(misleadingEpisode),
            ).matchedEpisodeOrNull(),
        )
        assertNull(
            DanmakuMatchPolicy.matchEpisode(
                DanmakuEpisodeMatchRequest(99),
                listOf(misleadingEpisode),
            ).matchedEpisodeOrNull(),
        )
    }

    private fun bangumi(id: Long = 10L, title: String) = DanmakuBangumi(
        remoteAnimeId = id,
        remoteBangumiId = id.toString(),
        title = title,
    )

    private fun episode(id: Long) = DanmakuEpisode(
        remoteEpisodeId = id,
        remoteAnimeId = 10L,
        remoteBangumiId = "10",
        bangumiTitle = "测试番剧",
        episodeTitle = "远端选集",
        episodeNumber = null,
    )
}
