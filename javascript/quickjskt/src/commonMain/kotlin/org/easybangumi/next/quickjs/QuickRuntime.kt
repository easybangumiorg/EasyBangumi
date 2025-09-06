package org.easybangumi.next.quickjs

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.AsyncFunctionBinding
import com.dokar.quickjs.binding.JsObject
import kotlinx.coroutines.CoroutineDispatcher
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
 */
class QuickRuntime(
    private val dispatcher: CoroutineDispatcher,
    private val singleDispatcher: CoroutineDispatcher,
    private val bridgeList: List<QuickBridge>,
) {

    // 暂时不允许重名
    private val bridgeMap: Map<String, QuickBridge> by lazy {
        bridgeList.associateBy { it.name }
    }

    private val quickJs: QuickJs by lazy {
        QuickJs.create(dispatcher).apply {
            defineBinding("sendMessage", object: AsyncFunctionBinding<JsObject?> {
                override suspend fun invoke(args: Array<Any?>): JsObject? {
                    val module = args.getOrNull(0) as? String ?: return null
                    val action = args.getOrNull(1) as? String ?: return null
                    val param = args.getOrNull(2) as? JsObject
                    val bridge = bridgeMap[module] ?: return null
                    return bridge.invoke(this@QuickRuntime, action, param)
                }
            })
            bridgeList.forEach { bridge ->
                val jsCode = bridge.makeJsCode()
                evaluate(bridge.name, jsCode, false)
            }
        }
    }







}