package com.heyanle.easybangumi4.plugin.source

import com.heyanle.easybangumi4.plugin.api.Source
import java.io.File

sealed class SourceFileInfo {
    abstract val key: String
    abstract val label: String
    abstract val versionName: String
    abstract val versionCode: Long
    abstract val libVersion: Int
    abstract val file: File

    data class Loaded(
        override val key: String,
        override val label: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val file: File,
        val source: Source,
    ) : SourceFileInfo()

    data class Error(
        override val key: String,
        override val label: String,
        override val versionName: String,
        override val versionCode: Long,
        override val libVersion: Int,
        override val file: File,
        val message: String,
        val exception: Throwable? = null,
    ) : SourceFileInfo()
}
