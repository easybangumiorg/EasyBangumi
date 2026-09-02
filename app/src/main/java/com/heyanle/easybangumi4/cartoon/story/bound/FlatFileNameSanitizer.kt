package com.heyanle.easybangumi4.cartoon.story.bound

/**
 * 扁平下载目录文件名清洗：默认“番名-集名”，去除文件系统特殊字符。
 */
object FlatFileNameSanitizer {

    private val illegalChars = Regex("[\\\\/:*?\"<>|\\x00-\\x1f]")

    fun sanitize(name: String): String {
        val cleaned = name
            .replace(illegalChars, " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val taken = if (cleaned.length > 100) cleaned.take(100) else cleaned
        val trimmed = taken.trimEnd('.', ' ').trim()
        return if (trimmed.isEmpty()) "video" else trimmed
    }

    fun defaultName(cartoonName: String, episodeLabel: String): String {
        return sanitize("$cartoonName-$episodeLabel")
    }

}
