package com.heyanle.easybangumi4.splash

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.okkv2.core.okkv
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashActivityConfigurationChangeTest {

    private val themeStepVersion = okkv("splash_step_Theme_version", def = -1)
    private val localStepVersion = okkv("splash_step_Local_version", def = -1)
    private val permissionStepVersion = okkv("splash_step_permission_version", def = -1)

    private var previousThemeStepVersion = -1
    private var previousLocalStepVersion = -1
    private var previousPermissionStepVersion = -1
    private var previousSplashCompletely = false

    @Before
    fun showGuide() {
        // SplashGuildController evaluates the pending steps when it is first requested.
        previousThemeStepVersion = themeStepVersion.get()
        previousLocalStepVersion = localStepVersion.get()
        previousPermissionStepVersion = permissionStepVersion.get()
        previousSplashCompletely = SplashActivity.splashCompletely

        themeStepVersion.set(-1)
        localStepVersion.set(-1)
        permissionStepVersion.set(-1)
        SplashActivity.splashCompletely = false
        SplashActivity.lastSplashActivity = null
    }

    @After
    fun restoreGuideState() {
        themeStepVersion.set(previousThemeStepVersion)
        localStepVersion.set(previousLocalStepVersion)
        permissionStepVersion.set(previousPermissionStepVersion)
        SplashActivity.splashCompletely = previousSplashCompletely
        SplashActivity.lastSplashActivity = null
    }

    @Test
    fun recreate_keepsGuideTaskAndRegistersReplacementActivity() {
        val scenario = ActivityScenario.launch(SplashActivity::class.java)
        try {
            lateinit var original: SplashActivity
            scenario.onActivity { activity ->
                original = activity
                assertFalse(activity.isFinishing)
            }

            // ActivityScenario.recreate() follows the same destroy/create lifecycle as rotation.
            scenario.recreate()

            scenario.onActivity { replacement ->
                assertNotSame(original, replacement)
                assertFalse(replacement.isFinishing)
                assertSame(replacement, SplashActivity.lastSplashActivity?.get())
            }
        } finally {
            scenario.close()
        }
    }
}
