package com.heyanle.easybangumi4.plugin.source.repository

import com.heyanle.easybangumi4.plugin.source.js.JsSourceFileLoader
import com.heyanle.easybangumi4.plugin.source.js.runtime.JSRuntimeProvider
import java.io.File
import java.security.MessageDigest

/** Metadata is inspected before installation; no downloaded JS is executed here. */
internal fun validateRepositoryDownload(entry: RepositoryEntry, file: File) {
    entry.sha256?.let { expected ->
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        require(digest.equals(expected, ignoreCase = true)) { "下载源的 SHA-256 与仓库索引不一致，请刷新仓库后重试" }
    }
    val metadata = JsSourceFileLoader(file, JSRuntimeProvider()).inspect()
        ?: throw IllegalArgumentException("下载文件不是有效的 JS 源")
    require(metadata.key == entry.key) { "下载源标识与仓库索引不一致" }
    // Older custom catalogues may omit versionCode (the default is 0).
    if (entry.versionCode > 0) {
        require(metadata.versionCode == entry.versionCode) { "下载源版本与仓库索引不一致，请刷新仓库后重试" }
    }
}
