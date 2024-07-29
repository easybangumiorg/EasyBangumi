package com.heyanle.easybangumi4.plugin.js.runtime

import com.heyanle.easybangumi4.utils.PostThread
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.mozilla.javascript.Context as JSContext


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
            // Android 只能运行翻译模式
            context.optimizationLevel = -1
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


    suspend fun <R>  runWithScope(block: (JSContext, ScriptableObject) -> R) : R? {
        return suspendCoroutine<R?> { continuation ->
            postThread.post {
                val ctx = JSContextLocal.get()
                val scope = JSTopScopeLocal.get()
                if (ctx == null || scope == null) {
                    continuation.resume(null)
                    return@post
                }
                try {
                    continuation.resume(block(ctx, scope))
                }catch (e: Throwable){
                    e.printStackTrace()
                    continuation.resumeWithException(e)
                }


            }
        }
    }

}