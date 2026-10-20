package com.heyanle.easybangumi4.sourcelab

import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class LabStep(val label: String) {
    HOME("首页"), SEARCH("搜索"), PLAY("播放");
    companion object { fun all(): Set<LabStep> = entries.toSet() }
}

enum class LabVerdict(val label: String) {
    PASS("通过"), SUSPECT("待复核"), FAIL("失败"), SKIP("跳过");
    val severity: Int get() = when (this) { FAIL -> 0; SUSPECT -> 1; SKIP -> 2; PASS -> 3 }
}

enum class LabFailureKind { NETWORK, TIMEOUT, VERIFICATION, PARSING, PLAYBACK, UNKNOWN }

data class LabStepResult(
    val step: LabStep,
    val verdict: LabVerdict,
    val elapsedMs: Long,
    val detail: String,
    val failureKind: LabFailureKind? = null,
)

enum class LabSearchStatus { RESULTS, EMPTY, ERROR, TIMEOUT, VERIFICATION_REQUIRED, UNAVAILABLE }
data class LabSearchAttempt(
    val keyword: String,
    val status: LabSearchStatus,
    val resultCount: Int,
    val elapsedMs: Long,
    val detail: String,
    val pageKey: Int? = null,
    val failureKind: LabFailureKind? = null,
)

enum class LabSelectionRoute { SEARCH_RESULT, HOME_FALLBACK }
data class LabSelection(
    val route: LabSelectionRoute,
    val cartoonId: String,
    val title: String,
    val resultIndex: Int = 0,
    val keyword: String? = null,
    val homePageLabel: String? = null,
)

enum class LabLineStatus { PASS, ERROR, TIMEOUT, VERIFICATION_REQUIRED, EMPTY_LINE, NO_FIRST_FRAME, NO_PROGRESS }
data class LabLineAttempt(
    val lineIndex: Int,
    val lineId: String,
    val lineLabel: String,
    val episodeId: String = "",
    val episodeLabel: String = "",
    val episodeOrder: Int = 0,
    val status: LabLineStatus,
    val elapsedMs: Long,
    val firstFrame: Boolean = false,
    val progressMs: Long = 0L,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val detail: String,
    val failureKind: LabFailureKind? = null,
)

enum class LabAcceptance {
    SEARCH_PLAYABLE,
    HOME_PLAYABLE_SEARCH_EMPTY,
    HOME_PLAYABLE_SEARCH_UNAVAILABLE,
    PLAYBACK_FAILED,
    MANUAL_VERIFICATION_REQUIRED,
    INCOMPLETE,
}

data class LabDeviceEvidence(
    val manufacturer: String = "",
    val model: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    /** adb get-serialno；设备端默认不猜测序列号。 */
    val serial: String = "",
    /** 由 adb 验证后填写；模拟器不能产生正式准入证据。 */
    val isPhysicalDevice: Boolean = false,
)
data class LabAppEvidence(
    val packageName: String = "",
    val versionName: String = "",
    val versionCode: Int = 0,
    val buildType: String = "",
    val gitCommit: String = "",
)
data class LabEvidenceContext(
    val device: LabDeviceEvidence = LabDeviceEvidence(),
    val app: LabAppEvidence = LabAppEvidence(),
    val repositoryEnvironment: String = "dev",
    val network: String = "",
)

