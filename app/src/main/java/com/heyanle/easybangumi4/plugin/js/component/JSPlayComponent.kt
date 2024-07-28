package com.heyanle.easybangumi4.plugin.js.component

import com.heyanle.easybangumi4.plugin.js.runtime.JSScope
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.ComponentWrapper
import com.heyanle.easybangumi4.source_api.component.play.PlayComponent
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.source_api.entity.PlayLine
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import org.mozilla.javascript.Function

/**
 * Created by heyanle on 2024/7/28.
 * https://github.com/heyanLE
 */
class JSPlayComponent(
    private val jsScope: JSScope,
    private val getPlayInfo: Function,
): ComponentWrapper(), PlayComponent {

    companion object {
        const val FUNCTION_NAME_GET_PLAY_INFO = "PlayComponent_getPlayInfo"

        suspend fun of (jsScope: JSScope) : JSPlayComponent ? {
            return jsScope.runWithScope { _, scriptable ->
                val getPlayInfo = scriptable.get(FUNCTION_NAME_GET_PLAY_INFO, scriptable) as? Function
                    ?: return@runWithScope null
                return@runWithScope JSPlayComponent(jsScope, getPlayInfo)
            }
        }

    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode
    ): SourceResult<PlayerInfo> {
        TODO("Not yet implemented")
    }
}