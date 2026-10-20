package com.heyanle.easybangumi4.plugin.source.js.component

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.Source
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSRuntime
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.utils.WebViewProxyKtWrapper
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.KClass

/** Runs the actual component overloads and Rhino runtime against local proxy contents only. */
@RunWith(AndroidJUnit4::class)
class JSVerificationWebProxyBridgeInstrumentedTest {
    @Test
    fun searchResumeUsesDefaultArgumentBridgeAndReleasesOriginalProxy() = runBlocking {
        searchResume(repeatChallenge = false)
    }

    @Test
    fun searchRepeatedChallengeKeepsOriginalProxyForNextSubmission() = runBlocking {
        searchResume(repeatChallenge = true)
    }

    @Test
    fun playResumeUsesDefaultArgumentBridgeAndReleasesOriginalProxy() = runBlocking {
        playResume(repeatChallenge = false)
    }

    @Test
    fun playRepeatedChallengeKeepsOriginalProxyForNextSubmission() = runBlocking {
        playResume(repeatChallenge = true)
    }

    private suspend fun searchResume(repeatChallenge: Boolean) = withTimeout(10_000) {
        val fixture = Fixture()
        try {
            fixture.init(repeatChallenge)
            val component = requireNotNull(JSSearchComponent.of(fixture.scope)).apply {
                innerSource = TEST_SOURCE
                setWebProxyManager(fixture.manager)
            }
            val initial = searchError(component.search(4, "target"))
            assertEquals(4, initial.request.key)
            assertEquals("target", initial.request.keyword)
            assertEquals(TEST_SOURCE.key, initial.request.source)
            assertSame(fixture.proxy, (initial.verificationParam as VerificationParam.WebView).iWebProxy)
            assertFalse(fixture.proxy.closed.isCompleted)
            if (repeatChallenge) {
                val repeated = searchError(component.search(4, "target", VerificationResult.WebView(fixture.proxy)))
                assertEquals(initial.request, repeated.request)
                assertSame(fixture.proxy, (repeated.verificationParam as VerificationParam.WebView).iWebProxy)
                assertFalse("A renewed challenge must not close the retained session", fixture.proxy.closed.isCompleted)
                fixture.allowResume()
            }
            val result = component.search(4, "target", VerificationResult.WebView(fixture.proxy))
            assertTrue(result is SourceResult.Complete)
            assertEquals("local-hit", (result as SourceResult.Complete).data.second.single().id)
            fixture.proxy.closed.await()
            fixture.assertBridgeCalls(if (repeatChallenge) 2 else 1)
        } finally { fixture.release() }
    }

    private suspend fun playResume(repeatChallenge: Boolean) = withTimeout(10_000) {
        val fixture = Fixture()
        try {
            fixture.init(repeatChallenge)
            val component = requireNotNull(JSPlayComponent.of(fixture.scope)).apply {
                innerSource = TEST_SOURCE
                setWebProxyManager(fixture.manager)
            }
            val summary = CartoonSummary("local-title", TEST_SOURCE.key)
            val episode = Episode("first-episode", "First", 1)
            val line = PlayLine("local-line", "Local", arrayListOf(episode))
            val initial = playError(component.getPlayInfo(summary, line, episode, canCache = false))
            assertSame(summary, initial.request?.summary)
            assertSame(line, initial.request?.playLine)
            assertSame(episode, initial.request?.episode)
            assertSame(fixture.proxy, (initial.verificationParam as VerificationParam.WebView).iWebProxy)
            assertFalse(fixture.proxy.closed.isCompleted)
            if (repeatChallenge) {
                val repeated = playError(component.getPlayInfo(summary, line, episode, VerificationResult.WebView(fixture.proxy), canCache = false))
                assertEquals(initial.request, repeated.request)
                assertSame(fixture.proxy, (repeated.verificationParam as VerificationParam.WebView).iWebProxy)
                assertFalse("A renewed challenge must not close the retained session", fixture.proxy.closed.isCompleted)
                fixture.allowResume()
            }
            val result = component.getPlayInfo(summary, line, episode, VerificationResult.WebView(fixture.proxy), canCache = false)
            assertTrue(result is SourceResult.Complete)
            assertEquals("https://example.invalid/local-only.m3u8", (result as SourceResult.Complete).data.uri)
            fixture.proxy.closed.await()
            fixture.assertBridgeCalls(if (repeatChallenge) 2 else 1)
        } finally { fixture.release() }
    }

