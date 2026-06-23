package com.heyanle.easybangumi4.plugin.source.js.component

import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.source.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedVerificationBusinessException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoVerificationRequest
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.plugin.api.withResult
import kotlinx.coroutines.Dispatchers
import org.mozilla.javascript.WrappedException

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSPlayComponent(
    private val jsScope: JSScope,
    private val getPlayInfo: JSFunction,
    private val getPlayInfoWithCheck: JSFunction?
): ComponentWrapper(), PlayComponent, JSBaseComponent {

    companion object {
        const val FUNCTION_NAME_GET_PLAY_INFO = "PlayComponent_getPlayInfo"
        const val FUNCTION_NAME_GET_PLAY_INFO_WITH_CHECK = "PlayComponent_getPlayInfoWithCheck"

        suspend fun of (jsScope: JSScope) : JSPlayComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getPlayInfo = scriptable.get(FUNCTION_NAME_GET_PLAY_INFO, scriptable) as? JSFunction
                    ?: return@runWithScope null
                val getPlayInfoWithCheck = scriptable.get(FUNCTION_NAME_GET_PLAY_INFO_WITH_CHECK, scriptable) as? JSFunction
                return@runWithScope JSPlayComponent(jsScope, getPlayInfo, getPlayInfoWithCheck)
            }
        }

    }

    private var webProxyManager: WebProxyManager? = null

    override fun setWebProxyManager(webProxyManager: WebProxyManager) {
        this.webProxyManager = webProxyManager
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        return withResult(Dispatchers.IO) {
            jsScope.requestRunWithScope { context, scriptable ->
                try {
                    val res = getPlayInfo.call(
                        context, scriptable, scriptable,
                        arrayOf(
                            summary, playLine, episode
                        )
                    ).jsUnwrap() as? PlayerInfo
                    if (res == null) {
                        webProxyManager?.close()
                        throw ParserException("js parse error")
                    }
                    return@requestRunWithScope res.apply {
                        webProxyManager?.close()
                    }
                } catch (e: Exception) {
                    // 有点 hard，但是旧版本暂不继续维护。
                    // 如果需要过验证，需要先把 WebView 的生命周期提升。
                    // 递归验证
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须包装成 ParserException 才会透传
                            throw ParserException(message = "need web check", exception = PlayInfoNeedVerificationBusinessException(
                                request = PlayInfoVerificationRequest(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                ),
                                iWebProxy = e.iWebProxy,
                                tips = e.tips,
                            ))

                        }

                    }
                    webProxyManager?.close()
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        verificationResult: VerificationResult,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        val iWebProxy = (verificationResult as? VerificationResult.WebView)?.iWebProxy
            ?: return SourceResult.Error(ParserException("unsupported verification result"), true)
        if (getPlayInfoWithCheck == null) {
            throw RuntimeException("getPlayInfoWithCheck is null")
        }
        return withResult(Dispatchers.IO) {
            jsScope.requestRunWithScope { context, scriptable ->
                try {
                    val res = getPlayInfoWithCheck?.call(
                        context, scriptable, scriptable,
                        arrayOf(
                            summary, playLine, episode, iWebProxy
                        )
                    ).jsUnwrap() as? PlayerInfo
                    if (res == null) {
                        webProxyManager?.close()
                        throw ParserException("js parse error")
                    }
                    return@requestRunWithScope res.apply {
                        webProxyManager?.close()
                    }
                } catch (e: Exception) {
                    // 有点 hard，但是旧版本暂不继续维护。
                    // 如果需要过验证，需要先把 WebView 的生命周期提升。
                    // 递归验证
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须包装成 ParserException 才会透传
                            throw ParserException(message = "need web check", exception = PlayInfoNeedVerificationBusinessException(
                                request = PlayInfoVerificationRequest(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                ),
                                iWebProxy = e.iWebProxy,
                                tips = e.tips,
                            ))

                        }

                    }
                    webProxyManager?.close()
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}

suspend fun PlayComponent.getPlayInfoWithCheck(
    summary: CartoonSummary,
    playLine: PlayLine,
    episode: Episode,
    iWebProxy: IWebProxy
): SourceResult<PlayerInfo> {
    return getPlayInfo(
        summary, playLine, episode, VerificationResult.WebView(iWebProxy)
    )
}
