package com.heyanle.easybangumi4.utils

import org.chromium.base.BuildInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewPackageNameScopeTest {

    @Test
    fun spoofingIsLimitedToChromiumBuildInfoInsideProviderScope() {
        assertFalse(BuildInfo.getAll())

        WebViewPackageNameScope.withSpoofing(enabled = true) {
            assertFalse(WebViewPackageNameScope.shouldSpoofPackageName())
            assertTrue(BuildInfo.getAll())
        }

        assertFalse(BuildInfo.getAll())
    }

    @Test
    fun compatibilityModeKeepsRealPackageNameForChromiumBuildInfo() {
        WebViewPackageNameScope.withSpoofing(enabled = false) {
            assertFalse(BuildInfo.getAll())
        }
    }

    @Test
    fun nestedScopesRestoreTheOuterSpoofingWindow() {
        WebViewPackageNameScope.withSpoofing(enabled = true) {
            WebViewPackageNameScope.withSpoofing(enabled = true) {
                assertTrue(BuildInfo.getAll())
            }
            assertTrue(BuildInfo.getAll())
        }

        assertFalse(BuildInfo.getAll())
    }
}
