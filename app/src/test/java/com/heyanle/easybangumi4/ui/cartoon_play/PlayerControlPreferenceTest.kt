package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.ui.common.PlayerCutoutInsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerControlPreferenceTest {

    @Test
    fun fullscreenControlPosition_resolvesFixedAndAutomaticSides() {
        assertFalse(
            resolveFullscreenControlOnRight(
                SettingPreferences.FullscreenControlPosition.AUTO,
                automaticSideOnRight = false,
            ),
        )
        assertTrue(
            resolveFullscreenControlOnRight(
                SettingPreferences.FullscreenControlPosition.AUTO,
                automaticSideOnRight = true,
            ),
        )
        assertFalse(
            resolveFullscreenControlOnRight(
                SettingPreferences.FullscreenControlPosition.LEFT,
                automaticSideOnRight = true,
            ),
        )
        assertTrue(
            resolveFullscreenControlOnRight(
                SettingPreferences.FullscreenControlPosition.RIGHT,
                automaticSideOnRight = false,
            ),
        )
    }

    @Test
    fun cutoutAvoidance_autoUsesDetectedSide_disabledUsesNeither_manualUsesBoth() {
        val automatic = PlayerCutoutInsets.Resolved(
            detectedSide = PlayerCutoutInsets.Side.LEFT,
            mode = SettingPreferences.PlayerCutoutAvoidanceMode.AUTO,
            manualWidth = 52.dp,
        )
        assertEquals(PlayerCutoutInsets.SAFE_WIDTH, automatic.paddingFor(PlayerCutoutInsets.Side.LEFT))
        assertEquals(0.dp, automatic.paddingFor(PlayerCutoutInsets.Side.RIGHT))

        val disabled = automatic.copy(mode = SettingPreferences.PlayerCutoutAvoidanceMode.DISABLED)
        assertEquals(0.dp, disabled.paddingFor(PlayerCutoutInsets.Side.LEFT))
        assertEquals(0.dp, disabled.paddingFor(PlayerCutoutInsets.Side.RIGHT))

        val manual = automatic.copy(mode = SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL)
        assertEquals(52.dp, manual.paddingFor(PlayerCutoutInsets.Side.LEFT))
        assertEquals(52.dp, manual.paddingFor(PlayerCutoutInsets.Side.RIGHT))
    }

    @Test
    fun cutoutAvoidance_belowApi28DefaultsToDisabledAndHidesAutomaticMode() {
        val auto = SettingPreferences.PlayerCutoutAvoidanceMode.AUTO
        val disabled = SettingPreferences.PlayerCutoutAvoidanceMode.DISABLED
        val manual = SettingPreferences.PlayerCutoutAvoidanceMode.MANUAL

        assertFalse(SettingPreferences.PlayerCutoutAvoidanceMode.isAutomaticSupported(sdkInt = 27))
        assertEquals(
            disabled,
            SettingPreferences.PlayerCutoutAvoidanceMode.defaultForSdk(sdkInt = 27),
        )
        assertEquals(
            disabled,
            SettingPreferences.PlayerCutoutAvoidanceMode.normalizeForSdk(auto, sdkInt = 27),
        )
        assertEquals(
            listOf(disabled, manual),
            SettingPreferences.PlayerCutoutAvoidanceMode.selectableValues(sdkInt = 27),
        )

        assertTrue(SettingPreferences.PlayerCutoutAvoidanceMode.isAutomaticSupported(sdkInt = 28))
        assertEquals(auto, SettingPreferences.PlayerCutoutAvoidanceMode.defaultForSdk(sdkInt = 28))
        assertEquals(
            auto,
            SettingPreferences.PlayerCutoutAvoidanceMode.normalizeForSdk(auto, sdkInt = 28),
        )
        assertEquals(
            listOf(auto, disabled, manual),
            SettingPreferences.PlayerCutoutAvoidanceMode.selectableValues(sdkInt = 28),
        )
    }
}
