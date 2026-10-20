package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.entity.Episode

/** One bounded attempt chain for a single episode. Manual selection/retry starts a new chain. */
internal class AutomaticLineFallback(private val maxAttempts: Int = 3) {
    data class Candidate(val line: PlayLineWrapper, val episode: Episode, val attempt: Int)

    private val attemptedLineIds = mutableSetOf<String>()
    private var originalEpisode: Episode? = null
    private var attempts = 0
    private var cancelled = false

    fun reset() {
        attemptedLineIds.clear()
        originalEpisode = null
        attempts = 0
        cancelled = false
    }

    fun cancel() {
        cancelled = true
    }

    fun next(
        failedLineId: String,
        failedEpisode: Episode,
        lines: List<PlayLineWrapper>,
    ): Candidate? {
        if (cancelled || attempts >= maxAttempts) return null
        attemptedLineIds += failedLineId
        val original = originalEpisode ?: failedEpisode.also { originalEpisode = it }
        val failedIndex = lines.indexOfFirst { it.playLine.id == failedLineId }
        val orderedLines = if (failedIndex < 0) lines else {
            lines.drop(failedIndex + 1) + lines.take(failedIndex)
        }
        for (line in orderedLines) {
            if (line.playLine.id in attemptedLineIds) continue
            // Use the source episode list, independent of the user's display sorting/filtering.
            val episode = matchEpisode(original, line.playLine.episode) ?: continue
            attemptedLineIds += line.playLine.id
            attempts++
            return Candidate(line, episode, attempts)
        }
        return null
    }

    companion object {
        private val whitespace = Regex("\\s+")
        private val episodeNumber = Regex("^(?:第|ep(?:isode)?\\.?)?0*(\\d+)(?:集|话|話|期)?$", RegexOption.IGNORE_CASE)

        internal fun requiresManualVerification(error: Throwable): Boolean {
            var current: Throwable? = error
            val visited = mutableSetOf<Throwable>()
            while (current != null && visited.add(current)) {
                if (current is PlayInfoNeedVerificationBusinessException) return true
                // ParserException keeps its business exception in cause as well as exception.
                current = current.cause
            }
            return false
        }

        /** Never use list indices: different lines can omit episodes or include trailers/specials. */
        internal fun matchEpisode(original: Episode, episodes: List<Episode>): Episode? {
            val label = original.label.replace(whitespace, "").lowercase()
            if (label.isNotEmpty()) {
                val labels = episodes.filter { it.label.replace(whitespace, "").lowercase() == label }
                if (labels.size == 1) return labels.single()
                if (labels.size > 1) return null
                val number = episodeNumber.matchEntire(label)?.groupValues?.get(1)?.toLongOrNull()
                if (number != null) {
                    val numbered = episodes.filter {
                        episodeNumber.matchEntire(it.label.replace(whitespace, ""))
                            ?.groupValues?.get(1)?.toLongOrNull() == number
                    }
                    if (numbered.size == 1) return numbered.single()
                    if (numbered.size > 1) return null
                }
            }
            // IDs may be shared across lines; conflicting nonblank labels are not equivalent.
            val byId = episodes.filter {
                original.id.isNotBlank() && it.id == original.id &&
                    (original.label.isBlank() || it.label.isBlank())
            }
            if (byId.size == 1) return byId.single()
            // Sources without episode labels can still declare a unique episode order.
            if (original.order < 0) return null
            return episodes.filter {
                it.order == original.order && (original.label.isBlank() || it.label.isBlank())
            }.singleOrNull()
        }
    }
}
