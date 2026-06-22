package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.source.PluginV3
import com.heyanle.easybangumi4.plugin.source.SourceFileInfo
import com.heyanle.easybangumi4.utils.MoshiArrayListJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

class JsonSourceFileLoader(
    private val file: File,
) {
    companion object {
        private val moshi: Moshi by lazy {
            Moshi.Builder()
                .add(MoshiArrayListJsonAdapter.FACTORY)
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }
    }

    fun canLoad(): Boolean {
        return file.isFile &&
            file.exists() &&
            file.canRead() &&
            file.name.endsWith(PluginV3.JSON_SOURCE_SUFFIX)
    }

    fun load(): SourceFileInfo? {
        if (!canLoad()) return null

        val rule = runCatching {
            moshi.adapter(JsonSourceRule::class.java).fromJson(file.readText())
        }.getOrNull()

        fun error(
            message: String,
            parsed: JsonSourceRule? = rule,
            exception: Throwable? = null,
        ): SourceFileInfo.Error {
            return SourceFileInfo.Error(
                key = parsed?.key.orEmpty(),
                label = parsed?.label.orEmpty(),
                versionName = parsed?.versionName.orEmpty(),
                versionCode = parsed?.versionCode?.toLong() ?: -1,
                libVersion = parsed?.libVersion ?: -1,
                file = file,
                message = message,
                exception = exception,
            )
        }

        if (rule == null) {
            return error("json source parse failed", exception = null)
        }
        if (rule.key.isBlank() || rule.label.isBlank() || rule.versionName.isBlank() || rule.versionCode < 0 || rule.libVersion < 0) {
            return error("json source metadata is incomplete", rule)
        }
        if (!rule.isSupported()) {
            return error(
                "unsupported json source libVersion: ${rule.libVersion}, supported ${PluginV3.SUPPORTED_LIB_VERSION_RANGE}",
                rule,
            )
        }

        return SourceFileInfo.Loaded(
            key = rule.key,
            label = rule.label,
            versionName = rule.versionName,
            versionCode = rule.versionCode.toLong(),
            libVersion = rule.libVersion,
            file = file,
            source = JsonSource(rule, file),
        )
    }
}
