package com.heyanle.easybangumi4.plugin.source.js.runtime

import com.heyanle.easybangumi4.plugin.source.js.utils.JSContext
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

    // 闇€瑕佸湪 runtime 閲屽垵濮嬪寲
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
        // 鏈夌殑鍡呮帰宸ュ叿瓒呮椂鏃堕棿鏄?20s锛堟湁鐨勬簮浼氬欢杩?10s 鍔犺浇锛夛紝杩欓噷闇€瑕佸姞楗卞拰 buffer
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