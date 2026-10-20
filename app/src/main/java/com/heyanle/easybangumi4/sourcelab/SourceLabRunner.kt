package com.heyanle.easybangumi4.sourcelab

import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.plugin.api.component.page.PageComponent
import com.heyanle.easybangumi4.plugin.api.component.page.SourcePage
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.component.search.SearchComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCover
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.easybangumi4.plugin.source.js.source.JsSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.lang.reflect.InvocationTargetException
import java.util.Collections
import java.util.IdentityHashMap
import org.mozilla.javascript.WrappedException

/** 测试用组件边界；避免单测依赖 Android、JS 引擎或网络。 */
internal interface LabSourceAccess {
    fun source(key: String): Source?
    fun page(key: String): PageComponent?
    fun search(key: String): SearchComponent?
    fun detailed(key: String): DetailedComponent?
    fun play(key: String): PlayComponent?
}

/**
 * 固定验收路径：首页取样、按顺序搜索别名、第一部的各线路首集。
 * 搜索/首页只加载一次，找到一个结果后不再搜索其它别名或另选作品掩盖播放失败。
 */
class SourceLabRunner internal constructor(
    private val access: LabSourceAccess,
    private val probe: LabPlaybackProbe,
    val evidenceContext: LabEvidenceContext = LabEvidenceContext(),
    private val verificationHandler: LabVerificationHandler? = null,
) {
    constructor(
        bundle: SourceBundle,
        probe: SourceLabPlayProbe,
        evidenceContext: LabEvidenceContext = LabEvidenceContext(),
        verificationHandler: LabVerificationHandler? = null,
    ) : this(object : LabSourceAccess {
        override fun source(key: String) = bundle.source(key)
        override fun page(key: String) = bundle.page(key)
        override fun search(key: String) = bundle.search(key)
        override fun detailed(key: String) = bundle.detailed(key)
        override fun play(key: String) = bundle.play(key)
    }, probe, evidenceContext, verificationHandler)

    private class Sample {
        var homeLoaded = false
        var homeHit: CartoonCover? = null
        var homeLabel: String? = null
        var searched = false
        var searchHit: CartoonCover? = null
        var selection: LabSelection? = null
        var budgetExhausted = false
        var totalLineCount = 0
        var manualWaitMs = 0L
        var stopSource = false
        val verificationEvents = ArrayList<LabVerificationEvent>()
        val searches = ArrayList<LabSearchAttempt>()
        val lines = ArrayList<LabLineAttempt>()
    }

    suspend fun runSource(
        key: String,
        config: LabConfig,
        onStep: suspend (LabStepResult) -> Unit = {},
    ): LabSourceResult {
        var interruptedResult: LabSourceResult? = null
        return try { withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        val source = access.source(key)
        val sourceHash = sourceHash(source)
        val sample = Sample()
        val results = ArrayList<LabStepResult>()
        fun snapshot() = LabSourceResult(
            key = key, label = source?.label ?: key, steps = results.toList(),
            versionName = source?.version.orEmpty(), versionCode = source?.versionCode ?: 0,
            sourceSha256 = sourceHash, startedAt = startedAt, finishedAt = System.currentTimeMillis(),
            searchAttempts = sample.searches.toList(), selection = sample.selection,
            lineAttempts = sample.lines.toList(), playBudgetExhausted = sample.budgetExhausted,
            minimumProgressMs = config.minimumProgressMs, totalLineCount = sample.totalLineCount,
            verificationEvents = sample.verificationEvents.toList(), manualWaitMs = sample.manualWaitMs,
        )
        var currentStep: LabStep? = null
        try {
            for (step in LabStep.entries.filter { it in config.steps }) {
                currentStep = step
                val result = if (sample.stopSource) {
                    LabStepResult(step, LabVerdict.SKIP, 0, "当前源人工验证未完成，跳过后续步骤")
                } else when (step) {
                    LabStep.HOME -> home(key, config, sample)
                    LabStep.SEARCH -> search(key, config, sample)
                    LabStep.PLAY -> play(key, config, sample)
                }
                results.add(result)
                onStep(result)
            }
        } catch (cancelled: CancellationException) {
            if (sample.verificationEvents.lastOrNull()?.decision == LabVerificationDecision.CANCELLED) {
                val event = sample.verificationEvents.last()
                if (event.step == LabStep.SEARCH && sample.searches.none { it.keyword == event.keyword }) {
                    sample.searches.add(LabSearchAttempt(event.keyword.orEmpty(), LabSearchStatus.VERIFICATION_REQUIRED,
                        0, 0, "人工验证已取消，当前搜索未完成", pageKey = event.searchPageKey))
                }
                for (step in LabStep.entries.filter { it in config.steps && results.none { result -> result.step == it } }) {
                    results.add(LabStepResult(step,
                        if (step == currentStep) LabVerdict.SUSPECT else LabVerdict.SKIP, 0,
                        if (step == currentStep) "人工验证已取消，当前请求未完成" else "测试已取消，未执行"))
                }
                val partial = snapshot()
                // IO → caller 的 prompt cancellation 可能替换具体异常；在 dispatcher 外重建它。
                interruptedResult = partial
                throw LabVerificationCancelledException(partial).apply { initCause(cancelled) }
            }
            throw cancelled
        }
        if (sourceHash.isNotEmpty() && sourceHash != sourceHash(source)) {
            val invalid = LabStepResult(LabStep.PLAY, LabVerdict.FAIL, 0, "源文件在体检中被修改，证据失效")
            val index = results.indexOfFirst { it.step == LabStep.PLAY }
            if (index >= 0) results[index] = invalid else results.add(invalid)
        }
        snapshot()
        } } catch (cancelled: CancellationException) {
            if (cancelled is LabVerificationCancelledException) throw cancelled
            interruptedResult?.let { partial ->
                throw LabVerificationCancelledException(partial).apply { initCause(cancelled) }
            }
            throw cancelled
        }
    }

    private suspend fun home(key: String, config: LabConfig, sample: Sample): LabStepResult {
        val call = request(config.stepTimeoutMs) {
            sample.homeLoaded = true
            val component = access.page(key) ?: error("源没有提供 PageComponent")
            val pages = component.getPages()
            val first = pages.firstOrNull() ?: error("getPages() 返回空列表")
            val page = when (first) {
                is SourcePage.Group -> first.loadPage().value().firstOrNull()
                is SourcePage.SingleAsyncPage -> first.load().value()
                is SourcePage.SingleCartoonPage -> first
            } ?: error("首页第一个分组没有可加载页签")
            val items = page.load(page.firstKey()).value().second
            sample.homeHit = items.firstOrNull()
            sample.homeLabel = page.label
            if (items.isEmpty()) "首页页签「${page.label}」返回 0 条内容" else "首页页签「${page.label}」${items.size} 条内容"
        }
        call.failure?.let { recordUnsupportedVerification(it, key, LabStep.HOME, sample) }
        return LabStepResult(
            LabStep.HOME,
            if (call.status != null) verdictOf(call.status) else if (sample.homeHit != null) LabVerdict.PASS else LabVerdict.SUSPECT,
            call.elapsedMs, call.detail.ifEmpty { call.value.orEmpty() },
            failureKind = call.diagnosticFailure(),
        )
    }

    private suspend fun search(key: String, config: LabConfig, sample: Sample): LabStepResult {
        sample.searched = true
        val start = monotonicMs()
        val waitAtStart = sample.manualWaitMs
        val keywords = config.keywords.map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { LabConfig.DEFAULT_KEYWORDS }
        val component = access.search(key)
        if (component == null) {
            sample.searches.add(LabSearchAttempt(keywords.first(), LabSearchStatus.UNAVAILABLE, 0, 0, "源没有提供 SearchComponent"))
        } else for (keyword in keywords) {
            var pageKey: Int? = null
            val call = verifiedRequest(config.stepTimeoutMs, config, sample,
                context = { param, attempt -> LabVerificationRequest(
                    key, access.source(key)?.label ?: key, LabStep.SEARCH,
                    keyword = keyword, searchPageKey = pageKey, attempt = attempt, verificationParam = param,
                ) },
                matches = { throwable -> searchChallenge(throwable, key, keyword, pageKey) },
            ) { verification ->
                val fixedPageKey = pageKey ?: component.getFirstSearchKey(keyword).also { pageKey = it }
                if (verification == null) component.search(fixedPageKey, keyword).value().second
                else component.search(fixedPageKey, keyword, verification).value().second
            }
            val list = call.value.orEmpty()
            val status = call.status ?: if (list.isEmpty()) LabSearchStatus.EMPTY else LabSearchStatus.RESULTS
            sample.searches.add(LabSearchAttempt(
                keyword, status, list.size, call.elapsedMs,
                call.detail.ifEmpty { "「$keyword」${list.size} 条" }, pageKey = pageKey,
                failureKind = call.diagnosticFailure(),
            ))
            if (list.isNotEmpty()) {
                sample.searchHit = list.first()
                sample.selection = LabSelection(
                    LabSelectionRoute.SEARCH_RESULT, list.first().id, list.first().title, keyword = keyword,
                )
                break
            }
            // 仅正常空结果使用语言 fallback；认证、网络或解析失败立即短路以减少重复请求。
            if (status != LabSearchStatus.EMPTY) break
        }
        val allEmpty = sample.searches.all { it.status == LabSearchStatus.EMPTY }
        val errors = sample.searches.any { it.status !in setOf(LabSearchStatus.RESULTS, LabSearchStatus.EMPTY) }
        val verdict = when {
            sample.searchHit != null && !errors -> LabVerdict.PASS
            allEmpty || sample.searches.any { it.status == LabSearchStatus.VERIFICATION_REQUIRED } -> LabVerdict.SUSPECT
            sample.searchHit != null -> LabVerdict.SUSPECT
            else -> LabVerdict.FAIL
        }
        val detail = sample.searches.joinToString(" → ") { "「${it.keyword}」${it.status}(${it.resultCount})" } + when {
            sample.searchHit != null -> "；选择结果第 1 部「${sample.searchHit?.title}」"
            allEmpty -> "；标题未收录，播放阶段使用首页第 1 部，仍需人工审核"
            else -> "；搜索不可用，首页播放只能供诊断，不能自动准入"
        }
        return LabStepResult(LabStep.SEARCH, verdict, activeElapsed(start, sample, waitAtStart), detail,
            failureKind = sample.searches.firstNotNullOfOrNull { it.failureKind })
    }

    private suspend fun play(key: String, config: LabConfig, sample: Sample): LabStepResult {
        val start = monotonicMs()
        val waitAtStart = sample.manualWaitMs
        fun result(verdict: LabVerdict, detail: String, failureKind: LabFailureKind? = null) =
            LabStepResult(LabStep.PLAY, verdict, activeElapsed(start, sample, waitAtStart), detail, failureKind)
        if (!sample.searched) search(key, config, sample)
        if (sample.stopSource) return result(LabVerdict.SUSPECT, "人工验证未完成，当前源停止验收")
        val hit = sample.searchHit ?: run {
            if (!sample.homeLoaded) home(key, config, sample)
            sample.homeHit?.also {
                sample.selection = LabSelection(
                    LabSelectionRoute.HOME_FALLBACK, it.id, it.title, homePageLabel = sample.homeLabel,
                )
            }
        } ?: return result(LabVerdict.FAIL, "所有别名无可用结果且首页第 1 部不可用，无法定位播放样本")
        if (hit.id.isBlank() || hit.source != key) return result(LabVerdict.FAIL, "第 1 部的 id 为空或 source 与被测源不一致")
        val detailed = access.detailed(key) ?: return result(LabVerdict.FAIL, "源没有提供 DetailedComponent")
        val summary = CartoonSummary(hit.id, key)
        val detail = request(config.stepTimeoutMs) { detailed.getAll(summary).value() }
        detail.failure?.let { recordUnsupportedVerification(it, key, LabStep.PLAY, sample, summary.id) }
        if (detail.status != null) return result(verdictOf(detail.status), "详情：${detail.detail}", detail.diagnosticFailure())
        val lines = detail.value?.second.orEmpty()
        sample.totalLineCount = lines.size
        if (lines.isEmpty()) return result(LabVerdict.FAIL, "「${hit.title}」没有播放线路")
        val playComponent = access.play(key) ?: return result(LabVerdict.FAIL, "源没有提供 PlayComponent")
        val lineStart = monotonicMs()
        val lineWaitStart = sample.manualWaitMs
        for ((index, line) in lines.withIndex()) {
            val remaining = config.playBudgetMs - activeElapsed(lineStart, sample, lineWaitStart)
            if (remaining <= 0) {
                sample.budgetExhausted = true
                break
            }
            val attempt = testLine(summary, line, index, playComponent, config, remaining, sample)
            sample.lines.add(attempt)
            if (sample.stopSource) return result(LabVerdict.SUSPECT, "线路 ${index + 1} 人工验证未完成，当前源停止验收")
            if (attempt.status == LabLineStatus.PASS) {
                val path = if (sample.selection?.route == LabSelectionRoute.HOME_FALLBACK) "首页回退" else "搜索"
                return result(LabVerdict.PASS,
                    "$path「${hit.title}」线路 ${index + 1}/${lines.size}「${line.label}」${attempt.episodeLabel}，" +
                        "首帧 ${attempt.videoWidth}x${attempt.videoHeight}，推进 ${attempt.progressMs}ms（已尝试 ${sample.lines.size} 条线路）")
            }
        }
        if (sample.lines.size < lines.size) sample.budgetExhausted = true
        return when {
            sample.budgetExhausted -> result(LabVerdict.SUSPECT, "播放总预算 ${config.playBudgetMs / 1000}s 用尽，已尝试 ${sample.lines.size}/${lines.size} 条线路；剩余线路未验证")
            sample.lines.any { it.status == LabLineStatus.VERIFICATION_REQUIRED } -> result(LabVerdict.SUSPECT, "所有 ${lines.size} 条线路未播放通过，部分需要人工验证；请验证后重测")
            else -> result(LabVerdict.FAIL, "全部 ${lines.size} 条线路首集未通过：" + sample.lines.joinToString("，") { "${it.lineLabel}=${it.status}" })
        }
    }

    private suspend fun testLine(
        summary: CartoonSummary, line: PlayLine, index: Int, component: PlayComponent,
        config: LabConfig, remainingMs: Long, sample: Sample,
    ): LabLineAttempt {
        val start = monotonicMs()
        val waitAtStart = sample.manualWaitMs
        // order 升序稳定排序，和播放页的“首集”含义一致；order 相同保留源返回顺序。
        val episode = line.episode.minByOrNull { it.order }
        fun attempt(status: LabLineStatus, detail: String, outcome: ProbeOutcome? = null,
                    failureKind: LabFailureKind? = null) = LabLineAttempt(
            lineIndex = index, lineId = line.id, lineLabel = line.label,
            episodeId = episode?.id.orEmpty(), episodeLabel = episode?.label.orEmpty(), episodeOrder = episode?.order ?: 0,
            status = status, elapsedMs = activeElapsed(start, sample, waitAtStart), firstFrame = outcome?.firstFrame ?: false,
            progressMs = outcome?.progressMs ?: 0L, videoWidth = outcome?.videoSize?.width ?: 0,
            videoHeight = outcome?.videoSize?.height ?: 0, detail = detail, failureKind = failureKind,
        )
        if (episode == null) return attempt(LabLineStatus.EMPTY_LINE, "线路没有剧集")
        val expectedSummary = summary.copy()
        val expectedLine = PlayLine(line.id, line.label, ArrayList(line.episode))
        val parsed = try { verifiedRequest(config.playInfoTimeoutMs.coerceAtMost(remainingMs), config, sample,
            context = { param, attempt -> LabVerificationRequest(
                expectedSummary.source, access.source(expectedSummary.source)?.label ?: expectedSummary.source, LabStep.PLAY,
                lineIndex = index, lineId = line.id, lineLabel = line.label,
                episodeId = episode.id, episodeLabel = episode.label,
                attempt = attempt, verificationParam = param, cartoonId = expectedSummary.id,
            ) },
            matches = { throwable -> playChallenge(throwable, expectedSummary, expectedLine, episode) },
        ) { verification ->
            check(summary == expectedSummary && sameLine(line, expectedLine)) { "源修改了固定播放样本" }
            if (verification == null) component.getPlayInfo(summary, line, episode, canCache = false).value()
            else component.getPlayInfo(summary, line, episode, verification, canCache = false).value()
        } } catch (cancelled: CancellationException) {
            if (sample.verificationEvents.lastOrNull()?.decision == LabVerificationDecision.CANCELLED) {
                sample.lines.add(attempt(LabLineStatus.VERIFICATION_REQUIRED, "人工验证已取消，当前线路首集未完成"))
            }
            throw cancelled
        }
        if (parsed.status != null) return attempt(lineStatusOf(parsed.status), "地址解析：${parsed.detail}",
            failureKind = parsed.diagnosticFailure().let { if (it == LabFailureKind.UNKNOWN) LabFailureKind.PARSING else it })
        val info = parsed.value ?: return attempt(LabLineStatus.ERROR, "地址解析返回空结果", failureKind = LabFailureKind.PARSING)
        if (info.uri.isBlank()) return attempt(LabLineStatus.ERROR, "解析出的播放地址为空", failureKind = LabFailureKind.PARSING)
        val remainingProbe = remainingMs - activeElapsed(start, sample, waitAtStart)
        if (remainingProbe <= 0) return attempt(LabLineStatus.TIMEOUT, "线路地址解析耗尽剩余播放预算")
        val playback = request((config.playTimeoutMs + 1_000L).coerceAtMost(remainingProbe)) {
            probe.probe(info, config.playTimeoutMs.coerceAtMost(remainingProbe), config.minimumProgressMs)
        }
        if (playback.status != null) return attempt(lineStatusOf(playback.status), playback.detail,
            failureKind = playback.diagnosticFailure())
        val outcome = playback.value ?: return attempt(LabLineStatus.ERROR, "播放器没有返回结果")
        val status = when {
            outcome.error -> LabLineStatus.ERROR
            outcome.firstFrame && outcome.progressMs >= config.minimumProgressMs &&
                (outcome.videoSize?.width ?: 0) > 0 && (outcome.videoSize?.height ?: 0) > 0 -> LabLineStatus.PASS
            outcome.firstFrame -> LabLineStatus.NO_PROGRESS
            outcome.timedOut && !outcome.ready -> LabLineStatus.TIMEOUT
            else -> LabLineStatus.NO_FIRST_FRAME
        }
        return attempt(status, outcome.message, outcome,
            failureKind = when {
                status == LabLineStatus.PASS -> null
                status == LabLineStatus.TIMEOUT -> LabFailureKind.TIMEOUT
                else -> outcome.failureKind ?: LabFailureKind.PLAYBACK
            })
    }

    private data class Call<T>(
        val value: T?, val status: LabSearchStatus?, val elapsedMs: Long, val detail: String,
        val failure: Throwable? = null,
    )

    private data class Challenge(val param: VerificationParam, val matches: Boolean)

    private fun Call<*>.diagnosticFailure(): LabFailureKind? = when (status) {
        null -> null
        LabSearchStatus.TIMEOUT -> LabFailureKind.TIMEOUT
        LabSearchStatus.VERIFICATION_REQUIRED -> LabFailureKind.VERIFICATION
        else -> {
            val causes = failure?.let(::causesOf).orEmpty()
            when {
                causes.any { it is java.io.IOException } -> LabFailureKind.NETWORK
                causes.any { it is ParserException || it is org.mozilla.javascript.RhinoException } -> LabFailureKind.PARSING
                else -> LabFailureKind.UNKNOWN
            }
        }
    }

    /** 网络请求独立计时，等待人手验证完全在超时作用域外；重试复用固定请求参数。 */
    private suspend fun <T : Any> verifiedRequest(
        timeoutMs: Long,
        config: LabConfig,
        sample: Sample,
        context: (VerificationParam, Int) -> LabVerificationRequest,
        matches: (Throwable) -> Challenge?,
        block: suspend (VerificationResult?) -> T,
    ): Call<T> {
        var activeMs = 0L
        var submissions = 0
        var verification: VerificationResult? = null
        var precedingEvent: Int? = null
        while (true) {
            val remainingMs = timeoutMs - activeMs
            val call: Call<T> = if (remainingMs > 0) request(remainingMs) { block(verification) }
                else Call(null, LabSearchStatus.TIMEOUT, 0, "当前请求的活跃预算已用尽")
            activeMs += call.elapsedMs
            precedingEvent?.let { index ->
                sample.verificationEvents[index] = sample.verificationEvents[index].copy(
                    retryOutcome = when (call.status) {
                        null -> LabVerificationRetryOutcome.SUCCEEDED
                        LabSearchStatus.VERIFICATION_REQUIRED -> LabVerificationRetryOutcome.VERIFICATION_REQUIRED
                        LabSearchStatus.TIMEOUT -> LabVerificationRetryOutcome.TIMEOUT
                        else -> LabVerificationRetryOutcome.ERROR
                    },
                )
            }
            val challenge = call.failure?.let(matches)
            if (challenge == null) {
                // 验证重试的异常可能回显答案或会话信息，报告只保留类型与结果。
                return call.copy(elapsedMs = activeMs, detail = if (verification != null && call.failure != null) {
                    "${call.failure.javaClass.simpleName}：验证后的请求失败（敏感详情未导出）"
                } else call.detail)
            }
            val request = context(challenge.param, submissions + 1)
            val requestedAt = System.currentTimeMillis()
            fun event(decision: LabVerificationDecision, respondedAt: Long = requestedAt, waitMs: Long = 0L) =
                LabVerificationEvent(
                    sourceKey = request.sourceKey, step = request.step,
                    type = when (request.verificationParam) {
                        is VerificationParam.WebView -> LabVerificationType.WEB_VIEW
                        is VerificationParam.ImageCaptcha -> LabVerificationType.IMAGE_CAPTCHA
                    },
                    attempt = request.attempt, requestedAt = requestedAt, respondedAt = respondedAt,
                    waitMs = waitMs, decision = decision,
                    keyword = request.keyword, searchPageKey = request.searchPageKey, cartoonId = request.cartoonId,
                    lineIndex = request.lineIndex, lineId = request.lineId, episodeId = request.episodeId,
                )
            val unavailableDecision = when {
                !challenge.matches -> LabVerificationDecision.REQUEST_MISMATCH
                verificationHandler == null -> LabVerificationDecision.UNAVAILABLE
                submissions >= config.maxManualAttempts -> LabVerificationDecision.ATTEMPT_LIMIT
                else -> null
            }
            if (unavailableDecision != null) {
                sample.verificationEvents.add(event(unavailableDecision))
                discardVerification(request.verificationParam)
                // 无 handler 保持旧的诊断行为；准入结论仍被 manual 证据拦截。
                sample.stopSource = verificationHandler != null || unavailableDecision == LabVerificationDecision.REQUEST_MISMATCH
                return Call(null, LabSearchStatus.VERIFICATION_REQUIRED, activeMs, when (unavailableDecision) {
                    LabVerificationDecision.REQUEST_MISMATCH -> "人工验证请求与当前样本不一致，已拒绝接力"
                    LabVerificationDecision.ATTEMPT_LIMIT -> "人工验证已达 ${config.maxManualAttempts} 次提交上限"
                    else -> "需要人工验证，当前运行器没有验证界面"
                })
            }
            val waitStart = monotonicMs()
            val response: LabVerificationResponse
            try {
                response = verificationHandler!!.verify(request)
            } catch (cancelled: CancellationException) {
                val waitMs = elapsed(waitStart)
                sample.manualWaitMs += waitMs
                sample.verificationEvents.add(event(LabVerificationDecision.CANCELLED, System.currentTimeMillis(), waitMs))
                discardVerification(request.verificationParam)
                throw cancelled
            } catch (_: Exception) {
                val waitMs = elapsed(waitStart)
                sample.manualWaitMs += waitMs
                sample.verificationEvents.add(event(LabVerificationDecision.UNAVAILABLE, System.currentTimeMillis(), waitMs))
                discardVerification(request.verificationParam)
                sample.stopSource = true
                return Call(null, LabSearchStatus.VERIFICATION_REQUIRED, activeMs, "人工验证界面不可用，当前源停止验收")
            }
            val waitMs = elapsed(waitStart)
            sample.manualWaitMs += waitMs
            val decision = when (response) {
                is LabVerificationResponse.Completed -> if (response.result.matches(request.verificationParam)) {
                    LabVerificationDecision.COMPLETED
                } else LabVerificationDecision.REQUEST_MISMATCH
                LabVerificationResponse.Skipped -> LabVerificationDecision.SKIPPED
                LabVerificationResponse.Cancelled -> LabVerificationDecision.CANCELLED
                LabVerificationResponse.Expired -> LabVerificationDecision.EXPIRED
                LabVerificationResponse.Unavailable -> LabVerificationDecision.UNAVAILABLE
            }
            sample.verificationEvents.add(event(decision, System.currentTimeMillis(), waitMs))
            if (decision == LabVerificationDecision.CANCELLED) {
                discardVerification(request.verificationParam)
                throw CancellationException("人工验证已取消")
            }
            if (decision != LabVerificationDecision.COMPLETED) {
                discardVerification(request.verificationParam)
                sample.stopSource = true
                return Call(null, LabSearchStatus.VERIFICATION_REQUIRED, activeMs, when (decision) {
                    LabVerificationDecision.SKIPPED -> "用户跳过人工验证，当前源停止验收"
                    LabVerificationDecision.EXPIRED -> "人工验证等待超时，当前源停止验收"
                    LabVerificationDecision.REQUEST_MISMATCH -> "验证结果与当前请求类型或网页会话不一致，已拒绝接力"
                    else -> "人工验证不可用，当前源停止验收"
                })
            }
            submissions++
            verification = (response as LabVerificationResponse.Completed).result
            precedingEvent = sample.verificationEvents.lastIndex
        }
    }

    private fun VerificationResult.matches(param: VerificationParam): Boolean = when {
        this is VerificationResult.ImageCaptcha && param is VerificationParam.ImageCaptcha -> true
        this is VerificationResult.WebView && param is VerificationParam.WebView -> iWebProxy === param.iWebProxy
        else -> false
    }

    private suspend fun discardVerification(param: VerificationParam) {
        val proxy = (param as? VerificationParam.WebView)?.iWebProxy ?: return
        // 原生 WebView 的 detach/recycle 必须在 Main；无 WebView 的测试/代理不依赖 Android dispatcher。
        if (runCatching { proxy.getWebView() }.getOrNull() == null) runCatching { proxy.close() }
        else withContext(NonCancellable + Dispatchers.Main.immediate) { runCatching { proxy.close() } }
    }

    /** 首页和详情 API 没有验证结果重载；只保留人工证据，不伪造“恢复成功”。 */
    private suspend fun recordUnsupportedVerification(
        t: Throwable, key: String, step: LabStep, sample: Sample, cartoonId: String? = null,
    ) {
        val param = causesOf(t).firstNotNullOfOrNull {
            when (it) {
                is SearchNeedVerificationBusinessException -> it.verificationParam
                is PlayInfoNeedVerificationBusinessException -> it.verificationParam
                is NeedWebViewCheckExceptionInner -> VerificationParam.WebView(it.iWebProxy, it.tips)
                else -> null
            }
        } ?: return
        val time = System.currentTimeMillis()
        sample.verificationEvents.add(LabVerificationEvent(
            sourceKey = key, step = step,
            type = if (param is VerificationParam.WebView) LabVerificationType.WEB_VIEW else LabVerificationType.IMAGE_CAPTCHA,
            attempt = 1, requestedAt = time, respondedAt = time, waitMs = 0,
            decision = LabVerificationDecision.UNAVAILABLE, cartoonId = cartoonId,
        ))
        sample.stopSource = true
        discardVerification(param)
    }

    private fun searchChallenge(t: Throwable, key: String, keyword: String, pageKey: Int?): Challenge? {
        val causes = causesOf(t)
        val typed = causes.firstOrNull { it is SearchNeedVerificationBusinessException || it is PlayInfoNeedVerificationBusinessException }
        return when (typed) {
            is SearchNeedVerificationBusinessException -> Challenge(typed.verificationParam,
                typed.request.source == key && typed.request.keyword == keyword && typed.request.key == pageKey)
            is PlayInfoNeedVerificationBusinessException -> Challenge(typed.verificationParam, false)
            else -> causes.filterIsInstance<NeedWebViewCheckExceptionInner>().firstOrNull()?.let {
                Challenge(VerificationParam.WebView(it.iWebProxy, it.tips), pageKey != null)
            }
        }
    }

    private fun playChallenge(t: Throwable, summary: CartoonSummary, line: PlayLine, episode: Episode): Challenge? {
        val causes = causesOf(t)
        val typed = causes.firstOrNull { it is SearchNeedVerificationBusinessException || it is PlayInfoNeedVerificationBusinessException }
        return when (typed) {
            is PlayInfoNeedVerificationBusinessException -> Challenge(typed.verificationParam, typed.request?.let {
                it.summary == summary && sameLine(it.playLine, line) && sameEpisode(it.episode, episode)
            } == true)
            is SearchNeedVerificationBusinessException -> Challenge(typed.verificationParam, false)
            else -> causes.filterIsInstance<NeedWebViewCheckExceptionInner>().firstOrNull()?.let {
                Challenge(VerificationParam.WebView(it.iWebProxy, it.tips), true)
            }
        }
    }

    private fun sameLine(actual: PlayLine, expected: PlayLine): Boolean =
        actual.id == expected.id && actual.label == expected.label && actual.episode.size == expected.episode.size &&
            actual.episode.zip(expected.episode).all { (first, second) -> sameEpisode(first, second) }

    private fun sameEpisode(actual: Episode, expected: Episode): Boolean =
        actual.id == expected.id && actual.label == expected.label && actual.order == expected.order

    private fun activeElapsed(start: Long, sample: Sample, waitAtStart: Long): Long =
        (elapsed(start) - (sample.manualWaitMs - waitAtStart)).coerceAtLeast(0L)

    private suspend fun <T : Any> request(timeoutMs: Long, block: suspend () -> T): Call<T> {
        val start = monotonicMs()
        return try {
            val value = withTimeoutOrNull(timeoutMs) { block() }
            if (value == null) Call(null, LabSearchStatus.TIMEOUT, elapsed(start), "超时：${timeoutMs / 1000}s")
            else Call(value, null, elapsed(start), "")
        } catch (t: Exception) {
            // 组件代理可能包一层 InvocationTargetException；用户取消不能被误记成网络失败。
            causesOf(t)
                .filterIsInstance<CancellationException>().firstOrNull()?.let { throw it }
            val status = statusOf(t)
            Call(null, status, elapsed(start), if (status == LabSearchStatus.VERIFICATION_REQUIRED) {
                "需要人工验证（敏感参数未导出）"
            } else describe(t), t)
        }
    }

    private fun statusOf(t: Throwable): LabSearchStatus {
        val causes = causesOf(t)
        return when {
            causes.any { it is NeedWebViewCheckExceptionInner || it is SearchNeedVerificationBusinessException ||
                it is PlayInfoNeedVerificationBusinessException } -> LabSearchStatus.VERIFICATION_REQUIRED
            causes.any { it.javaClass.simpleName.contains("Timeout", true) } -> LabSearchStatus.TIMEOUT
            else -> LabSearchStatus.ERROR
        }
    }
    private fun causesOf(t: Throwable): List<Throwable> {
        val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
        val pending = ArrayDeque<Throwable>().apply { add(t) }
        val result = ArrayList<Throwable>()
        while (pending.isNotEmpty() && result.size < 24) {
            val current = pending.removeFirst()
            if (!seen.add(current)) continue
            result.add(current)
            current.cause?.let(pending::addLast)
            if (current is ParserException) current.exception?.let(pending::addLast)
            if (current is InvocationTargetException) current.targetException?.let(pending::addLast)
            if (current is WrappedException) current.wrappedException?.let(pending::addLast)
        }
        return result
    }
    private fun verdictOf(status: LabSearchStatus) = if (status == LabSearchStatus.VERIFICATION_REQUIRED) LabVerdict.SUSPECT else LabVerdict.FAIL
    private fun lineStatusOf(status: LabSearchStatus) = when (status) {
        LabSearchStatus.VERIFICATION_REQUIRED -> LabLineStatus.VERIFICATION_REQUIRED
        LabSearchStatus.TIMEOUT -> LabLineStatus.TIMEOUT
        else -> LabLineStatus.ERROR
    }
    private fun describe(t: Throwable): String = "${t.javaClass.simpleName}：${t.message.orEmpty()}"
        .replace(Regex("https?://\\S+"), "[url]").take(240)
    private fun sourceHash(source: Source?): String = runCatching {
        val js = source as? JsSource ?: return@runCatching ""
        val bytes = js.getJsFile()?.readBytes() ?: js.getJsString().toByteArray(Charsets.UTF_8)
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }.getOrDefault("")
}

private fun monotonicMs(): Long = System.nanoTime() / 1_000_000L
private fun elapsed(start: Long): Long = (monotonicMs() - start).coerceAtLeast(0)
private fun <T> SourceResult<T>.value(): T = when (this) {
    is SourceResult.Complete -> data
    is SourceResult.Error -> throw throwable
}
