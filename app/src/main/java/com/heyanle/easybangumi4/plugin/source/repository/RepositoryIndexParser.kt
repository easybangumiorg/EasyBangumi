package com.heyanle.easybangumi4.plugin.source.repository

/** Reject a broken catalogue instead of silently presenting it as an empty one. */
internal fun parseRepositoryIndex(
    body: String,
    repoUrl: String,
    decode: (String) -> RepositoryEntry?,
): List<RepositoryEntry> {
    val keys = hashSetOf<String>()
    return body.removePrefix("\uFEFF").lineSequence().mapIndexedNotNull { index, line ->
        if (line.isBlank()) return@mapIndexedNotNull null
        val entry = try {
            decode(line)
        } catch (e: Exception) {
            throw IllegalArgumentException("索引第 ${index + 1} 行不是有效的源信息", e)
        } ?: throw IllegalArgumentException("索引第 ${index + 1} 行不是有效的源信息")
        require(entry.key.isNotBlank() && entry.label.isNotBlank() && entry.versionCode >= 0) {
            "索引第 ${index + 1} 行的源信息不完整"
        }
        require(isRepositoryUrlValid(entry.url)) { "索引第 ${index + 1} 行的下载地址无效" }
        require(entry.sha256 == null || entry.sha256.matches(Regex("[0-9a-fA-F]{64}"))) {
            "索引第 ${index + 1} 行的 SHA-256 无效"
        }
        require(keys.add(entry.key)) { "索引包含重复的源标识：${entry.key}" }
        entry.also { it.repoUrl = repoUrl }
    }.toList()
}
