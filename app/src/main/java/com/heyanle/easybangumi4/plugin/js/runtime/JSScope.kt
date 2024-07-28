package com.heyanle.easybangumi4.plugin.js.runtime

import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.Context as JSContext

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSScope(
    private val jsRuntime: JSRuntime
) {

    // 需要在 runtime 里初始化
    private val scriptable: Scriptable by lazy {
        val ctx = JSRuntime.JSContextLocal.get() ?: throw IllegalStateException("JSContext is null")
        val scope = JSRuntime.JSTopScopeLocal.get() ?: throw IllegalStateException("JSTopScope is null")
        ctx.newObject(scope)
    }


    fun postWithScope(block: (JSContext, Scriptable) -> Unit) {
        jsRuntime.postWithScope { ctx, scope ->
            block(ctx, scriptable)
        }
    }

    suspend fun <R> runWithScope(block: (JSContext, Scriptable) -> R) : R? {
        return jsRuntime.runWithScope { ctx, scope ->
            block(ctx, scriptable)
        }
    }


}