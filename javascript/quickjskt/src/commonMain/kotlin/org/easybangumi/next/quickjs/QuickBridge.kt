package org.easybangumi.next.quickjs

import com.dokar.quickjs.binding.JsObject

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
interface QuickBridge {

    class Factory(
        val create: suspend () -> List<QuickBridge>
    )

    val name: String
    val action: List<String>

    suspend fun invoke(scope: QuickRuntime, action: String, param: JsObject?): JsObject?

    open fun makeJsCode(): String {
        return """
        const let ${name} = {
            ${action.joinToString(",\n") { act ->
                """
                $act: function(param) {
                    return new Promise((resolve) => {
                        sendMessage("$name", "$act", param).then((res) => {
                            resolve(res);
                        });
                    });
                }
                """.trimIndent()
            } }
        };
    """.trimIndent()
    }
}