package org.easybangumi.next.quickjs

import com.dokar.quickjs.binding.JsObject
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher

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
 *  一个闭包，对外提供 QuickApi
 */
class QuickJsScope(
    // 需要唯一
    private val key: String,
    private val runtime: QuickRuntime,
    private val apiFactory: QuickApi.Factory,
    private val sourceJsCodeFactory: suspend ()-> String,
) {

    private val initAtomic = atomic(false)

    suspend fun init() {
        if (initAtomic.compareAndSet(false, update = true)) {
            val codeBuilder = StringBuilder()
            codeBuilder.apply {
                append("Global_ScopeMap.set(\"${key}\", function(){")
                append(sourceJsCodeFactory.invoke())
                append("\n")
                append("return {")
                val apiList = apiFactory.create()
                apiList.forEachIndexed { index, api ->
                    append("${api.name}: {")
                    api.action.forEachIndexed { actionIndex, action ->
                        append("""
                            $action: function(param) {
                                return new Promise((resolve) => {
                                    // 暴露闭包里的 name_action 函数
                                    ${api.name}_${api.action}(JSON.parse(param)).then((res) => {
                                        resolve(res);
                                    });
                                });
                            }
                        """.trimIndent())
                        if (actionIndex != api.action.size -1) {
                            append(",")
                        }
                    }
                    append("}")
                    if (index != apiList.size -1) {
                        append(",")
                    }
                }
                append("}")
                append("}());")
            }
            runtime.evaluate<Unit>(codeBuilder.toString(), "$key.js", false)
        }
    }


    suspend fun callApi(
        apiName: String,
        action: String,
        param: String? = null,
    ): JsObject? {
        return runtime.evaluate(
            """
            Global_ScopeMap.get("$key").${apiName}.$action(${param ?: "null"});
            """.trimIndent(),
            "$key-call-$apiName-$action.js",
            false
        )
    }

    suspend fun destroy() {
        runtime.quickJs.evaluate<Unit>(
            """
            Global_ScopeMap.delete("$key");
            """.trimIndent(),
            "$key-destroy.js",
            false
        )
    }




}