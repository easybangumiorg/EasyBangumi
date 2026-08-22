package com.heyanle.easybangumi4.cartoon.story.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadTaskSwitchPolicyTest {

    @Test
    fun `failed or paused quick task can switch to available different engine`() {
        assertTrue(
            DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = true,
                currentEngineId = "aria",
                targetEngineId = "okhttp-direct",
                targetEngineAvailable = true,
                runtimeReplaceable = true,
            )
        )
    }

    @Test
    fun `full running unknown or same engine cannot switch`() {
        assertFalse(
            DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = false,
                currentEngineId = "aria",
                targetEngineId = "okhttp-direct",
                targetEngineAvailable = true,
                runtimeReplaceable = true,
            )
        )
        assertFalse(
            DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = true,
                currentEngineId = "aria",
                targetEngineId = "aria",
                targetEngineAvailable = true,
                runtimeReplaceable = true,
            )
        )
        assertFalse(
            DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = true,
                currentEngineId = "aria",
                targetEngineId = "removed",
                targetEngineAvailable = false,
                runtimeReplaceable = true,
            )
        )
        assertFalse(
            DownloadTaskSwitchPolicy.canSwitchEngine(
                isQuickMode = true,
                currentEngineId = "aria",
                targetEngineId = "okhttp-direct",
                targetEngineAvailable = true,
                runtimeReplaceable = false,
            )
        )
    }
}