data class LabSourceResult(
    val key: String,
    val label: String,
    val steps: List<LabStepResult>,
    val versionName: String = "",
    val versionCode: Int = 0,
    /** 实际被加载 JS 文件字节的 SHA-256，无法读取时留空且禁止 release 准入。 */
    val sourceSha256: String = "",
    val startedAt: Long = 0L,
    val finishedAt: Long = 0L,
    val searchAttempts: List<LabSearchAttempt> = emptyList(),
    val selection: LabSelection? = null,
    val lineAttempts: List<LabLineAttempt> = emptyList(),
    val playBudgetExhausted: Boolean = false,
    val minimumProgressMs: Long = LabConfig.DEFAULT_MINIMUM_PROGRESS_MS,
    val totalLineCount: Int = 0,
    val verificationEvents: List<LabVerificationEvent> = emptyList(),
    /** 不计入步骤/线路活跃耗时及机器预算。 */
    val manualWaitMs: Long = 0L,
) {
    val verdict: LabVerdict get() = when {
        steps.any { it.verdict == LabVerdict.FAIL } -> LabVerdict.FAIL
        steps.any { it.verdict == LabVerdict.SUSPECT } -> LabVerdict.SUSPECT
        steps.isNotEmpty() && steps.all { it.verdict == LabVerdict.PASS } -> LabVerdict.PASS
        else -> LabVerdict.SKIP
    }
    val elapsedMs: Long get() = steps.sumOf { it.elapsedMs }
    fun step(step: LabStep): LabStepResult? = steps.firstOrNull { it.step == step }

    /** 候选结论；正式 release 仍须校验证据、文件 hash 和人工审核记录。 */
    val acceptance: LabAcceptance get() {
        // 未解决的验证码优先于首页/其它线路的诊断成功，防止旁路准入。
        if (searchAttempts.any { it.status == LabSearchStatus.VERIFICATION_REQUIRED } ||
            lineAttempts.any { it.status == LabLineStatus.VERIFICATION_REQUIRED } ||
            verificationEvents.any { it.decision != LabVerificationDecision.COMPLETED } ||
            verificationEvents.lastOrNull()?.retryOutcome == LabVerificationRetryOutcome.VERIFICATION_REQUIRED) {
            return LabAcceptance.MANUAL_VERIFICATION_REQUIRED
        }
        if (LabStep.entries.any { step(it) == null } || playBudgetExhausted) return LabAcceptance.INCOMPLETE
        val proven = step(LabStep.PLAY)?.verdict == LabVerdict.PASS && lineAttempts.any {
            it.status == LabLineStatus.PASS && it.firstFrame && it.progressMs >= minimumProgressMs &&
                it.videoWidth > 0 && it.videoHeight > 0
        }
        if (proven && step(LabStep.HOME)?.verdict != LabVerdict.PASS) return LabAcceptance.INCOMPLETE
        if (proven && step(LabStep.HOME)?.verdict == LabVerdict.PASS) {
            return when (selection?.route) {
                LabSelectionRoute.SEARCH_RESULT -> if (step(LabStep.SEARCH)?.verdict == LabVerdict.PASS) {
                    LabAcceptance.SEARCH_PLAYABLE
                } else LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE
                LabSelectionRoute.HOME_FALLBACK -> if (searchAttempts.isNotEmpty() &&
                    searchAttempts.all { it.status == LabSearchStatus.EMPTY }) {
                    LabAcceptance.HOME_PLAYABLE_SEARCH_EMPTY
                } else LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE
                null -> LabAcceptance.INCOMPLETE
            }
        }
        if (searchAttempts.any { it.status == LabSearchStatus.VERIFICATION_REQUIRED } ||
            lineAttempts.any { it.status == LabLineStatus.VERIFICATION_REQUIRED } ||
            steps.any { it.verdict == LabVerdict.SUSPECT && it.detail.contains("人工验证") }) {
            return LabAcceptance.MANUAL_VERIFICATION_REQUIRED
        }
        return LabAcceptance.PLAYBACK_FAILED
    }
}

data class LabConfig(
    /** 有序 fallback 别名：找到第一批非空结果后立即停止，不做多个独立搜索任务。 */
    val keywords: List<String> = DEFAULT_KEYWORDS,
    val steps: Set<LabStep> = LabStep.all(),
    val selected: Set<String> = emptySet(),
    /** 每个首页、关键词和详情请求的超时。 */
    val stepTimeoutMs: Long = 25_000L,
    /** 每条线路的首帧及进度观察超时。 */
    val playTimeoutMs: Long = 60_000L,
    val minimumProgressMs: Long = DEFAULT_MINIMUM_PROGRESS_MS,
    /** 所有线路合计预算；用尽则 INCOMPLETE，不伪称已遍历全部线路。 */
    val playBudgetMs: Long = 240_000L,
    /** 地址解析可能包含源自身 30 秒的 WebView 嗅探，独立预算仍低于 JS 默认 50 秒。 */
    val playInfoTimeoutMs: Long = 45_000L,
    /** 每个固定关键词/线路首集最多允许提交几次人工验证（硬上限为 3）。 */
    val maxManualAttempts: Int = 3,
    /** 人工界面独立截止；仅用于接力配置与证据，不占网络/播放器预算。 */
    val manualTimeoutMs: Long = 600_000L,
) {
    init {
        require(stepTimeoutMs > 0 && playTimeoutMs > 0 && playBudgetMs > 0 && playInfoTimeoutMs > 0)
        require(minimumProgressMs >= DEFAULT_MINIMUM_PROGRESS_MS)
        require(maxManualAttempts in 1..3)
        require(manualTimeoutMs > 0)
    }
    companion object {
        const val DEFAULT_MINIMUM_PROGRESS_MS = 3_000L
        val DEFAULT_KEYWORDS = listOf("孤独摇滚", "孤獨搖滾", "ぼっち・ざ・ろっく！", "Bocchi the Rock")
    }
}

