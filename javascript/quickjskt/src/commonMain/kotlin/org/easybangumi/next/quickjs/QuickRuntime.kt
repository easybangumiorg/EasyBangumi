package org.easybangumi.next.quickjs

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.QuickJsException
import com.dokar.quickjs.binding.AsyncFunctionBinding
import com.dokar.quickjs.binding.JsObject
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  QuickJs 运行时
 */
class QuickRuntime(
    private val dispatcher: CoroutineDispatcher,
    private val bridgeList: List<QuickBridge>,
) {

    companion object {
        const val GLOBAL_SCOPE_MAP = "Global_ScopeMap"
        const val NATIVE_SEND_MESSAGE = "Native_SendMessage"
    }

    // 暂时不允许重名
    private val bridgeMap: Map<String, QuickBridge> by lazy {
        bridgeList.associateBy { it.name }
    }

    val quickJs: QuickJs by lazy {
        QuickJs.create(dispatcher)
    }

    private val initAtomic = atomic(false)

    suspend fun init() {
        if (initAtomic.compareAndSet(expect = false, update = true)) {
            // 初始化 bridge
            quickJs.defineBinding(NATIVE_SEND_MESSAGE, object: AsyncFunctionBinding<JsObject?> {
                override suspend fun invoke(args: Array<Any?>): JsObject? {
                    val module = args.getOrNull(0) as? String ?: return null
                    val action = args.getOrNull(1) as? String ?: return null
                    val param = args.getOrNull(2) as? JsObject
                    val bridge = bridgeMap[module] ?: return null
                    return bridge.invoke(this@QuickRuntime, action, param)
                }
            })
            bridgeList.forEach { bridge ->
                quickJs.evaluate(bridge.makeJsCode(), bridge.name, false)
            }
            // scope map
            quickJs.evaluate<Unit>("const ${GLOBAL_SCOPE_MAP} = new Map();", "init.js", false)
        }
    }

    @Throws(QuickJsException::class, CancellationException::class)
    suspend inline fun <reified T> evaluate(
        code: String,
        filename: String = "main.js",
        asModule: Boolean = false,
    ): T {
        return quickJs.evaluate(code, filename, asModule) as T
    }







}