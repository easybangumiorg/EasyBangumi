package com.heyanle.easybangumi4.plugin.source.js

import com.heyanle.easybangumi4.plugin.source.jsengine.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.plugin.source.jsengine.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.jsengine.source.JsSource
import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceFileInfo
import java.io.File

class JsSourceFileLoader(
    private val file: File,
    private val jsRuntimeProvider: JSRuntimeProvider,
) {

    fun canLoad(): Boolean {
        return file.isFile &&
            file.exists() &&
            file.canRead() &&
            file.name.endsWith(PluginV3.JS_SOURCE_SUFFIX)
    }

    fun load(): SourceFileInfo? {
        if (!canLoad()) {
            return null
        }
        val metadata = readMetadata()
        val key = metadata[SourceMetadata.SOURCE_TAG_KEY].orEmpty()
        val label = metadata[SourceMetadata.SOURCE_TAG_LABEL].orEmpty()
        val versionName = metadata[SourceMetadata.SOURCE_TAG_VERSION_NAME].orEmpty()
        val versionCode = metadata[SourceMetadata.SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1
        val libVersion = metadata[SourceMetadata.SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1

        fun error(message: String, exception: Throwable? = null): SourceFileInfo.Error {
            return SourceFileInfo.Error(
                key = key,
                label = label,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                file = file,
                message = message,
                exception = exception,
            )
        }

        if (key.isBlank() || label.isBlank() || versionName.isBlank() || versionCode < 0 || libVersion < 0) {
            return error("source metadata is incomplete")
        }
        if (libVersion !in PluginV3.SUPPORTED_LIB_VERSION_RANGE) {
            return error(
                "unsupported source libVersion: $libVersion, supported ${PluginV3.SUPPORTED_LIB_VERSION_RANGE}"
            )
        }

        return try {
            SourceFileInfo.Loaded(
                key = key,
                label = label,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                file = file,
                source = JsSource(metadata, file, JSScope(jsRuntimeProvider.getRuntime())),
            )
        } catch (e: Exception) {
            error("load source failed: ${e.message}", e)
        }
    }

    private fun readMetadata(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        file.bufferedReader().use { reader ->
            var line = reader.readLine()
            while (line != null) {
                if (line.isEmpty() || !line.startsWith("//")) {
                    break
                }
                val body = line.removePrefix("//").trimStart()
                if (body.startsWith("@")) {
                    val split = body.indexOf(' ')
                    if (split > 1) {
                        map[body.substring(1, split)] = body.substring(split + 1)
                    }
                }
                line = reader.readLine()
            }
        }
        return map
    }
}
