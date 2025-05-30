package org.easybangumi.next.rhino

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.easybangumi.next.lib.utils.coroutineProvider
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JSRuntime(
    // Android 只能运行翻译模式，Desktop 可以尝试优化
    private val optimizationLevel: Int = -1
) {

    companion object {
        val JSContextLocal = ThreadLocal<JSContext>()
        val JSTopScopeLocal = ThreadLocal<ScriptableObject> ()
    }

    private val singleDispatcher =  coroutineProvider.newSingle()
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + singleDispatcher + CoroutineName("$this"))
    }


    fun init(){
        scope.launch {
            val context = JSContext.enter()
            context.optimizationLevel = optimizationLevel
            JSContextLocal.set(context)
            val scope: ScriptableObject = ImporterTopLevel(context)
            context.initStandardObjects(scope)
            context.newObject(scope)
            JSTopScopeLocal.set(scope)
        }
    }

    fun release (){
        scope.cancel()
    }

    fun postWithScope(block: (JSContext, ScriptableObject) -> Unit) {
        scope.launch {
            val ctx = JSContextLocal.get()
            val scope = JSTopScopeLocal.get()
            if (ctx == null || scope == null) {
                return@launch
            }
            block(ctx, scope)
        }
    }


    suspend fun <R>  runWithScope(block: (JSContext, ScriptableObject) -> R) : R? {
        return scope.async {
            val ctx = JSContextLocal.get()
            val scope = JSTopScopeLocal.get()
            if (ctx == null || scope == null) {
                return@async null
            }
            try {
                return@async block(ctx, scope)
            }catch (e: Throwable){
                e.printStackTrace()
                throw e
            }
        }.await()
    }

}