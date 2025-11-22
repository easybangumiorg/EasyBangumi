package org.easybangumi.next.rhino

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.mozilla.javascript.Scriptable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.atomics.AtomicArray

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class RhinoScope(
    private val rhinoRuntime: RhinoRuntime
) {

    // 需要在 runtime 里初始化
    private val scriptable: Scriptable by lazy {
        val ctx = RhinoRuntime.RhinoContextLocal.get() ?: throw IllegalStateException("JSContext is null")
        val scope = RhinoRuntime.JSTopScopeLocal.get() ?: throw IllegalStateException("JSTopScope is null")
        ctx.newObject(scope)
    }

    private val isInit = AtomicBoolean(false)
    private val isRelease = AtomicBoolean(false)

    suspend fun init(): Boolean {
        if (isInit.compareAndSet(false, true)) {
            return rhinoRuntime.lock()
        }
        return false
    }

    fun release() {
        if (isRelease.compareAndSet(false, true)) {
            rhinoRuntime.unlock()
        }
    }


    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    fun postWithScope(block: (RhinoContext, Scriptable) -> Unit) {
        rhinoRuntime.postWithScope { ctx, scope ->
            block(ctx, scriptable)
        }
    }

    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    suspend fun <R> runWithScope(block: suspend (RhinoContext, Scriptable) -> R) : R? {
        return rhinoRuntime.runWithScope { ctx, scope ->
            block(ctx, scriptable)
        }
    }

    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    suspend fun <R> requestRunWithScope(
        // 有的嗅探工具超时时间是 20s（有的源会延迟 10s 加载），这里需要加饱和 buffer
        timeout: Long = 30000L,
        block: (RhinoContext, Scriptable) -> R,
    ) : R {
        return withTimeout(timeout) {
            rhinoRuntime.runWithScope { ctx, scope ->
                block(ctx, scriptable)
            } ?: throw RhinoScopeException("jsContext or jsScope is null")
        }
    }

    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    suspend fun findFunction(
        name: String,
    ) : RhinoFunction? {
        return rhinoRuntime.runWithScope { ctx, scope ->
            val func = scope.get(name, scope)
            func as? RhinoFunction
        }
    }

    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    suspend fun <R> callFunction(
        rhinoFunction: RhinoFunction,
        vararg args: Any?,
    ) : R {
        return rhinoRuntime.runWithScope { ctx, scope ->
            rhinoFunction.call(ctx, scope, scriptable, args)?.jsUnwrap() as? R
                ?: throw RhinoScopeException("Function $rhinoFunction return null")
        } ?: throw RhinoScopeException("Run with scope return null")
    }




}

class RhinoScopeException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}