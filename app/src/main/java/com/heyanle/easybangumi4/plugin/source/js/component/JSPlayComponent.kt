package com.heyanle.easybangumi4.plugin.source.js.component

import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.source.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.plugin.api.ParserException
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.SearchNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.plugin.api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.plugin.api.component.PlayInfoWebViewCheckParam
import com.heyanle.easybangumi4.plugin.api.component.SearchWebViewCheckParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
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
                    // 鏈夌偣 hard 锛屼絾鏄笉绠′簡锛屾棫鐗堟湰閮借涓嶇淮鎶や簡
                    // 濡傛灉闇€瑕佽繃楠岃瘉锛岄渶瑕佸厛鎶婇偅涓?WebView 鐨勭敓鍛藉懆鏈熸彁鍗?
                    // 閫掑綊楠岃瘉
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 蹇呴』瑕?ParserException 鎵嶄細閫忎紶
                            throw ParserException(message = "need web check", exception = PlayInfoNeedWebViewCheckBusinessException(
                                param = PlayInfoWebViewCheckParam(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips,
                                ),
                                verificationParam = VerificationParam.WebView(e.iWebProxy, e.tips),
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
                    // 鏈夌偣 hard 锛屼絾鏄笉绠′簡锛屾棫鐗堟湰閮借涓嶇淮鎶や簡
                    // 濡傛灉闇€瑕佽繃楠岃瘉锛岄渶瑕佸厛鎶婇偅涓?WebView 鐨勭敓鍛藉懆鏈熸彁鍗?
                    // 閫掑綊楠岃瘉
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 蹇呴』瑕?ParserException 鎵嶄細閫忎紶
                            throw ParserException(message = "need web check", exception = PlayInfoNeedWebViewCheckBusinessException(
                                param = PlayInfoWebViewCheckParam(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips,
                                ),
                                verificationParam = VerificationParam.WebView(e.iWebProxy, e.tips),
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
