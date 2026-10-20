package com.heyanle.easybangumi4.plugin.source.js.runtime

import com.heyanle.easybangumi4.plugin.source.js.utils.JSContext
import com.heyanle.easybangumi4.utils.PostThread
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject


/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSRuntime {

    companion object {
        val JSContextLocal = ThreadLocal<JSContext>()
        val JSTopScopeLocal = ThreadLocal<ScriptableObject> ()
    }

    private val postThread = PostThread("JSRuntime")

    fun init(){
        postThread.post {
            println("JSRuntime init")
            val context = JSContext.enter()
            // Android 只能运行解释模式
            context.optimizationLevel = -1
            context.languageVersion = JSContext.VERSION_ES6
            JSContextLocal.set(context)
            val scope: ScriptableObject = ImporterTopLevel(context)
            context.initStandardObjects(scope)
            context.newObject(scope)
            JSTopScopeLocal.set(scope)
        }
    }

    fun release (){
        postThread.release()
    }

    fun postWithScope(block: (JSContext, ScriptableObject) -> Unit) {
        postThread.post {
            val ctx = JSContextLocal.get()
            val scope = JSTopScopeLocal.get()
            if (ctx == null || scope == null) {
                return@post
            }
            block(ctx, scope)
        }
    }


    suspend fun <R> runWithScope(block: (JSContext, ScriptableObject) -> R): R? {
        return awaitPostedRuntimeTask(postThread::post) {
            val ctx = JSContextLocal.get()
            val scope = JSTopScopeLocal.get()
            if (ctx == null || scope == null) null else block(ctx, scope)
        }
    }

}

/**
 * Cancels the caller's wait and skips work that has not started on the runtime thread.
 * PostThread has no queue-removal API, so cancelled Runnables remain queued until skipped.
 * Synchronous JS or network work already running cannot be forcibly stopped safely: it may
 * continue occupying the HandlerThread, but its late result/exception never resumes the caller.
 */
@OptIn(InternalCoroutinesApi::class)
internal suspend fun <R> awaitPostedRuntimeTask(
    post: (Runnable) -> Unit,
    block: () -> R,
): R = suspendCancellableCoroutine { continuation ->
    val task = Runnable {
        if (!continuation.isActive) return@Runnable
        val result = runCatching(block)
        val token = result.fold(
            onSuccess = { continuation.tryResume(it) },
            onFailure = { continuation.tryResumeWithException(it) },
        ) ?: return@Runnable
        continuation.completeResume(token)
    }
    try {
        post(task)
    } catch (e: Throwable) {
        val token = continuation.tryResumeWithException(e)
        if (token != null) continuation.completeResume(token)
    }
}
