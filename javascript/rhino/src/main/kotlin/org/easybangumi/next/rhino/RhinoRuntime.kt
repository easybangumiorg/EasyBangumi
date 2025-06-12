package org.easybangumi.next.rhino

import kotlinx.coroutines.*
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class RhinoRuntime(
    // Android 只能运行翻译模式，Desktop 可以尝试优化
    private val optimizationLevel: Int = -1
) {

    companion object {
        val RhinoContextLocal = ThreadLocal<RhinoContext>()
        val JSTopScopeLocal = ThreadLocal<ScriptableObject> ()
    }

    private val singleDispatcher = RhinoService.service.getSingletonDispatcher()
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + singleDispatcher + CoroutineName("$this"))
    }
    @Volatile
    private var isRelease = false


    fun init(){
        scope.launch {
            yield()
            val context = RhinoContext.enter()
            context.optimizationLevel = optimizationLevel
            RhinoContextLocal.set(context)
            val scope: ScriptableObject = ImporterTopLevel(context)
            context.initStandardObjects(scope)
            context.newObject(scope)
            JSTopScopeLocal.set(scope)
        }
    }

    fun release (){
        // 先新增标记后开始释放，避免后续业务使用了
        isRelease = true
        scope.launch {
            RhinoContext.exit()
            this@RhinoRuntime.scope.cancel()
        }
    }


    fun postWithScope(block: (RhinoContext, ScriptableObject) -> Unit) {
        scope.launch {
            val ctx = RhinoContextLocal.get()
            val scope = JSTopScopeLocal.get()
            if (ctx == null || scope == null) {
                return@launch
            }
            block(ctx, scope)
        }
    }


    @Throws(TimeoutCancellationException::class, RhinoScopeException::class, Exception::class)
    suspend fun <R>  runWithScope(block: suspend (RhinoContext, ScriptableObject) -> R) : R? {
        return try {
            scope.async {
                val ctx = RhinoContextLocal.get()
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
        } catch (cancel: CancellationException) {
            throw RhinoScopeException("RhinoRuntime is release", cancel)
        } catch (e: Throwable) {
            throw RhinoScopeException(e)
        }
    }

    // 引用计数法
    @Volatile
    private var refCount = 0

    suspend fun lock(): Boolean {
        return scope.async {
            refCount ++
            isActive && !isRelease
        }.await()
    }

    fun unlock(){
        scope.launch {
            refCount --
            if (refCount == 0) {
                release()
            }
        }
    }

}