package com.heyanle.easybangumi4.sourcelab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceDiagnosticsReportTest {
    @Test
    fun networkParsingAndPlaybackHaveDifferentAdvice() {
        val cases = mapOf(
            LabFailureKind.NETWORK to "网络请求失败",
            LabFailureKind.TIMEOUT to "请求超时",
            LabFailureKind.PARSING to "内容解析失败",
            LabFailureKind.PLAYBACK to "播放失败",
            LabFailureKind.VERIFICATION to "需要手动验证",
        )
        cases.forEach { (kind, expected) ->
            assertEquals(expected, result(LabStepResult(LabStep.PLAY, LabVerdict.FAIL, 0, "private", kind))
                .diagnosticStep(LabStep.PLAY).title)
        }
    }

    @Test
    fun emptySearchIsNotReportedAsSourceFailure() {
        val item = result(LabStepResult(LabStep.SEARCH, LabVerdict.SUSPECT, 1, "internal admission text"))
            .copy(searchAttempts = listOf(LabSearchAttempt("test", LabSearchStatus.EMPTY, 0, 1, "")))
        assertEquals("没有搜索结果", item.diagnosticStep(LabStep.SEARCH).title)
        assertTrue(item.diagnosticStep(LabStep.SEARCH).advice.contains("不代表整个源不可用"))
    }

    @Test
    fun successfulFallbackLineDoesNotShowEarlierLineFailure() {
        val item = result(LabStepResult(LabStep.PLAY, LabVerdict.PASS, 1, "internal"))
            .copy(lineAttempts = listOf(LabLineAttempt(0, "line", "线路", status = LabLineStatus.ERROR,
                elapsedMs = 1, detail = "decoder failed", failureKind = LabFailureKind.PLAYBACK)))
        assertEquals("样本可以播放", item.diagnosticStep(LabStep.PLAY).title)
    }

    @Test
    fun reportNeverExportsRawExceptionsOrInternalEvidence() {
        val item = result(LabStepResult(LabStep.HOME, LabVerdict.FAIL, 1,
            "Cookie=private-session https://media.test/secret 准入失败", LabFailureKind.NETWORK))
            .copy(sourceSha256 = "PRIVATE_HASH")
        val report = LabRun(LabConfig(), listOf(item), 1).toDiagnosticsReport()
        listOf("private-session", "https://", "准入", "PRIVATE_HASH", "androidVersion").forEach {
            assertFalse(it, report.contains(it))
        }
        assertTrue(report.contains("网络请求失败"))
    }

    @Test
    fun retryIncludesInterruptedAndNotStartedSourcesButSkipsPassedOnes() {
        val run = LabRun(LabConfig(selected = setOf("passed", "failed", "not-started")), listOf(
            result(LabStepResult(LabStep.HOME, LabVerdict.PASS, 1, "")).copy(key = "passed"),
            result(LabStepResult(LabStep.HOME, LabVerdict.FAIL, 1, "")).copy(key = "failed"),
        ), 3)
        assertEquals(setOf("failed", "not-started"), run.retrySourceKeys())
    }

    private fun result(step: LabStepResult) = LabSourceResult("test", "测试源", listOf(step))
}
