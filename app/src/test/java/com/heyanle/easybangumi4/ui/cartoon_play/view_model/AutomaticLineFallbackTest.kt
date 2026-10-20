package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class AutomaticLineFallbackTest {
    @Test
    fun verificationInsideSourceParserExceptionNeverTriggersFallback() {
        val verification = PlayInfoNeedVerificationBusinessException(
            request = null,
            verificationParam = VerificationParam.ImageCaptcha(image = "captcha"),
        )
        assertTrue(AutomaticLineFallback.requiresManualVerification(verification))
        assertTrue(AutomaticLineFallback.requiresManualVerification(ParserException("need web check", verification)))
        assertTrue(AutomaticLineFallback.requiresManualVerification(IllegalStateException(ParserException("need web check", verification))))
    }

    @Test
    fun ordinaryParseAndPlaybackErrorsCanFallback() {
        assertFalse(AutomaticLineFallback.requiresManualVerification(ParserException("parse failed")))
        assertFalse(AutomaticLineFallback.requiresManualVerification(IllegalStateException("network failed")))
    }

    @Test
    fun findsSameEpisodeAfterReorderingAndMissingEpisodes() {
        val original = Episode("a-3", "第 03 集", 2)
        val wanted = Episode("b-3", "EP03", 0)
        val line = line("b", Episode("b-4", "第4集", 1), wanted)

        val result = AutomaticLineFallback().next("a", original, listOf(line))!!

        assertSame(wanted, result.episode)
        assertEquals("b", result.line.playLine.id)
    }

    @Test
    fun doesNotPlayDifferentEpisodeAtSameIndexOrOrder() {
        assertNull(AutomaticLineFallback.matchEpisode(
            Episode("a-2", "第2集", 1),
            listOf(Episode("b-3", "第3集", 1)),
        ))
    }

    @Test
    fun specialEpisodeIsNotMatchedToRegularEpisodeWithSameNumber() {
        assertNull(AutomaticLineFallback.matchEpisode(
            Episode("a-ova", "OVA 1", 1),
            listOf(Episode("b-1", "第1集", 1)),
        ))
    }

    @Test
    fun ambiguousEpisodeLabelsAreSkipped() {
        assertNull(AutomaticLineFallback.matchEpisode(
            Episode("a", "第1集", 0),
            listOf(Episode("b", "第1集", 0), Episode("c", "第1集", 1)),
        ))
    }

    @Test
    fun unlabeledEpisodesRequireUniqueDeclaredOrder() {
        val expected = Episode("b", "", 5)
        assertSame(expected, AutomaticLineFallback.matchEpisode(
            Episode("a", "", 5), listOf(Episode("c", "", 1), expected),
        ))
        assertNull(AutomaticLineFallback.matchEpisode(
            Episode("a", "", 0), listOf(Episode("b", "", 0), Episode("c", "", 0)),
        ))
    }

    @Test
    fun attemptsAtMostThreeOtherLinesWithoutRevisiting() {
        val episode = Episode("episode", "第1集", 1)
        val lines = (0..5).map { line("line-$it", episode) }
        val policy = AutomaticLineFallback()

        assertEquals("line-1", policy.next("line-0", episode, lines)!!.line.playLine.id)
        assertEquals("line-2", policy.next("line-1", episode, lines)!!.line.playLine.id)
        val third = policy.next("line-2", episode, lines)!!
        assertEquals(3, third.attempt)
        assertEquals("line-3", third.line.playLine.id)
        assertNull(policy.next("line-3", episode, lines))
    }

    @Test
    fun exhaustedCandidatesNeverWrapBackToOriginalLine() {
        val episode = Episode("episode", "第1集", 1)
        val lines = listOf(line("a", episode), line("b", episode))
        val policy = AutomaticLineFallback()
        assertEquals("b", policy.next("a", episode, lines)!!.line.playLine.id)
        assertNull(policy.next("b", episode, lines))
    }

    @Test
    fun cancellingStopsFurtherAttemptsUntilExplicitReset() {
        val episode = Episode("episode", "第1集", 1)
        val lines = listOf(line("a", episode), line("b", episode), line("c", episode))
        val policy = AutomaticLineFallback()
        policy.next("a", episode, lines)
        policy.cancel()
        assertNull(policy.next("b", episode, lines))
        policy.reset()
        assertEquals(1, policy.next("b", episode, lines)!!.attempt)
    }

    private fun line(id: String, vararg episodes: Episode) = PlayLineWrapper(
        PlayLine(id, id, arrayListOf(*episodes)),
        comparator = compareBy { it.order },
    )
}
