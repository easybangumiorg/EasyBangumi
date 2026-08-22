package com.heyanle.easybangumi4.anime4k

internal data class Anime4KDeviceProfile(
    val memoryClassMb: Int,
    val isLowRamDevice: Boolean,
)

internal data class Anime4KSafetyDecision(
    val requestedScale: Int,
    val appliedScale: Int,
    val reason: String? = null,
) {
    val fellBackToAuto: Boolean get() = requestedScale > 0 && requestedScale != appliedScale
}

internal data class Anime4KScaleCapability(
    val supportedScales: Set<Int> = setOf(0, 1, 2),
    val unsupportedReasons: Map<Int, String> = mapOf(4 to "等待视频与 GPU 能力信息"),
)

internal object Anime4KSafetyPolicy {
    private const val FALLBACK_MAX_TEXTURE_SIZE = 4096
    private val ManualScales = listOf(1, 2, 4)

    fun evaluate(
        inputWidth: Int,
        inputHeight: Int,
        displayWidth: Int,
        requestedScale: Int,
        maxTextureSize: Int,
        deviceProfile: Anime4KDeviceProfile,
    ): Anime4KSafetyDecision {
        val safeInputWidth = inputWidth.coerceAtLeast(1)
        val safeInputHeight = inputHeight.coerceAtLeast(1)
        val automaticScale = A4KChain.scaleFor(safeInputWidth, displayWidth, manualScale = 0)
        if (requestedScale <= 0) {
            return Anime4KSafetyDecision(0, automaticScale)
        }

        val textureLimit = maxTextureSize.takeIf { it > 0 } ?: FALLBACK_MAX_TEXTURE_SIZE
        val outputWidth = safeInputWidth.toLong() * requestedScale
        val outputHeight = safeInputHeight.toLong() * requestedScale
        if (outputWidth > textureLimit || outputHeight > textureLimit) {
            return Anime4KSafetyDecision(
                requestedScale = requestedScale,
                appliedScale = automaticScale,
                reason = "${requestedScale}× 输出超过 GPU 最大纹理尺寸 ${textureLimit}px",
            )
        }

        val outputPixels = outputWidth * outputHeight
        val pixelBudget = safeOutputPixelBudget(deviceProfile)
        if (outputPixels > pixelBudget) {
            return Anime4KSafetyDecision(
                requestedScale = requestedScale,
                appliedScale = automaticScale,
                reason = "${requestedScale}× 预计占用过高，超过设备安全渲染预算",
            )
        }
        return Anime4KSafetyDecision(requestedScale, requestedScale)
    }

    fun capability(
        inputWidth: Int,
        inputHeight: Int,
        displayWidth: Int,
        maxTextureSize: Int,
        deviceProfile: Anime4KDeviceProfile,
    ): Anime4KScaleCapability {
        if (inputWidth <= 0 || inputHeight <= 0) return Anime4KScaleCapability()
        val supported = linkedSetOf(0)
        val reasons = linkedMapOf<Int, String>()
        ManualScales.forEach { scale ->
            val decision = evaluate(
                inputWidth = inputWidth,
                inputHeight = inputHeight,
                displayWidth = displayWidth,
                requestedScale = scale,
                maxTextureSize = maxTextureSize,
                deviceProfile = deviceProfile,
            )
            if (decision.fellBackToAuto) {
                reasons[scale] = decision.reason ?: "当前设备不建议使用"
            } else {
                supported += scale
            }
        }
        return Anime4KScaleCapability(supported, reasons)
    }

    private fun safeOutputPixelBudget(profile: Anime4KDeviceProfile): Long {
        val budget = when {
            profile.memoryClassMb <= 256 -> 2_100_000L
            profile.memoryClassMb <= 512 -> 4_200_000L
            else -> 8_300_000L
        }
        return if (profile.isLowRamDevice) minOf(budget, 2_100_000L) else budget
    }
}

internal sealed interface Anime4KSafetyEvent {
    data class Capability(
        val inputWidth: Int,
        val inputHeight: Int,
        val maxTextureSize: Int,
    ) : Anime4KSafetyEvent

    data class AutomaticFallback(val reason: String) : Anime4KSafetyEvent
    data class RenderFailure(val reason: String) : Anime4KSafetyEvent
}
