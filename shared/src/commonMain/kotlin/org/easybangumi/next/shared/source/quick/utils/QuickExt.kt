package org.easybangumi.next.shared.source.quick.utils

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.JsObject
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.serialization.deserialize
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.serialization.serialize
import org.easybangumi.next.lib.utils.DataState

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

internal val logger = logger("QuickUtils")

suspend fun QuickJs.checkFunctionExists(vararg functionName: String): Boolean {
    val code = buildString {
        append("(function() {")
        append("let result = true;")
        functionName.forEach {
            append("if (typeof $it !== 'function') { result = false; return result; }")
        }
        append("return result;")
        append("})();")
    }
    return this.evaluate<Boolean>(code)
}



suspend fun QuickJs.callFunctionWithDataState(
    functionName: String,
    fileName: String = functionName,
    vararg args: Any?
): JsObject {
    val code = buildString {
        append("await (async function() {")
        append("try {")
        append("let res = await ")
        append(functionName)
        append("(")
        args.forEachIndexed { index, arg ->
            if (arg is String || arg is Number || arg is Boolean) {
                append(arg)
            } else {
                // Serialize each argument to JSON and parse it in JavaScript
                append("JSON.parse(\"")
                append(jsonSerializer.serialize(args))
                append("\")")
            }
            if (index != args.size -1) {
                append(",")
            }
        }
        append(");")
        append("return {error: false, data: JSON.stringify(res)};")
        append("} catch (e) {")
        append("Log.e('Error in function $functionName:', e);")
        append("return {error: true, message: e.message};")
        append("}")

        append("})();")
    }
    logger.info(code)
    return this.evaluate<JsObject>(code, fileName)
}

suspend inline fun <reified T: Any> JsObject.toDataState(): DataState<T>? {
    val isError: Boolean = this["error"] as? Boolean ?: return null
    return if (isError) {
        val message: String = this["message"] as? String ?: "Unknown error"
        DataState.Error(message, null)
    } else {
        val dataString: String = this["data"] as? String ?: return null
        if (T::class == String::class){
            return DataState.Ok(dataString as T)
        } else if (T::class == Boolean::class) {
            return DataState.Ok(dataString.toBoolean() as T)
        } else if (T::class == Int::class) {
            return DataState.Ok(dataString.toInt() as T)
        } else if (T::class == Double::class) {
            return DataState.Ok(dataString.toDouble() as T)
        } else if (T::class == Float::class) {
            return DataState.Ok(dataString.toFloat() as T)
        } else if (T::class == Long::class) {
            return DataState.Ok(dataString.toLong() as T)
        } else if (T::class == Short::class) {
            return DataState.Ok(dataString.toShort() as T)
        } else if (T::class == Byte::class) {
            return DataState.Ok(dataString.toByte() as T)
        } else if (T::class == Char::class) {
            return DataState.Ok(dataString.first() as T)
        }
        val data = jsonSerializer.deserialize<T>(dataString, null)
        if (data != null) {
            DataState.Ok(data)
        } else {
            DataState.Error("Failed to deserialize data", null)
        }
    }
}