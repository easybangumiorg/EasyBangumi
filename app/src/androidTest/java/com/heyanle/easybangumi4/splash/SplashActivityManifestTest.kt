package com.heyanle.easybangumi4.splash

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashActivityManifestTest {

    @Test
    fun splashActivity_handlesRotationWithoutRecreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val activityInfo = context.packageManager.getActivityInfo(
            ComponentName(context, SplashActivity::class.java),
            0,
        )
        val rotationConfigChanges =
            ActivityInfo.CONFIG_ORIENTATION or ActivityInfo.CONFIG_SCREEN_SIZE

        assertEquals(
            rotationConfigChanges,
            activityInfo.configChanges and rotationConfigChanges,
        )
    }
}
