package com.heyanle.easybangumi4.sourcelab

import androidx.test.platform.app.InstrumentationRegistry

/** OEM systems may deny ActivityScenario's background launch after instrumentation restarts the app. */
internal fun foregroundSourceLabForDeviceTests() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val target = instrumentation.targetContext.packageName
    require(target == "com.heyanle.easybangumi4.sourcelab") { "Use the isolated source acceptance package" }
    // Start only the normal exported app entry point. No settings or permissions are changed.
    val command = "am start -W -n $target/com.heyanle.easybangumi4.splash.SplashActivity"
    instrumentation.uiAutomation.executeShellCommand(command).use { descriptor ->
        android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { reader ->
            val result = reader.readText()
            check(!result.contains("Error:") && !result.contains("Exception")) {
                "Cannot foreground source acceptance app through its launcher"
            }
        }
    }
    instrumentation.waitForIdleSync()
}