    private fun searchError(result: SourceResult<*>): SearchNeedVerificationBusinessException =
        ((result as SourceResult.Error).throwable as ParserException).exception as SearchNeedVerificationBusinessException

    private fun playError(result: SourceResult<*>): PlayInfoNeedVerificationBusinessException =
        ((result as SourceResult.Error).throwable as ParserException).exception as PlayInfoNeedVerificationBusinessException

    private class Fixture {
        val runtime = JSRuntime().apply { init() }
        val scope = JSScope(runtime)
        val proxy = LocalProxy()
        val manager = WebProxyManager()

        suspend fun init(repeatChallenge: Boolean) {
            scope.requestRunWithScope { context, scriptable ->
                scriptable.put("initialProxy", scriptable, WebViewProxyKtWrapper(proxy))
                scriptable.put("repeatChallenge", scriptable, repeatChallenge)
                scriptable.put("searchResult", scriptable, null to arrayListOf(CartoonCoverImpl("local-hit", TEST_SOURCE.key, "", "Local")))
                scriptable.put("playerInfo", scriptable, PlayerInfo(uri = "https://example.invalid/local-only.m3u8"))
                context.evaluateString(scriptable, """
                    function checkSession(proxy) {
                        if (!proxy.waitingForPageLoaded()) throw new Error("page unavailable");
                        if (proxy.getContentWithIframe() != "validated local contents") throw new Error("session changed");
                        if (repeatChallenge) proxy.needUserCheck("local challenge still pending");
                    }
                    function SearchComponent_search(page, keyword) {
                        initialProxy.needUserCheck("local initial challenge");
                    }
                    function SearchComponent_searchWithCheck(page, keyword, proxy) {
                        if (page != 4 || keyword != "target") throw new Error("search request changed");
                        checkSession(proxy);
                        return searchResult;
                    }
                    function PlayComponent_getPlayInfo(summary, line, episode) {
                        initialProxy.needUserCheck("local initial challenge");
                    }
                    function PlayComponent_getPlayInfoWithCheck(summary, line, episode, proxy) {
                        if (summary.id != "local-title" || line.id != "local-line" || episode.id != "first-episode") {
                            throw new Error("play request changed");
                        }
                        checkSession(proxy);
                        return playerInfo;
                    }
                """.trimIndent(), "local-verification-bridge", 1, null)
            }
        }

        suspend fun allowResume() {
            scope.requestRunWithScope { _, scriptable -> scriptable.put("repeatChallenge", scriptable, false) }
        }

        fun assertBridgeCalls(expected: Int) {
            assertEquals(List(expected) { 5_000L }, proxy.pageTimeouts)
            assertEquals(List(expected) { 5_000L }, proxy.contentTimeouts)
            assertEquals("Resuming must preserve the page without reloading", 0, proxy.loads)
            assertEquals(1, proxy.closes)
        }

        fun release() {
            manager.close()
            runtime.release()
        }
    }

    /** No network, real captcha, or Activity; verifies the exact proxy supplied to Rhino. */
    class LocalProxy : IWebProxy {
        val pageTimeouts = ArrayList<Long>()
        val contentTimeouts = ArrayList<Long>()
        val closed = CompletableDeferred<Unit>()
        var loads = 0
        var closes = 0
        override suspend fun href(url: String, cleanLoaded: Boolean) { loads++ }
        override suspend fun loadUrl(url: String, userAgent: String?, headers: Map<String, String>, interceptResRegex: String?, needBlob: Boolean) { loads++ }
        override suspend fun waitingForPageLoaded(timeout: Long): Boolean { pageTimeouts.add(timeout); return true }
        override suspend fun waitingForResourceLoaded(urlRegex: String, sticky: Boolean, timeout: Long): String? = null
        override suspend fun waitingForBlobText(urlRegex: String?, textRegex: String?, sticky: Boolean, timeout: Long): Pair<String?, String?>? = null
        override suspend fun getContent(timeout: Long): String? = "validated local contents"
        override suspend fun getContentWithIframe(timeout: Long): String { contentTimeouts.add(timeout); return "validated local contents" }
        override suspend fun executeJavaScript(script: String, delay: Long): String? = null
        override fun addToWindow(show: Boolean) = Unit
        override fun getWebView(): WebView? = null
        override fun close() { closes++; closed.complete(Unit) }
    }

    companion object {
        private val TEST_SOURCE = object : Source {
            override val key = "local.verification.bridge"
            override val label = "Local verification bridge"
            override val version = "1.0"
            override val versionCode = 1
            override val describe: String? = null
            override fun register(): List<KClass<*>> = emptyList()
        }
    }
}
