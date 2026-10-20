package com.heyanle.easybangumi4.sourcelab

/** 面向用户的诊断结果，不包含内部准入结论、设备信息、异常原文或媒体地址。 */
data class SourceDiagnosticMessage(val title: String, val advice: String)

fun LabVerdict.diagnosticLabel(): String = when (this) {
    LabVerdict.PASS -> "正常"
    LabVerdict.SUSPECT -> "需关注"
    LabVerdict.FAIL -> "异常"
    LabVerdict.SKIP -> "未完成"
}

fun LabSourceResult.diagnosticStep(step: LabStep): SourceDiagnosticMessage {
    val result = this.step(step) ?: return SourceDiagnosticMessage("未执行", "可以重新诊断此源。")
    if (result.verdict == LabVerdict.PASS) {
        return when (step) {
            LabStep.HOME -> SourceDiagnosticMessage("首页加载成功", "已读取首页内容。")
            LabStep.SEARCH -> SourceDiagnosticMessage("搜索可用", "已找到搜索结果。")
            LabStep.PLAY -> SourceDiagnosticMessage("样本可以播放", "已显示画面并播放至少 3 秒；其他作品和剧集可能不同。")
        }
    }
    if (result.verdict == LabVerdict.SKIP) {
        return SourceDiagnosticMessage("未执行", "前面的步骤未完成，可以重新诊断。")
    }
    val verificationNeeded = result.failureKind == LabFailureKind.VERIFICATION ||
        verificationEvents.any { it.step == step && (it.decision != LabVerificationDecision.COMPLETED ||
            it.retryOutcome == LabVerificationRetryOutcome.VERIFICATION_REQUIRED) } ||
        (step == LabStep.SEARCH && searchAttempts.any { it.status == LabSearchStatus.VERIFICATION_REQUIRED }) ||
        (step == LabStep.PLAY && lineAttempts.any { it.status == LabLineStatus.VERIFICATION_REQUIRED })
    if (verificationNeeded) {
        val unavailable = verificationEvents.any {
            it.step == step && it.decision == LabVerificationDecision.UNAVAILABLE
        }
        return SourceDiagnosticMessage("需要手动验证", if (unavailable) {
            "此步骤暂不能在诊断中恢复验证，请在对应源页面完成验证后重试。"
        } else "请在弹出的验证窗口中完成验证，然后重试未通过项。")
    }
    if (step == LabStep.SEARCH && searchAttempts.isNotEmpty() && searchAttempts.all { it.status == LabSearchStatus.EMPTY }) {
        return SourceDiagnosticMessage("没有搜索结果", "可能未收录该作品，换一个关键词再试；不代表整个源不可用。")
    }
    if (step == LabStep.SEARCH && searchAttempts.any { it.status == LabSearchStatus.UNAVAILABLE }) {
        return SourceDiagnosticMessage("此源不支持搜索", "可以尝试从源首页浏览内容。")
    }
    if (step == LabStep.PLAY && playBudgetExhausted) {
        return SourceDiagnosticMessage("播放检查超时", "已尝试 ${lineAttempts.size}/$totalLineCount 条线路，可以稍后重试。")
    }
    val failure = result.failureKind ?: when (step) {
        LabStep.SEARCH -> searchAttempts.firstNotNullOfOrNull { it.failureKind }
            ?: if (searchAttempts.any { it.status == LabSearchStatus.TIMEOUT }) LabFailureKind.TIMEOUT else null
        LabStep.PLAY -> lineAttempts.firstNotNullOfOrNull { it.failureKind }
            ?: if (lineAttempts.any { it.status == LabLineStatus.TIMEOUT }) LabFailureKind.TIMEOUT else null
        LabStep.HOME -> null
    }
    return when (failure) {
        LabFailureKind.NETWORK -> SourceDiagnosticMessage("网络请求失败", "检查网络连接或稍后重试；源网站也可能暂时不可用。")
        LabFailureKind.TIMEOUT -> SourceDiagnosticMessage("请求超时", "检查网络后重试，也可以稍后再试。")
        LabFailureKind.PARSING -> SourceDiagnosticMessage("内容解析失败", "源规则可能已失效，请检查该源是否有更新。")
        LabFailureKind.PLAYBACK -> SourceDiagnosticMessage("播放失败", "已取得播放地址，但没有完成播放。可在播放页尝试其他线路。")
        else -> when (step) {
            LabStep.HOME -> SourceDiagnosticMessage("首页未加载成功", "稍后重试，或检查源配置和源更新。")
            LabStep.SEARCH -> SourceDiagnosticMessage("搜索请求失败", "稍后重试，或检查源配置和源更新。")
            LabStep.PLAY -> if (selection == null) {
                SourceDiagnosticMessage("没有可测试的作品", "先处理首页或搜索的问题，再重新诊断。")
            } else SourceDiagnosticMessage("未完成播放", "作品详情或线路可能不可用，可在播放页尝试其他作品或线路。")
        }
    }
}

fun LabRun.toDiagnosticsReport(): String = buildString {
    append("源诊断报告\n")
    append("已完成 ").append(items.size).append('/').append(total).append(" 个源\n")
    append("本次仅检查所选源的一个播放样本，结果受当前网络和源网站状态影响。\n")
    sortedItems().forEach { item ->
        append('\n').append(item.label).append(" · ").append(item.verdict.diagnosticLabel()).append('\n')
        append("源版本：").append(item.versionName).append('\n')
        LabStep.entries.filter { it in config.steps }.forEach { step ->
            val message = item.diagnosticStep(step)
            append(step.label).append("：").append(message.title).append("。")
                .append(message.advice).append('\n')
        }
    }
}
