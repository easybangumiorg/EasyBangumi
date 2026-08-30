package com.heyanle.easybangumi4.danmaku

import java.text.Normalizer
import java.util.Locale

/**
 * Pure, deterministic policies for the two progressive matching stages.
 *
 * Bangumi matching only decides which remote bangumi owns the episode list. Episode matching is
 * deliberately independent of local Episode metadata and maps the current sorted position to the
 * same position in the remote list.
 */
internal object DanmakuMatchPolicy {
    const val TITLE_SIMILARITY_THRESHOLD = 0.80

    suspend fun matchBangumi(
        title: String,
        searchBangumi: suspend (String) -> DanmakuResult<List<DanmakuBangumi>>,
    ): DanmakuResult<DanmakuBangumiMatch> {
        return when (val result = searchBangumi(title)) {
            is DanmakuResult.Success -> {
                val first = result.value.firstOrNull()
                DanmakuResult.Success(
                    value = DanmakuBangumiMatch(
                        candidates = result.value,
                        matchedBangumi = first?.takeIf {
                            titleSimilarity(title, it.title) > TITLE_SIMILARITY_THRESHOLD
                        },
                    ),
                    fromCache = result.fromCache,
                )
            }

            DanmakuResult.CredentialsMissing -> DanmakuResult.CredentialsMissing
            is DanmakuResult.InvalidResponse -> result
            is DanmakuResult.Unavailable -> result
            DanmakuResult.Stale -> DanmakuResult.Stale
        }
    }

    fun matchEpisode(
        request: DanmakuEpisodeMatchRequest,
        episodes: List<DanmakuEpisode>,
    ): DanmakuEpisodeMatch {
        val position = request.sortedEpisodePosition
        return DanmakuEpisodeMatch(
            episode = position.takeIf { it > 0 }?.let { episodes.getOrNull(it - 1) },
            sortedEpisodePosition = position,
        )
    }

    /**
     * Matches the request position shifted by a manually recorded offset.
     *
     * [episodeOffset] = remote episode position - local playback position, captured when the user
     * hand-picks an episode (playing a, picked b -> b - a). Shifting the current position by it
     * replays the same choice on later episodes (a + n -> b + n). An out-of-range shifted position
     * yields episode = null so callers can fall back to [matchEpisode].
     */
    fun matchEpisodeWithOffset(
        request: DanmakuEpisodeMatchRequest,
        episodes: List<DanmakuEpisode>,
        episodeOffset: Int,
    ): DanmakuEpisodeMatch {
        val shiftedPosition = request.sortedEpisodePosition + episodeOffset
        return DanmakuEpisodeMatch(
            episode = shiftedPosition.takeIf { it > 0 }?.let { episodes.getOrNull(it - 1) },
            sortedEpisodePosition = request.sortedEpisodePosition,
        )
    }

    fun titleSimilarity(first: String, second: String): Double {
        val normalizedFirst = normalizeTitle(first)
        val normalizedSecond = normalizeTitle(second)
        if (normalizedFirst == normalizedSecond) return 1.0
        val maxLength = maxOf(normalizedFirst.length, normalizedSecond.length)
        if (maxLength == 0) return 1.0
        return 1.0 - levenshteinDistance(normalizedFirst, normalizedSecond).toDouble() / maxLength
    }

    private fun normalizeTitle(value: String): String {
        val compatible = Normalizer.normalize(value, Normalizer.Form.NFKC)
            .lowercase(Locale.ROOT)
        return buildString(compatible.length) {
            compatible.forEach { character ->
                if (character.isLetterOrDigit()) append(character)
            }
        }
    }

    private fun levenshteinDistance(first: String, second: String): Int {
        if (first == second) return 0
        if (first.isEmpty()) return second.length
        if (second.isEmpty()) return first.length

        var previous = IntArray(second.length + 1) { it }
        var current = IntArray(second.length + 1)
        first.forEachIndexed { firstIndex, firstCharacter ->
            current[0] = firstIndex + 1
            second.forEachIndexed { secondIndex, secondCharacter ->
                val substitutionCost = if (firstCharacter == secondCharacter) 0 else 1
                current[secondIndex + 1] = minOf(
                    current[secondIndex] + 1,
                    previous[secondIndex + 1] + 1,
                    previous[secondIndex] + substitutionCost,
                )
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[second.length]
    }
}
