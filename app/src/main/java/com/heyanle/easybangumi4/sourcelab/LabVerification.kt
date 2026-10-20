package com.heyanle.easybangumi4.sourcelab

import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import kotlinx.coroutines.CancellationException

/** 仅在内存中接力；验证参数与答案不得进入报告、日志或保存状态。 */
data class LabVerificationRequest(
    val sourceKey: String,
    val sourceLabel: String,
    val step: LabStep,
    val keyword: String? = null,
    val lineIndex: Int? = null,
    val lineId: String? = null,
    val lineLabel: String? = null,
    val episodeId: String? = null,
    val episodeLabel: String? = null,
    val attempt: Int,
    val verificationParam: VerificationParam,
    val searchPageKey: Int? = null,
    val cartoonId: String? = null,
)

fun interface LabVerificationHandler {
    suspend fun verify(request: LabVerificationRequest): LabVerificationResponse
}

sealed interface LabVerificationResponse {
    data class Completed(val result: VerificationResult) : LabVerificationResponse
    data object Skipped : LabVerificationResponse
    data object Cancelled : LabVerificationResponse
    data object Expired : LabVerificationResponse
    data object Unavailable : LabVerificationResponse
}

enum class LabVerificationType { WEB_VIEW, IMAGE_CAPTCHA }
enum class LabVerificationDecision { COMPLETED, SKIPPED, CANCELLED, EXPIRED, UNAVAILABLE, ATTEMPT_LIMIT, REQUEST_MISMATCH }
enum class LabVerificationRetryOutcome { SUCCEEDED, VERIFICATION_REQUIRED, ERROR, TIMEOUT, NOT_RETRIED }

/** 可导出的验证证据只包含请求定位、时间与结果，不含 URL、Cookie、答案及原始异常。 */
data class LabVerificationEvent(
    val sourceKey: String,
    val step: LabStep,
    val type: LabVerificationType,
    val attempt: Int,
    val requestedAt: Long,
    val respondedAt: Long,
    val waitMs: Long,
    val decision: LabVerificationDecision,
    val retryOutcome: LabVerificationRetryOutcome = LabVerificationRetryOutcome.NOT_RETRIED,
    val keyword: String? = null,
    val searchPageKey: Int? = null,
    val cartoonId: String? = null,
    val lineIndex: Int? = null,
    val lineId: String? = null,
    val episodeId: String? = null,
)

/** 仍然是取消信号；调用方可先保存当前源的中断证据，再结束整个批次。 */
class LabVerificationCancelledException(val partialResult: LabSourceResult) :
    CancellationException("源体检人工验证已取消")
