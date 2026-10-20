package com.heyanle.easybangumi4.sourcelab

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.KeyEvent
import android.view.TextureView
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.v2.MainV2Activity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Uses local challenges only; it never solves or bypasses a real site's captcha. */
@RunWith(AndroidJUnit4::class)
class SourceLabVerificationHostInstrumentedTest {
    @Test
    fun completedWebViewReturnsOriginalProxyAndParentAndKeepsSurface() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            withTimeout(15_000) { fixture.surfaceReady.await() }
            val originalParams = withContext(Dispatchers.Main) { fixture.proxy.view.layoutParams }
            val originalTexture = withContext(Dispatchers.Main) { fixture.texture.surfaceTexture }
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            withContext(Dispatchers.Main) {
                assertTrue(fixture.proxy.view.parent !== fixture.root)
                assertSame(fixture.root, fixture.texture.parent)
                val webView = fixture.proxy.view
                val decor = webView.rootView
                assertTrue("Verification page must use at least 90% of dialog width",
                    webView.width >= decor.width * 0.9f)
                if (decor.height > decor.width) {
                    assertTrue("Portrait verification page must use at least 70% of dialog height",
                        webView.height >= decor.height * 0.7f)
                }
            }
            assertActionTouchTargets()
            onView(withText("验证完成，重试")).inRoot(isDialog()).perform(click())
            val response = withTimeout(5_000) { waiter.await() } as LabVerificationResponse.Completed
            assertSame(fixture.proxy, (response.result as VerificationResult.WebView).iWebProxy)
            withContext(Dispatchers.Main) {
                assertSame(fixture.root, fixture.proxy.view.parent)
                assertSame(originalParams, fixture.proxy.view.layoutParams)
                assertEquals(0, fixture.root.indexOfChild(fixture.proxy.view))
                assertSame(fixture.root, fixture.texture.parent)
                assertTrue(fixture.texture.isAvailable)
                assertSame(originalTexture, fixture.texture.surfaceTexture)
                assertEquals(0, fixture.proxy.closeCount)
            }
            assertNull(host.pending.value)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun imageCaptchaRequiresInputAndReturnsOnlyUserSuppliedAnswer() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val bitmap = Bitmap.createBitmap(24, 16, Bitmap.Config.ARGB_8888)
            val request = fixture.webRequest().copy(
                verificationParam = VerificationParam.ImageCaptcha(bitmap, title = "本地测试验证码"),
            )
            val waiter = async(Dispatchers.Default) { host.verify(request) }
            awaitPending(host)
            onView(withText("验证完成，重试")).inRoot(isDialog()).perform(click())
            assertFalse(waiter.isCompleted)
            onView(withContentDescription("人工验证码答案")).inRoot(isDialog())
                .perform(click(), replaceText("test-answer"))
            InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(500L, 5_000L)
            onView(withContentDescription("人工验证码答案")).inRoot(isDialog()).check { view, error ->
                error?.let { throw it }
                assertTrue("Captcha input must open the keyboard",
                    view != null && ViewCompat.getRootWindowInsets(view)
                        ?.isVisible(WindowInsetsCompat.Type.ime()) == true)
            }
            for (label in listOf("验证完成，重试", "跳过此源", "停止测试")) {
                onView(withText(label)).inRoot(isDialog()).check(matches(isCompletelyDisplayed()))
            }
            onView(withContentDescription("人工验证码答案")).inRoot(isDialog()).perform(closeSoftKeyboard())
            onView(withText("验证完成，重试")).inRoot(isDialog()).perform(click())
            val response = withTimeout(5_000) { waiter.await() } as LabVerificationResponse.Completed
            assertEquals("test-answer", (response.result as VerificationResult.ImageCaptcha).input)
            assertNull(host.pending.value)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun skipClosesUnreturnedProxyAndDoesNotRestoreIt() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            onView(withText("跳过此源")).inRoot(isDialog()).perform(click())
            assertSame(LabVerificationResponse.Skipped, withTimeout(5_000) { waiter.await() })
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun backDismissesAsSkip() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            onView(isRoot()).inRoot(isDialog()).perform(pressKey(KeyEvent.KEYCODE_BACK))
            assertSame(LabVerificationResponse.Skipped, withTimeout(5_000) { waiter.await() })
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun stopReturnsBatchCancellationAndReleasesProxy() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            onView(withText("停止测试")).inRoot(isDialog()).perform(click())
            assertSame(LabVerificationResponse.Cancelled, withTimeout(5_000) { waiter.await() })
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun callerCancellationClosesDialogAndProxyWithoutHanging() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            withTimeout(5_000) { waiter.cancelAndJoin() }
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun humanTimeoutIsExpiredAndNeverCompletedOrUserSkipped() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity }, manualTimeoutMs = 150L)
        try {
            assertSame(LabVerificationResponse.Expired, withTimeout(5_000) { host.verify(fixture.webRequest()) })
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun activityLeavingForegroundEndsWaitAsUnavailable() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            fixture.scenario.moveToState(Lifecycle.State.CREATED)
            assertSame(LabVerificationResponse.Unavailable, withTimeout(5_000) { waiter.await() })
            assertReleased(host, fixture)
        } finally {
            host.close()
            fixture.close()
        }
    }

    @Test
    fun closeHostStopsCurrentWaitAndRejectsFutureChallenges() = runBlocking {
        val fixture = Fixture()
        val host = SourceLabVerificationHost({ fixture.activity })
        try {
            val waiter = async(Dispatchers.Default) { host.verify(fixture.webRequest()) }
            awaitPending(host)
            host.close()
            assertSame(LabVerificationResponse.Cancelled, withTimeout(5_000) { waiter.await() })
            assertReleased(host, fixture)
            assertSame(LabVerificationResponse.Cancelled, host.verify(fixture.webRequest()))
        } finally {
            host.close()
            fixture.close()
        }
    }

    private suspend fun awaitPending(host: SourceLabVerificationHost) {
        withTimeout(5_000) { host.pending.first { it != null } }
        // A pending request precedes Dialog.show(), and the Activity can remain the default root
        // until the dialog receives window focus. Espresso bounds its wait for this specific root
        // to appear and finish layout; never select the Activity root while waiting for the dialog.
        onView(isRoot()).inRoot(isDialog()).check { view, error ->
            error?.let { throw it }
            assertTrue("Verification dialog must be visible", view?.isShown == true)
            assertTrue("Verification dialog must own window focus", view?.hasWindowFocus() == true)
            assertFalse("Verification dialog must finish layout", view?.isLayoutRequested == true)
        }
        // Window focus/layout can precede the platform's input window transaction. Wait for a
        // bounded quiet interval in accessibility window events before injecting a real touch.
        InstrumentationRegistry.getInstrumentation().uiAutomation.waitForIdle(500L, 5_000L)
    }

    private fun assertActionTouchTargets() {
        for (label in listOf("验证完成，重试", "跳过此源", "停止测试")) {
            onView(withText(label)).inRoot(isDialog()).check { view, error ->
                error?.let { throw it }
                requireNotNull(view)
                val minimum = 48f * view.resources.displayMetrics.density
                assertTrue("$label must provide a 48dp touch target", view.height >= minimum)
            }
        }
    }

    private suspend fun assertReleased(host: SourceLabVerificationHost, fixture: Fixture) {
        assertNull(host.pending.value)
        withContext(Dispatchers.Main) {
            assertNull(fixture.proxy.view.parent)
            assertEquals(1, fixture.proxy.closeCount)
        }
    }

    private class Fixture {
        val scenario = run {
            foregroundSourceLabForDeviceTests()
            ActivityScenario.launch(MainV2Activity::class.java)
        }
        lateinit var activity: MainV2Activity
        lateinit var root: FrameLayout
        lateinit var proxy: LocalWebProxy
        lateinit var texture: TextureView
        val surfaceReady = CompletableDeferred<Unit>()

        init {
            scenario.onActivity { current ->
                activity = current
                root = FrameLayout(current)
                proxy = LocalWebProxy(WebView(current))
                root.addView(proxy.view, FrameLayout.LayoutParams(16, 16))
                texture = TextureView(current).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            surfaceReady.complete(Unit)
                        }
                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = true
                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                    }
                }
                root.addView(texture, FrameLayout.LayoutParams(160, 90))
                current.setContentView(root)
                proxy.view.loadDataWithBaseURL(null, "<p>本地验证测试</p>", "text/html", "UTF-8", null)
            }
        }

        fun webRequest() = LabVerificationRequest(
            sourceKey = "local-test", sourceLabel = "本地测试源", step = LabStep.SEARCH,
            keyword = "孤独摇滚", searchPageKey = 1, attempt = 1,
            verificationParam = VerificationParam.WebView(proxy, "请手动操作；仅点击完成不会记为测试成功"),
        )

        suspend fun close() {
            withContext(Dispatchers.Main) { if (proxy.closeCount == 0) proxy.close() }
            scenario.close()
        }
    }

    private class LocalWebProxy(val view: WebView) : IWebProxy {
        var closeCount = 0
        override fun close() {
            if (closeCount > 0) return
            closeCount++
            (view.parent as? ViewGroup)?.removeView(view)
            view.destroy()
        }
        override fun getWebView(): WebView? = view.takeIf { closeCount == 0 }
        override fun addToWindow(show: Boolean) = Unit
        override suspend fun href(url: String, cleanLoaded: Boolean) = Unit
        override suspend fun loadUrl(url: String, userAgent: String?, headers: Map<String, String>, interceptResRegex: String?, needBlob: Boolean) = Unit
        override suspend fun waitingForPageLoaded(timeout: Long) = true
        override suspend fun waitingForResourceLoaded(urlRegex: String, sticky: Boolean, timeout: Long): String? = null
        override suspend fun waitingForBlobText(urlRegex: String?, textRegex: String?, sticky: Boolean, timeout: Long): Pair<String?, String?>? = null
        override suspend fun getContent(timeout: Long): String? = null
        override suspend fun getContentWithIframe(timeout: Long): String? = null
        override suspend fun executeJavaScript(script: String, delay: Long): String? = null
    }
}
