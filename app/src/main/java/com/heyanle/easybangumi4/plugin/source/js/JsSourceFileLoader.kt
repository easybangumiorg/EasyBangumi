package com.heyanle.easybangumi4.plugin.source.js

import com.heyanle.easybangumi4.plugin.source.js.runtime.JSRuntimeProvider
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSScope
import com.heyanle.easybangumi4.plugin.source.js.source.JsSource
import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceFileInfo
import java.io.File

class JsSourceFileLoader(
    private val file: File,
    private val jsRuntimeProvider: JSRuntimeProvider,
) {

    data class Metadata(
        val key: String,
        val label: String,
        val versionName: String,
        val versionCode: Long,
        val libVersion: Int,
        val file: File,
    )

    fun canLoad(): Boolean {
        return file.isFile &&
            file.exists() &&
            file.canRead() &&
            file.name.endsWith(PluginV3.JS_SOURCE_SUFFIX)
    }

    fun inspect(): Metadata? {
        if (!canLoad()) {
            return null
        }
        val metadata = readMetadata()
        return Metadata(
            key = metadata[SourceMetadata.SOURCE_TAG_KEY].orEmpty(),
            label = metadata[SourceMetadata.SOURCE_TAG_LABEL].orEmpty(),
            versionName = metadata[SourceMetadata.SOURCE_TAG_VERSION_NAME].orEmpty(),
            versionCode = metadata[SourceMetadata.SOURCE_TAG_VERSION_CODE]?.toLongOrNull() ?: -1,
            libVersion = metadata[SourceMetadata.SOURCE_TAG_LIB_VERSION]?.toIntOrNull() ?: -1,
            file = file,
        )
    }

    fun load(): SourceFileInfo? {
        val metadata = inspect() ?: return null
        val key = metadata.key
        val label = metadata.label
        val versionName = metadata.versionName
        val versionCode = metadata.versionCode
        val libVersion = metadata.libVersion

        fun error(message: String, exception: Throwable? = null): SourceFileInfo.Error {
            return SourceFileInfo.Error(
                key = key,
                label = label,
                versionName = versionName,
                versionCode = versionCode,
                libVersion = libVersion,
                file = metadata.file,
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
                file = metadata.file,
                source = JsSource(readMetadata(), metadata.file, JSScope(jsRuntimeProvider.getRuntime())),
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
