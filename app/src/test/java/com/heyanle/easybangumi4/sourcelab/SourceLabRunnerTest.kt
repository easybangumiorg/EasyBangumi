package com.heyanle.easybangumi4.sourcelab

import androidx.media3.common.VideoSize
import com.google.gson.JsonParser
import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.SearchVerificationRequest
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoVerificationRequest
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import android.webkit.WebView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import org.mozilla.javascript.WrappedException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass

class SourceLabRunnerTest {
    @Test
    fun diagnosticsClassifiesWrappedNetworkAndParserFailuresSeparately() = runBlocking {
        for ((error, expected) in listOf(
            ParserException("wrapper", java.net.UnknownHostException("private-host")) to LabFailureKind.NETWORK,
            ParserException("invalid response", IllegalStateException("invalid structure")) to LabFailureKind.PARSING,
        )) {
            val access = FakeAccess().apply { searchError = InvocationTargetException(error) }
            val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
            assertEquals(expected, result.step(LabStep.SEARCH)?.failureKind)
            assertEquals(expected, result.searchAttempts.single().failureKind)
        }
    }

    @Test
    fun firstNonEmptyAliasUsesFirstResultOnceAndTriesNextLine() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS[1]] = listOf(hit("first", "第一部"), hit("second", "孤獨搖滾"))
            failedLines = setOf("line-1")
            lines = listOf(line("line-1"), line("line-2", Episode("ep-2", "第2集", 2), Episode("ep-1", "第1集", 1)))
        }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(LabConfig.DEFAULT_KEYWORDS.take(2), access.queries)
        assertEquals(listOf(7, 7), access.searchKeys)
        assertEquals(1, access.homeLoads)
        assertEquals("first", access.detailedId)
        assertEquals("first", result.selection?.cartoonId)
        assertEquals(0, result.selection?.resultIndex)
        assertEquals(listOf(LabLineStatus.ERROR, LabLineStatus.PASS), result.lineAttempts.map { it.status })
        assertEquals("ep-1", result.lineAttempts.last().episodeId)
        assertEquals(listOf(false, false), access.canCacheValues)
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
        assertEquals(LabVerdict.PASS, result.verdict)
    }

    @Test
    fun allEmptyAliasesFallbackToFirstHomeItemWithoutRefetching() = runBlocking {
        val access = FakeAccess()
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(LabConfig.DEFAULT_KEYWORDS, access.queries)
        assertEquals(1, access.homeLoads)
        assertEquals("home-first", access.detailedId)
        assertEquals(LabSelectionRoute.HOME_FALLBACK, result.selection?.route)
        assertEquals("推荐", result.selection?.homePageLabel)
        assertEquals(LabAcceptance.HOME_PLAYABLE_SEARCH_EMPTY, result.acceptance)
        assertEquals(LabVerdict.SUSPECT, result.verdict)
        assertEquals(LabVerdict.SUSPECT, result.step(LabStep.SEARCH)?.verdict)
        assertTrue(result.searchAttempts.all { it.status == LabSearchStatus.EMPTY })
    }

    @Test
    fun searchInfrastructureFailureStopsAliasLoopAndCannotEnterRelease() = runBlocking {
        val access = FakeAccess().apply { searchError = SocketTimeoutException("connect timed out") }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(listOf(LabConfig.DEFAULT_KEYWORDS.first()), access.queries)
        assertEquals(LabSearchStatus.TIMEOUT, result.searchAttempts.single().status)
        assertEquals(LabVerdict.PASS, result.step(LabStep.PLAY)?.verdict)
        assertEquals(LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE, result.acceptance)
        assertEquals(LabVerdict.FAIL, result.verdict)
    }

    @Test
    fun captchaIsManualReviewAndStopsRepeatedAliases() = runBlocking {
        val access = FakeAccess().apply { searchError = searchChallenge() }
        val result = SourceLabRunner(access, failureProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(1, access.queries.size)
        assertEquals(LabSearchStatus.VERIFICATION_REQUIRED, result.searchAttempts.single().status)
        assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
    }

    @Test
    fun playbackFailureDoesNotChangeTitleOrUseHomepage() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("search-first"), hit("search-second"))
            failedLines = setOf("line-1", "line-2")
            lines = listOf(line("line-1"), line("line-2"))
        }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(1, access.queries.size)
        assertEquals("search-first", access.detailedId)
        assertEquals(LabSelectionRoute.SEARCH_RESULT, result.selection?.route)
        assertEquals(2, result.lineAttempts.size)
        assertEquals(LabAcceptance.PLAYBACK_FAILED, result.acceptance)
        assertTrue(result.step(LabStep.PLAY)?.detail?.contains("全部 2 条") == true)
    }

    @Test
    fun firstFrameAloneIsNotPlaybackPass() = runBlocking {
        val access = FakeAccess().apply { searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first")) }
        val probe = object : LabPlaybackProbe {
            override suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long) =
                ProbeOutcome(false, true, true, VideoSize(1280, 720), 99_000, "stalled", progressMs = 500)
        }
        val result = SourceLabRunner(access, probe).runSource(SOURCE.key, LabConfig())
        assertEquals(LabLineStatus.NO_PROGRESS, result.lineAttempts.single().status)
        assertEquals(LabVerdict.FAIL, result.step(LabStep.PLAY)?.verdict)
    }

    @Test
    fun sourceWithoutSearchCanOnlyProduceDiagnosticPlayback() = runBlocking {
        val access = FakeAccess().apply { searchAvailable = false }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(LabSearchStatus.UNAVAILABLE, result.searchAttempts.single().status)
        assertEquals(LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE, result.acceptance)
        assertEquals(LabVerdict.FAIL, result.verdict)
    }

    @Test
    fun addressParsingCanOutlastOrdinaryStepBudgetWithinItsOwnBudget() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            playInfoDelayMs = 120
        }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key,
            LabConfig(stepTimeoutMs = 50, playInfoTimeoutMs = 500, playBudgetMs = 1_500))
        assertEquals(1, access.completedPlayInfos)
        assertEquals(LabVerdict.PASS, result.step(LabStep.HOME)?.verdict)
        assertEquals(LabVerdict.PASS, result.step(LabStep.SEARCH)?.verdict)
        assertEquals(LabLineStatus.PASS, result.lineAttempts.single().status)
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
    }

    @Test
    fun addressParsingStillFailsAtItsIndependentTimeout() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            playInfoDelayMs = 500
        }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key,
            LabConfig(stepTimeoutMs = 1_000, playInfoTimeoutMs = 30, playBudgetMs = 1_500))
        assertEquals(0, access.completedPlayInfos)
        assertEquals(LabLineStatus.TIMEOUT, result.lineAttempts.single().status)
        assertEquals(LabVerdict.FAIL, result.step(LabStep.PLAY)?.verdict)
        assertEquals(LabAcceptance.PLAYBACK_FAILED, result.acceptance)
        assertFalse(result.playBudgetExhausted)
    }

    @Test
    fun remainingTotalBudgetLimitsAddressParsingAndLeavesLaterLinesUntested() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            playInfoDelayMs = 500
            lines = listOf(line("line-1"), line("line-2"))
        }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key,
            LabConfig(stepTimeoutMs = 1_000, playInfoTimeoutMs = 1_000, playBudgetMs = 30))
        assertEquals(0, access.completedPlayInfos)
        assertEquals(1, access.canCacheValues.size)
        assertEquals(LabLineStatus.TIMEOUT, result.lineAttempts.single().status)
        assertTrue(result.playBudgetExhausted)
        assertEquals(LabAcceptance.INCOMPLETE, result.acceptance)
    }

    @Test
    fun exhaustedBudgetDoesNotClaimAllLinesWereTested() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            lines = listOf(line("line-1"), line("line-2"))
        }
        val slowProbe = object : LabPlaybackProbe {
            override suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long): ProbeOutcome {
                delay(25)
                return ProbeOutcome(true, false, false, null, 0, "failed")
            }
        }
        val result = SourceLabRunner(access, slowProbe).runSource(SOURCE.key, LabConfig(playBudgetMs = 1))
        assertTrue(result.playBudgetExhausted)
        assertTrue(result.lineAttempts.size < 2)
        assertEquals(LabAcceptance.INCOMPLETE, result.acceptance)
        assertTrue(result.step(LabStep.PLAY)?.detail?.contains("剩余线路未验证") == true)
    }

    @Test
    fun callerCancellationEscapesRunnerRatherThanBecomingAResult() = runBlocking {
        val access = FakeAccess().apply { searchDelayMs = 10_000 }
        var cancelled = false
        try {
            withTimeout(80) { SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig()) }
        } catch (_: CancellationException) { cancelled = true }
        assertTrue(cancelled)
    }

    @Test
    fun partialTestRunCannotBecomeAnAdmissionCandidate() = runBlocking {
        val access = FakeAccess().apply { searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first")) }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig(steps = setOf(LabStep.PLAY)))
        assertEquals(LabVerdict.PASS, result.step(LabStep.PLAY)?.verdict)
        assertEquals(LabAcceptance.INCOMPLETE, result.acceptance)
    }

    @Test
    fun reportIncludesStableAdmissionFieldsAndNoMediaAddressOrHeaders() = runBlocking {
        val access = FakeAccess().apply { searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first")) }
        val config = LabConfig()
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, config)
        val json = LabRun(config, items = listOf(result), total = 1).toEvidenceJson()
        val document = JsonParser.parseString(json).asJsonObject
        assertEquals(1, document.get("schemaVersion").asInt)
        assertEquals("source-acceptance-v1", document.get("protocolVersion").asString)
        val source = document.getAsJsonArray("results").first().asJsonObject
        assertEquals("SEARCH_PLAYABLE", source.get("acceptance").asString)
        assertEquals("PASS", source.get("verdict").asString)
        assertTrue(source.get("startedAt").asString.endsWith("Z"))
        assertEquals(3_200, source.getAsJsonArray("lineAttempts").first().asJsonObject.get("progressMs").asInt)
        assertFalse(json.contains("cdn.example"))
        assertFalse(json.contains("Cookie"))
    }

    @Test
    fun submittedCaptchaRetriesSameKeywordAndPageThenKeepsOneLogicalAttempt() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                if (verification == null) SourceResult.Error(searchChallenge(page, keyword))
                else {
                    assertEquals("correct-private-answer", (verification as VerificationResult.ImageCaptcha).input)
                    SourceResult.Complete(null to listOf(hit("first"), hit("second")))
                }
            }
        }
        val requests = ArrayList<LabVerificationRequest>()
        val handler = LabVerificationHandler { requests.add(it); LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("correct-private-answer")) }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = handler).runSource(SOURCE.key, LabConfig())
        assertEquals(listOf(LabConfig.DEFAULT_KEYWORDS.first(), LabConfig.DEFAULT_KEYWORDS.first()), access.queries)
        assertEquals(listOf(7, 7), access.searchKeys)
        assertEquals(1, result.searchAttempts.size)
        assertEquals(7, result.searchAttempts.single().pageKey)
        assertEquals("first", result.selection?.cartoonId)
        assertEquals(1, requests.single().attempt)
        assertEquals(7, requests.single().searchPageKey)
        assertEquals(LabVerificationRetryOutcome.SUCCEEDED, result.verificationEvents.single().retryOutcome)
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
    }

    @Test
    fun wrongCaptchaRequestsAnotherSubmissionWithoutChangingSample() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                if ((verification as? VerificationResult.ImageCaptcha)?.input != "right") SourceResult.Error(searchChallenge(page, keyword))
                else SourceResult.Complete(null to listOf(hit("first")))
            }
        }
        val requests = ArrayList<LabVerificationRequest>()
        val handler = LabVerificationHandler {
            requests.add(it)
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha(if (it.attempt == 1) "wrong" else "right"))
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = handler).runSource(SOURCE.key, LabConfig())
        assertEquals(listOf(1, 2), requests.map { it.attempt })
        assertEquals(1, access.queries.distinct().size)
        assertEquals(1, result.searchAttempts.size)
        assertEquals(listOf(LabVerificationRetryOutcome.VERIFICATION_REQUIRED, LabVerificationRetryOutcome.SUCCEEDED), result.verificationEvents.map { it.retryOutcome })
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
    }

    @Test
    fun verificationSubmissionLimitStopsCurrentSource() = runBlocking {
        val access = FakeAccess().apply { searchError = searchChallenge() }
        var submissions = 0
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            submissions++
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("wrong"))
        }).runSource(SOURCE.key, LabConfig())
        assertEquals(3, submissions)
        assertEquals(4, access.queries.size)
        assertEquals(listOf(1, 2, 3, 4), result.verificationEvents.map { it.attempt })
        assertEquals(LabVerificationDecision.ATTEMPT_LIMIT, result.verificationEvents.last().decision)
        assertEquals(LabSearchStatus.VERIFICATION_REQUIRED, result.searchAttempts.single().status)
        assertEquals(0, access.completedPlayInfos)
        assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
    }

    @Test
    fun skippedExpiredAndUnavailableRemainDistinctAndStopBeforePlayback() = runBlocking {
        for ((response, decision) in listOf(
            LabVerificationResponse.Skipped to LabVerificationDecision.SKIPPED,
            LabVerificationResponse.Expired to LabVerificationDecision.EXPIRED,
            LabVerificationResponse.Unavailable to LabVerificationDecision.UNAVAILABLE,
        )) {
            val access = FakeAccess().apply { searchError = searchChallenge() }
            val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler { response })
                .runSource(SOURCE.key, LabConfig())
            assertEquals(decision, result.verificationEvents.single().decision)
            assertEquals(LabVerdict.SKIP, result.step(LabStep.PLAY)?.verdict)
            assertEquals(0, access.completedPlayInfos)
            assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        }
    }

    @Test
    fun cancellingVerificationReturnsPartialEvidenceAndPropagatesCancellation() = runBlocking {
        val access = FakeAccess().apply { searchError = searchChallenge() }
        var partial: LabSourceResult? = null
        try {
            SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler { LabVerificationResponse.Cancelled })
                .runSource(SOURCE.key, LabConfig())
        } catch (cancelled: LabVerificationCancelledException) { partial = cancelled.partialResult }
        val result = requireNotNull(partial)
        assertEquals(LabVerificationDecision.CANCELLED, result.verificationEvents.single().decision)
        assertEquals(LabSearchStatus.VERIFICATION_REQUIRED, result.searchAttempts.single().status)
        assertEquals(LabVerdict.PASS, result.step(LabStep.HOME)?.verdict)
        assertEquals(LabVerdict.SUSPECT, result.step(LabStep.SEARCH)?.verdict)
        assertEquals(LabVerdict.SKIP, result.step(LabStep.PLAY)?.verdict)
        assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        assertEquals(0, access.completedPlayInfos)
    }

    @Test(timeout = 5_000)
    fun externalCancellationWhileWaitingKeepsCancelledEvidence() = runBlocking {
        val access = FakeAccess().apply { searchError = searchChallenge() }
        val waiting = CompletableDeferred<Unit>()
        val partial = CompletableDeferred<LabSourceResult>()
        val worker = async {
            try {
                SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
                    waiting.complete(Unit)
                    delay(10_000)
                    LabVerificationResponse.Skipped
                }).runSource(SOURCE.key, LabConfig())
            } catch (cancelled: LabVerificationCancelledException) {
                partial.complete(cancelled.partialResult)
                throw cancelled
            }
        }
        withTimeout(3_000) { waiting.await() }
        worker.cancel()
        withTimeout(1_000) { worker.join() }
        assertEquals(LabVerificationDecision.CANCELLED, withTimeout(1_000) { partial.await() }.verificationEvents.single().decision)
    }

    @Test
    fun mismatchingSearchSourceKeywordOrPageNeverReachesHumanHandler() = runBlocking {
        for (request in listOf(
            SearchVerificationRequest(7, LabConfig.DEFAULT_KEYWORDS.first(), "other.source"),
            SearchVerificationRequest(7, "different title", SOURCE.key),
            SearchVerificationRequest(8, LabConfig.DEFAULT_KEYWORDS.first(), SOURCE.key),
        )) {
            val access = FakeAccess().apply { searchError = SearchNeedVerificationBusinessException(request, imageParam()) }
            var prompts = 0
            val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
                prompts++; LabVerificationResponse.Skipped
            }).runSource(SOURCE.key, LabConfig())
            assertEquals(0, prompts)
            assertEquals(LabVerificationDecision.REQUEST_MISMATCH, result.verificationEvents.single().decision)
            assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        }
    }

    @Test
    fun playVerificationRetriesFixedSummaryLineAndFirstOrderedEpisode() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"), hit("second"))
            lines = listOf(line("line-1", Episode("ep-2", "第2集", 2), Episode("ep-1", "第1集", 1)), line("line-2"))
            playReply = { summary, line, episode, verification ->
                if (verification == null) SourceResult.Error(playChallenge(summary, line, episode))
                else SourceResult.Complete(PlayerInfo(uri = "https://cdn.example.test/play.m3u8"))
            }
        }
        val requests = ArrayList<LabVerificationRequest>()
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            requests.add(it); LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
        }).runSource(SOURCE.key, LabConfig())
        assertEquals(1, requests.size)
        assertEquals("first", requests.single().cartoonId)
        assertEquals("line-1", requests.single().lineId)
        assertEquals("ep-1", requests.single().episodeId)
        assertEquals(0, requests.single().lineIndex)
        assertEquals(listOf("first/line-1/ep-1", "first/line-1/ep-1"), access.playSamples)
        assertEquals(listOf(false, false), access.canCacheValues)
        assertEquals(1, result.lineAttempts.size)
        assertEquals(LabLineStatus.PASS, result.lineAttempts.single().status)
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
    }

    @Test
    fun mismatchedPlayRequestCannotBeBypassedByAnotherWorkingLine() = runBlocking {
        for (variant in 0..4) {
            val access = FakeAccess().apply {
                searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
                lines = listOf(line("line-1"), line("line-2"))
                playReply = { summary, line, episode, _ ->
                    val request = when (variant) {
                        0 -> PlayInfoVerificationRequest(summary.copy(source = "other.source"), line, episode)
                        1 -> PlayInfoVerificationRequest(summary.copy(id = "other.title"), line, episode)
                        2 -> PlayInfoVerificationRequest(summary, PlayLine("foreign-line", "foreign-line", arrayListOf(episode)), episode)
                        3 -> PlayInfoVerificationRequest(summary, line, Episode("other-episode", "第2集", 2))
                        else -> null
                    }
                    SourceResult.Error(PlayInfoNeedVerificationBusinessException(request, imageParam()))
                }
            }
            var prompts = 0
            val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
                prompts++; LabVerificationResponse.Skipped
            }).runSource(SOURCE.key, LabConfig())
            assertEquals(0, prompts)
            assertEquals(1, access.playSamples.size)
            assertEquals(LabVerificationDecision.REQUEST_MISMATCH, result.verificationEvents.single().decision)
            assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        }
    }

    @Test
    fun manualWaitingIsExcludedFromSearchAndTotalPlayBudgets() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                if (verification == null) SourceResult.Error(searchChallenge(page, keyword))
                else SourceResult.Complete(null to listOf(hit("first")))
            }
            playReply = { summary, line, episode, verification ->
                if (verification == null) SourceResult.Error(playChallenge(summary, line, episode))
                else SourceResult.Complete(PlayerInfo(uri = "https://cdn.example.test/play.m3u8"))
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            delay(180)
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
        }).runSource(SOURCE.key, LabConfig(stepTimeoutMs = 100, playInfoTimeoutMs = 100, playTimeoutMs = 100, playBudgetMs = 100))
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
        assertTrue(result.manualWaitMs >= 350)
        assertTrue(result.step(LabStep.SEARCH)!!.elapsedMs < 100)
        assertTrue(result.lineAttempts.single().elapsedMs < 100)
        assertEquals(result.manualWaitMs, result.verificationEvents.sumOf { it.waitMs })
        assertFalse(result.playBudgetExhausted)
    }

    @Test
    fun retryNetworkWorkStillConsumesItsActiveBudget() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                delay(if (verification == null) 90 else 220)
                if (verification == null) SourceResult.Error(searchChallenge(page, keyword))
                else SourceResult.Complete(null to listOf(hit("first")))
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            delay(220)
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
        }).runSource(SOURCE.key, LabConfig(stepTimeoutMs = 180))
        assertEquals(LabSearchStatus.TIMEOUT, result.searchAttempts.single().status)
        assertEquals(LabVerificationRetryOutcome.TIMEOUT, result.verificationEvents.single().retryOutcome)
        assertTrue(result.manualWaitMs >= 200)
        assertTrue(result.searchAttempts.single().elapsedMs >= 170)
        assertEquals(LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE, result.acceptance)
    }

    @Test
    fun wrappedWebChallengeKeepsOriginalProxyAndRejectsForeignProxy() = runBlocking {
        val proxy = FakeWebProxy()
        for (sameProxy in listOf(true, false)) {
            val access = FakeAccess().apply {
                searchReply = { _, _, verification ->
                    if (verification == null) SourceResult.Error(InvocationTargetException(ParserException("wrapper",
                        WrappedException(NeedWebViewCheckExceptionInner(proxy, "private-Cookie-hint")))))
                    else SourceResult.Complete(null to listOf(hit("first")))
                }
            }
            val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
                assertTrue((it.verificationParam as VerificationParam.WebView).iWebProxy === proxy)
                LabVerificationResponse.Completed(VerificationResult.WebView(if (sameProxy) proxy else FakeWebProxy()))
            }).runSource(SOURCE.key, LabConfig())
            assertEquals(if (sameProxy) LabAcceptance.SEARCH_PLAYABLE else LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
            assertEquals(if (sameProxy) LabVerificationDecision.COMPLETED else LabVerificationDecision.REQUEST_MISMATCH,
                result.verificationEvents.single().decision)
        }
    }

    @Test
    fun unrelatedClassNameIsNotTreatedAsCaptchaProtocol() = runBlocking {
        val access = FakeAccess().apply { searchError = CaptchaRequiredException() }
        var prompts = 0
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            prompts++; LabVerificationResponse.Skipped
        }).runSource(SOURCE.key, LabConfig())
        assertEquals(0, prompts)
        assertEquals(LabSearchStatus.ERROR, result.searchAttempts.single().status)
        assertTrue(result.verificationEvents.isEmpty())
    }

    @Test
    fun noHandlerPreservesDiagnosticPlaybackButNeverBypassesUnresolvedCaptcha() = runBlocking {
        val access = FakeAccess().apply { searchError = searchChallenge() }
        val result = SourceLabRunner(access, successProbe()).runSource(SOURCE.key, LabConfig())
        assertEquals(LabVerdict.PASS, result.step(LabStep.PLAY)?.verdict)
        assertEquals(LabVerificationDecision.UNAVAILABLE, result.verificationEvents.single().decision)
        assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
    }

    @Test
    fun exportedVerificationEvidenceContainsNoAnswerImageAddressCookiesOrProxy() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                if (verification == null) SourceResult.Error(searchChallenge(page, keyword))
                else SourceResult.Complete(null to listOf(hit("first")))
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("PRIVATE_CAPTCHA_ANSWER"))
        }).runSource(SOURCE.key, LabConfig())
        val report = LabRun(LabConfig(), listOf(result), 1)
        val json = report.toEvidenceJson()
        val markdown = report.toMarkdown()
        for (secret in listOf("PRIVATE_CAPTCHA_ANSWER", "SECRET_IMAGE_ADDRESS", "PRIVATE_CAPTCHA_HINT", "private-session", "iWebProxy", "verificationParam")) {
            assertFalse(secret, json.contains(secret))
            assertFalse(secret, markdown.contains(secret))
        }
        assertTrue(json.contains("verificationEvents"))
        assertTrue(json.contains("SUCCEEDED"))
    }

    @Test
    fun unsupportedHomeAndDetailVerificationCannotProduceAdmission() = runBlocking {
        for (duringHome in listOf(true, false)) {
            val access = FakeAccess().apply {
                searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
                if (duringHome) homeError = searchChallenge() else detailedError = searchChallenge()
            }
            var prompts = 0
            val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
                prompts++; LabVerificationResponse.Skipped
            }).runSource(SOURCE.key, LabConfig())
            assertEquals(0, prompts)
            assertEquals(LabVerificationDecision.UNAVAILABLE, result.verificationEvents.single().decision)
            assertEquals(0, access.completedPlayInfos)
            assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        }
    }

    @Test
    fun cancellationDuringPlayKeepsTheFixedLogicalLineEvidence() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            playReply = { summary, line, episode, _ -> SourceResult.Error(playChallenge(summary, line, episode)) }
        }
        var partial: LabSourceResult? = null
        try {
            SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler { LabVerificationResponse.Cancelled })
                .runSource(SOURCE.key, LabConfig())
        } catch (cancelled: LabVerificationCancelledException) { partial = cancelled.partialResult }
        val result = requireNotNull(partial)
        assertEquals(LabVerificationDecision.CANCELLED, result.verificationEvents.single().decision)
        assertEquals("line-1", result.lineAttempts.single().lineId)
        assertEquals("ep-1", result.lineAttempts.single().episodeId)
        assertEquals(LabLineStatus.VERIFICATION_REQUIRED, result.lineAttempts.single().status)
        assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
    }

    @Test
    fun parsingRetriesConsumeTotalActivePlaybackBudgetAfterHumanWait() = runBlocking {
        val access = FakeAccess().apply {
            searchResults[LabConfig.DEFAULT_KEYWORDS.first()] = listOf(hit("first"))
            lines = listOf(line("line-1"), line("line-2"))
            playReply = { summary, line, episode, verification ->
                delay(if (verification == null) 70 else 180)
                if (verification == null) SourceResult.Error(playChallenge(summary, line, episode))
                else SourceResult.Complete(PlayerInfo(uri = "https://cdn.example.test/play.m3u8"))
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            delay(200)
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
        }).runSource(SOURCE.key, LabConfig(playInfoTimeoutMs = 300, playBudgetMs = 150))
        assertEquals(1, result.lineAttempts.size)
        assertEquals(LabLineStatus.TIMEOUT, result.lineAttempts.single().status)
        assertEquals(LabVerificationRetryOutcome.TIMEOUT, result.verificationEvents.single().retryOutcome)
        assertTrue(result.playBudgetExhausted)
        assertTrue(result.manualWaitMs >= 180)
        assertEquals(LabAcceptance.INCOMPLETE, result.acceptance)
    }

    @Test
    fun genuinelyEmptyVerifiedSearchCanContinueOrderedAliasFallback() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                when {
                    keyword != LabConfig.DEFAULT_KEYWORDS.first() -> SourceResult.Complete(null to listOf(hit("first")))
                    verification == null -> SourceResult.Error(searchChallenge(page, keyword))
                    else -> SourceResult.Complete(null to emptyList())
                }
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
        }).runSource(SOURCE.key, LabConfig())
        assertEquals(listOf(LabSearchStatus.EMPTY, LabSearchStatus.RESULTS), result.searchAttempts.map { it.status })
        assertEquals(LabConfig.DEFAULT_KEYWORDS[1], result.selection?.keyword)
        assertEquals(LabVerificationRetryOutcome.SUCCEEDED, result.verificationEvents.single().retryOutcome)
        assertEquals(LabAcceptance.SEARCH_PLAYABLE, result.acceptance)
    }

    @Test
    fun verificationRetryFailureDoesNotExportEchoedAnswersOrCookies() = runBlocking {
        val access = FakeAccess().apply {
            searchReply = { page, keyword, verification ->
                if (verification == null) SourceResult.Error(searchChallenge(page, keyword))
                else SourceResult.Error(IllegalStateException("PRIVATE_CAPTCHA_ANSWER; Cookie=PRIVATE_SESSION"))
            }
        }
        val result = SourceLabRunner(access, successProbe(), verificationHandler = LabVerificationHandler {
            LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("PRIVATE_CAPTCHA_ANSWER"))
        }).runSource(SOURCE.key, LabConfig())
        val json = LabRun(LabConfig(), listOf(result), 1).toEvidenceJson()
        assertEquals(LabSearchStatus.ERROR, result.searchAttempts.single().status)
        assertEquals(LabVerificationRetryOutcome.ERROR, result.verificationEvents.single().retryOutcome)
        assertFalse(json.contains("PRIVATE_CAPTCHA_ANSWER"))
        assertFalse(json.contains("PRIVATE_SESSION"))
        assertFalse(json.contains("Cookie"))
    }

    @Test
    fun noHandlerClosesUnclaimedWebProxyAndImageResponseCannotSatisfyWebChallenge() = runBlocking {
        for (noHandler in listOf(true, false)) {
            val proxy = FakeWebProxy()
            val access = FakeAccess().apply { searchError = NeedWebViewCheckExceptionInner(proxy) }
            val result = SourceLabRunner(access, successProbe(), verificationHandler = if (noHandler) null else LabVerificationHandler {
                LabVerificationResponse.Completed(VerificationResult.ImageCaptcha("answer"))
            }).runSource(SOURCE.key, LabConfig())
            assertTrue(proxy.closed)
            assertEquals(if (noHandler) LabVerificationDecision.UNAVAILABLE else LabVerificationDecision.REQUEST_MISMATCH,
                result.verificationEvents.single().decision)
            assertEquals(LabAcceptance.MANUAL_VERIFICATION_REQUIRED, result.acceptance)
        }
    }

    private class CaptchaRequiredException : RuntimeException("verification needed")
    private fun successProbe() = object : LabPlaybackProbe {
        override suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long) =
            ProbeOutcome(false, true, true, VideoSize(1280, 720), 3_300, "first frame and progress", progressMs = 3_200)
    }
    private fun failureProbe() = object : LabPlaybackProbe {
        override suspend fun probe(info: PlayerInfo, timeoutMs: Long, minimumProgressMs: Long) =
            ProbeOutcome(true, false, false, null, 0, "decoder failed")
    }

    private class FakeAccess : LabSourceAccess {
        var homeLoads = 0
        var searchAvailable = true
        var homeError: Exception? = null
        var detailedError: Exception? = null
        var searchError: Exception? = null
        var searchDelayMs = 0L
        var playInfoDelayMs = 0L
        var completedPlayInfos = 0
        var detailedId = ""
        var lines = listOf(line("line-1"))
        var failedLines = emptySet<String>()
        var searchReply: (suspend (Int, String, VerificationResult?) -> SourceResult<Pair<Int?, List<CartoonCover>>>)? = null
        var playReply: (suspend (CartoonSummary, PlayLine, Episode, VerificationResult?) -> SourceResult<PlayerInfo>)? = null
        val queries = mutableListOf<String>()
        val searchKeys = mutableListOf<Int>()
        val searchResults = mutableMapOf<String, List<CartoonCover>>()
        val canCacheValues = mutableListOf<Boolean>()
        val playSamples = mutableListOf<String>()
        override fun source(key: String) = SOURCE
        override fun page(key: String) = object : PageComponent {
            override val source = SOURCE
            override fun getPages() = listOf(SourcePage.SingleCartoonPage.WithCover("推荐", { 11 }, { firstKey ->
                assertEquals(11, firstKey)
                homeLoads++
                homeError?.let { SourceResult.Error(it) }
                    ?: SourceResult.Complete(null to listOf(hit("home-first"), hit("home-second")))
            }))
        }
        override fun search(key: String): SearchComponent? = if (!searchAvailable) null else object : SearchComponent {
            override val source = SOURCE
            override fun getFirstSearchKey(keyword: String) = 7
            override suspend fun search(pageKey: Int, keyword: String) = reply(pageKey, keyword, null)
            override suspend fun search(pageKey: Int, keyword: String, verificationResult: VerificationResult) = reply(pageKey, keyword, verificationResult)
            private suspend fun reply(pageKey: Int, keyword: String, verification: VerificationResult?): SourceResult<Pair<Int?, List<CartoonCover>>> {
                queries.add(keyword)
                searchKeys.add(pageKey)
                if (searchDelayMs > 0) delay(searchDelayMs)
                searchReply?.let { return it(pageKey, keyword, verification) }
                searchError?.let { return SourceResult.Error(it) }
                return SourceResult.Complete(null to searchResults[keyword].orEmpty())
            }
        }
        override fun detailed(key: String) = object : DetailedComponent {
            override val source = SOURCE
            override suspend fun getDetailed(summary: CartoonSummary): SourceResult<Cartoon> = SourceResult.Complete(cartoon(summary))
            override suspend fun getPlayLine(summary: CartoonSummary): SourceResult<List<PlayLine>> = SourceResult.Complete(lines)
            override suspend fun getAll(summary: CartoonSummary): SourceResult<Pair<Cartoon, List<PlayLine>>> {
                detailedId = summary.id
                detailedError?.let { return SourceResult.Error(it) }
                return SourceResult.Complete(cartoon(summary) to lines)
            }
            private fun cartoon(summary: CartoonSummary) = CartoonImpl(summary.id, summary.source, "", "title")
        }
        override fun play(key: String) = object : PlayComponent {
            override val source = SOURCE
            override suspend fun getPlayInfo(summary: CartoonSummary, playLine: PlayLine, episode: Episode, canCache: Boolean) =
                reply(summary, playLine, episode, null, canCache)
            override suspend fun getPlayInfo(summary: CartoonSummary, playLine: PlayLine, episode: Episode,
                verificationResult: VerificationResult, canCache: Boolean) = reply(summary, playLine, episode, verificationResult, canCache)
            private suspend fun reply(summary: CartoonSummary, playLine: PlayLine, episode: Episode,
                verification: VerificationResult?, canCache: Boolean): SourceResult<PlayerInfo> {
                canCacheValues.add(canCache)
                playSamples.add("${summary.id}/${playLine.id}/${episode.id}")
                if (playInfoDelayMs > 0) delay(playInfoDelayMs)
                completedPlayInfos++
                playReply?.let { return it(summary, playLine, episode, verification) }
                if (playLine.id in failedLines) return SourceResult.Error(IllegalStateException("bad line"))
                return SourceResult.Complete(PlayerInfo(uri = "https://cdn.example.test/video.m3u8").apply {
                    header = mapOf("Cookie" to "private-session")
                })
            }
        }
    }
    private class FakeWebProxy : IWebProxy {
        var closed = false
        override suspend fun href(url: String, cleanLoaded: Boolean) = Unit
        override suspend fun loadUrl(url: String, userAgent: String?, headers: Map<String, String>, interceptResRegex: String?, needBlob: Boolean) = Unit
        override suspend fun waitingForPageLoaded(timeout: Long) = true
        override suspend fun waitingForResourceLoaded(urlRegex: String, sticky: Boolean, timeout: Long): String? = null
        override suspend fun waitingForBlobText(urlRegex: String?, textRegex: String?, sticky: Boolean, timeout: Long): Pair<String?, String?>? = null
        override suspend fun getContent(timeout: Long): String? = null
        override suspend fun getContentWithIframe(timeout: Long): String? = null
        override suspend fun executeJavaScript(script: String, delay: Long): String? = null
        override fun addToWindow(show: Boolean) = Unit
        override fun getWebView(): WebView? = null
        override fun close() { closed = true }
    }
    companion object {
        private fun imageParam() = VerificationParam.ImageCaptcha("SECRET_IMAGE_ADDRESS", hint = "PRIVATE_CAPTCHA_HINT")
        private fun searchChallenge(pageKey: Int = 7, keyword: String = LabConfig.DEFAULT_KEYWORDS.first()) =
            SearchNeedVerificationBusinessException(SearchVerificationRequest(pageKey, keyword, SOURCE.key), imageParam())
        private fun playChallenge(summary: CartoonSummary, line: PlayLine, episode: Episode) =
            PlayInfoNeedVerificationBusinessException(PlayInfoVerificationRequest(summary, line, episode), imageParam())
        private val SOURCE = object : Source {
            override val key = "fake.source"
            override val label = "Fake"
            override val version = "1.0"
            override val versionCode = 1
            override val describe: String? = null
            override fun register(): List<KClass<*>> = emptyList()
        }
        private fun hit(id: String, title: String = id): CartoonCover = CartoonCoverImpl(id, SOURCE.key, "", title)
        private fun line(id: String, vararg episodes: Episode) = PlayLine(id, id,
            if (episodes.isEmpty()) arrayListOf(Episode("ep-1", "第1集", 1)) else ArrayList(episodes.toList()))
    }
}
