package com.heyanle.easybangumi4.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class WebViewCompatibilityModeGuardTest {

    @Test
    fun nonEmptyParentTriggersCompatibilityAndClearDeletesTheParent() {
        val root = Files.createTempDirectory("webview-compat-guard").toFile()
        try {
            WebViewCompatibilityModeGuard.initialize(root)
            assertFalse(WebViewCompatibilityModeGuard.shouldEnableCompatibilityMode())

            root.resolve("application_package_name.tag").mkdirs()
            assertTrue(WebViewCompatibilityModeGuard.shouldEnableCompatibilityMode())

            assertTrue(WebViewCompatibilityModeGuard.clear())
            assertFalse(root.exists())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun openAndCloseSanitizeTheTemporaryTag() {
        val root = Files.createTempDirectory("webview-compat-guard").toFile()
        try {
            WebViewCompatibilityModeGuard.initialize(root)

            assertTrue(WebViewCompatibilityModeGuard.open("home/load page"))
            val tagDirectory = root.resolve("home_load_page.tag")
            assertTrue(tagDirectory.isDirectory)

            assertTrue(WebViewCompatibilityModeGuard.close("home/load page"))
            assertFalse(tagDirectory.exists())
        } finally {
            root.deleteRecursively()
        }
    }

}
