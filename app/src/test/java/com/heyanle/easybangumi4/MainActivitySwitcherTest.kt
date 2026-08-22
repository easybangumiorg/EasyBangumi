package com.heyanle.easybangumi4

import com.heyanle.easybangumi4.v2.MainV2Activity
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivitySwitcherTest {

    @Test
    fun legacyIsSelectedWhenV2IsDisabled() {
        assertEquals(
            MainActivity::class.java,
            MainActivitySwitcher.resolveTargetActivity(useV2Ui = false),
        )
    }

    @Test
    fun v2IsSelectedWhenV2IsEnabled() {
        assertEquals(
            MainV2Activity::class.java,
            MainActivitySwitcher.resolveTargetActivity(useV2Ui = true),
        )
    }
}
