package com.heyanle.easybangumi4.plugin.js.runtime

import com.heyanle.easybangumi4.plugin.js.utils.JSContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.mozilla.javascript.Scriptable
import kotlin.jvm.Throws

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

    @Throws(TimeoutCancellationException::class, JSScopeException::class, Exception::class)
    suspend fun <R> requestRunWithScope(
        // 有的嗅探工具超时时间是 20s（有的源会延迟 10s 加载），这里需要加饱和 buffer
        timeout: Long = 50000L,
        block: (JSContext, Scriptable) -> R,
    ) : R {
        return withTimeout(timeout) {
            jsRuntime.runWithScope { ctx, scope ->
                block(ctx, scriptable)
            } ?: throw JSScopeException("jsContext or jsScope is null")
        }
    }


}

class JSScopeException(msg: String) : Exception(msg)