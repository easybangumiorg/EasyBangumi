package com.heyanle.easybangumi4.v2

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainV2ActivityManifestTest {

    @Test
    fun v2ActivityIsPrivateSingleTaskAndHandlesRotation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val activityInfo = context.packageManager.getActivityInfo(
            ComponentName(context, MainV2Activity::class.java),
            0,
        )
        val rotationConfigChanges =
            ActivityInfo.CONFIG_ORIENTATION or ActivityInfo.CONFIG_SCREEN_SIZE

        assertFalse(activityInfo.exported)
        assertEquals(ActivityInfo.LAUNCH_SINGLE_TASK, activityInfo.launchMode)
        assertEquals(
            rotationConfigChanges,
            activityInfo.configChanges and rotationConfigChanges,
        )
    }
}