data class LabRun(
    val config: LabConfig,
    val items: List<LabSourceResult> = emptyList(),
    val total: Int = 0,
    val runningKey: String? = null,
    val runningStep: LabStep? = null,
    val finishedAt: Long? = null,
    val evidenceContext: LabEvidenceContext = LabEvidenceContext(),
) {
    val passed: Int get() = items.count { it.verdict == LabVerdict.PASS }
    val failed: Int get() = items.count { it.verdict == LabVerdict.FAIL }
    val suspect: Int get() = items.count { it.verdict == LabVerdict.SUSPECT }
    val skipped: Int get() = items.count { it.verdict == LabVerdict.SKIP }
    fun sortedItems(): List<LabSourceResult> = items.sortedWith(compareBy({ it.verdict.severity }, { it.label }))
    /** 取消时尚未开始或未完整执行的源也需要纳入重试。 */
    fun retrySourceKeys(): Set<String> = config.selected - items.filter { it.verdict == LabVerdict.PASS }.map { it.key }.toSet()
}

/** 稳定的准入证据 JSON；不导出媒体临时地址、Cookie 或请求头。 */
fun LabRun.toEvidenceJson(): String {
    val document = linkedMapOf<String, Any>(
        "schemaVersion" to 1,
        "protocolVersion" to "source-acceptance-v1",
        "generatedAt" to labIsoTime(finishedAt ?: System.currentTimeMillis()),
        "context" to evidenceContext,
        "config" to config,
        "results" to items.map { item ->
            linkedMapOf<String, Any?>(
                "key" to item.key, "label" to item.label,
                "versionName" to item.versionName, "versionCode" to item.versionCode,
                "sourceSha256" to item.sourceSha256,
                "startedAt" to labIsoTime(item.startedAt), "finishedAt" to labIsoTime(item.finishedAt),
                "verdict" to item.verdict, "acceptance" to item.acceptance,
                "steps" to item.steps, "searchAttempts" to item.searchAttempts,
                "selection" to item.selection, "lineAttempts" to item.lineAttempts,
                "playBudgetExhausted" to item.playBudgetExhausted,
                "minimumProgressMs" to item.minimumProgressMs, "totalLineCount" to item.totalLineCount,
                "verificationEvents" to item.verificationEvents, "manualWaitMs" to item.manualWaitMs,
            )
        },
    )
    return GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create().toJson(document)
}

internal fun labIsoTime(time: Long): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(time))

private fun markdownCell(text: String): String = text.replace("|", "\\|").replace("\n", " ")

fun LabRun.toMarkdown(): String = buildString {
    append("# 源体检报告\n\n")
    append("- 别名顺序：").append(config.keywords.joinToString(" → ")).append('\n')
    append("- 播放要求：真实首帧 + 进度推进 ≥ ").append(config.minimumProgressMs).append("ms\n")
    append("- 结果：通过 ").append(passed).append(" · 待复核 ").append(suspect)
        .append(" · 失败 ").append(failed).append(" · 跳过 ").append(skipped)
        .append("（已完成 ").append(items.size).append('/').append(total).append("）\n\n")
    append("| 源 | key | 结论 | 准入候选 | 首页 | 搜索 | 播放 |\n")
    append("| --- | --- | --- | --- | --- | --- | --- |\n")
    for (item in sortedItems()) {
        append("| ").append(markdownCell(item.label)).append(" | ").append(markdownCell(item.key))
            .append(" | ").append(item.verdict.label).append(" | ").append(item.acceptance).append(" |")
        LabStep.entries.forEach { step ->
            val result = item.step(step)
            append(' ').append(result?.let { "${it.verdict.label} · ${it.elapsedMs}ms" } ?: "-").append(" |")
        }
        append('\n')
    }
    append("\n## 证据明细\n\n")
    for (item in sortedItems()) {
        append("### ").append(item.label).append(" (").append(item.key).append(")\n\n")
        append("- 源版本：").append(item.versionName).append(" (").append(item.versionCode).append(")\n")
        append("- SHA-256：").append(item.sourceSha256.ifBlank { "未记录，不可准入" }).append('\n')
        append("- 时间：").append(labIsoTime(item.startedAt)).append(" → ").append(labIsoTime(item.finishedAt)).append('\n')
        item.steps.forEach { append("- ").append(it.step.label).append("：").append(it.detail).append('\n') }
        item.lineAttempts.forEach {
            append("- 线路 ").append(it.lineIndex + 1).append("「").append(it.lineLabel).append("」/")
                .append(it.episodeLabel).append("：").append(it.status)
                .append("，首帧=").append(it.firstFrame).append("，推进=").append(it.progressMs)
                .append("ms，").append(it.videoWidth).append('x').append(it.videoHeight)
                .append("，").append(it.detail).append('\n')
        }
        if (item.verificationEvents.isNotEmpty()) {
            append("- 人工等待：").append(item.manualWaitMs).append("ms（不计机器预算）\n")
            item.verificationEvents.forEach {
                append("- 验证 ").append(it.step.label).append(" #").append(it.attempt)
                    .append(' ').append(it.type).append("：").append(it.decision)
                    .append("，等待=").append(it.waitMs).append("ms，重试=").append(it.retryOutcome).append('\n')
            }
        }
        append('\n')
    }
}
