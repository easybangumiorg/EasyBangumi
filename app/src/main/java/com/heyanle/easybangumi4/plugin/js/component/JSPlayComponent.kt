package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.js.utils.JSFunction
import com.heyanle.easybangumi4.plugin.js.utils.jsUnwrap
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.plugin.source.utils.network.web.WebProxyManager
import com.heyanle.easybangumi4.source_api.ParserException
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.SearchNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.source_api.component.NeedWebViewCheckExceptionInner
import com.heyanle.easybangumi4.source_api.component.PlayInfoNeedWebViewCheckBusinessException
import com.heyanle.easybangumi4.source_api.component.PlayInfoWebViewCheckParam
import com.heyanle.easybangumi4.source_api.component.SearchWebViewCheckParam
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.source_api.withResult
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
        episode: Episode
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
                    // 有点 hard ，但是不管了，旧版本都要不维护了
                    // 如果需要过验证，需要先把那个 WebView 的生命周期提升
                    // 递归验证
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须要 ParserException 才会透传
                            throw ParserException(message = "need web check", exception = PlayInfoNeedWebViewCheckBusinessException(
                                param = PlayInfoWebViewCheckParam(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips,
                                )
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

    suspend fun getPlayInfoWithCheck(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        iWebProxy: IWebProxy
    ): SourceResult<PlayerInfo> {
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
                    // 有点 hard ，但是不管了，旧版本都要不维护了
                    // 如果需要过验证，需要先把那个 WebView 的生命周期提升
                    // 递归验证
                    if (e is WrappedException) {
                        val e = e.wrappedException
                        if (e is NeedWebViewCheckExceptionInner) {
                            webProxyManager?.removeWebProxy(e.iWebProxy)
                            webProxyManager?.close()
                            // 必须要 ParserException 才会透传
                            throw ParserException(message = "need web check", exception = PlayInfoNeedWebViewCheckBusinessException(
                                param = PlayInfoWebViewCheckParam(
                                    summary = summary,
                                    playLine = playLine,
                                    episode = episode,
                                    iWebProxy = e.iWebProxy,
                                    tips = e.tips,
                                )
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
    return (this as? JSPlayComponent)!!.getPlayInfoWithCheck(
        summary, playLine, episode, iWebProxy
    )
}