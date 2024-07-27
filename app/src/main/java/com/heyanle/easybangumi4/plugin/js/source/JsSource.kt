package com.heyanle.easybangumi4.plugin.js.source

import com.heyanle.easybangumi4.plugin.js.runtime.JSRuntime
import com.heyanle.easybangumi4.source_api.Source
import java.io.File
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2024/7/27.
 * https://github.com/heyanLE
 */
class JsSource(
    private val map: Map<String, String>,
    private val jsString: String,
    private val jsRuntime: JSRuntime
): Source {

    companion object {
        const val JS_IMPORT = """
            importPackage(Packages.com.heyanle.easybangumi4.plugin.extension)
            importPackage(Packages.com.heyanle.easybangumi4.plugin.js.runtime)
            importPackage(Packages.org.jsoup)
            importPackage(Packages.okhttp3)
            importPackage(Packages.com.heyanle.easybangumi4.source_api.utils.api)
        """
    }

    override val describe: String?
        get() = map.get("describe")
    override val key: String
        get() = map.get("key") ?: ""
    override val label: String
        get() = map.get("label") ?: ""
    override val version: String
        get() = map.get("version") ?: ""
    override val versionCode: Int
        get() = map.get("versionCode")?.toIntOrNull() ?: 0

    init {
        jsRuntime.postWithScope { context, scriptableObject ->
            context.evaluateString(scriptableObject, JS_IMPORT, "<js>", 1, null)
            context.evaluateString(scriptableObject, jsString, "<js>", 1, null)

        }
    }

    override fun register(): List<KClass<*>> {
        TODO("Not yet implemented")
    }
}