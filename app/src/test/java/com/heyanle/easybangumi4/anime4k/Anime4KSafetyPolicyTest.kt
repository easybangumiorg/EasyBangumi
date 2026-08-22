package com.heyanle.easybangumi4.anime4k

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Anime4KSafetyPolicyTest {
    private val normalDevice = Anime4KDeviceProfile(memoryClassMb = 512, isLowRamDevice = false)

    @Test
    fun forced4xFallsBackWhenOutputExceedsTextureLimit() {
        val decision = Anime4KSafetyPolicy.evaluate(
            inputWidth = 1920,
            inputHeight = 1080,
            displayWidth = 1080,
            requestedScale = 4,
            maxTextureSize = 4096,
            deviceProfile = normalDevice,
        )
        assertTrue(decision.fellBackToAuto)
        assertEquals(1, decision.appliedScale)
    }

    @Test
    fun forced4xFallsBackWhenPixelBudgetIsUnsafe() {
        val decision = Anime4KSafetyPolicy.evaluate(
            inputWidth = 1280,
            inputHeight = 720,
            displayWidth = 1080,
            requestedScale = 4,
            maxTextureSize = 16384,
            deviceProfile = normalDevice,
        )
        assertTrue(decision.fellBackToAuto)
    }

    @Test
    fun forced2xRemainsAvailableForSmallVideo() {
        val decision = Anime4KSafetyPolicy.evaluate(
            inputWidth = 854,
            inputHeight = 480,
            displayWidth = 1080,
            requestedScale = 2,
            maxTextureSize = 8192,
            deviceProfile = normalDevice,
        )
        assertFalse(decision.fellBackToAuto)
        assertEquals(2, decision.appliedScale)
    }

    @Test
    fun capabilityHidesUnsafe4xBeforeSelection() {
        val capability = Anime4KSafetyPolicy.capability(
            inputWidth = 1920,
            inputHeight = 1080,
            displayWidth = 1080,
            maxTextureSize = 8192,
            deviceProfile = normalDevice,
        )
        assertFalse(4 in capability.supportedScales)
        assertTrue(capability.unsupportedReasons.containsKey(4))
    }
}
