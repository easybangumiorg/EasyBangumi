package org.easybangumi.next.shared.download.action.m3u8

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText

/**
 * M3U8 播放列表数据
 */
data class M3u8Playlist(
    val version: Int?,
    val targetDuration: Int?,
    val segments: List<M3u8Segment>,
    val isEndList: Boolean,
)

/**
 * M3U8 分片
 */
data class M3u8Segment(
    val url: String,
    val duration: Double,
    val title: String?,
    val encryption: M3u8Encryption?,
)

/**
 * M3U8 加密信息
 */
data class M3u8Encryption(
    val method: String,       // "AES-128", "NONE"
    val uri: String?,         // 密钥 URL
    val iv: ByteArray?,       // 初始化向量
)

/**
 * M3U8 播放列表解析器
 */
object M3u8Parser {

    /**
     * 解析 M3U8 播放列表
     */
    suspend fun parse(
        client: HttpClient,
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): M3u8Playlist {
        val content = client.prepareGet(url) {
            headers.forEach { (k, v) -> header(k, v) }
        }.execute { response ->
            response.bodyAsText()
        }

        return parse(content, url)
    }

    /**
     * 解析 M3U8 内容
     */
    fun parse(content: String, baseUrl: String): M3u8Playlist {
        val lines = content.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var version: Int? = null
        var targetDuration: Int? = null
        var isEndList = false
        val segments = mutableListOf<M3u8Segment>()

        var currentEncryption: M3u8Encryption? = null
        var currentDuration = 0.0
        var currentTitle: String? = null

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                line.startsWith("#EXT-X-VERSION:") -> {
                    version = line.substringAfter(":").trim().toIntOrNull()
                }

                line.startsWith("#EXT-X-TARGETDURATION:") -> {
                    targetDuration = line.substringAfter(":").trim().toIntOrNull()
                }

                line.startsWith("#EXT-X-ENDLIST") -> {
                    isEndList = true
                }

                line.startsWith("#EXT-X-KEY:") -> {
                    currentEncryption = parseEncryption(line, baseUrl)
                }

                line.startsWith("#EXTINF:") -> {
                    val parts = line.substringAfter(":").split(",", limit = 2)
                    currentDuration = parts[0].trim().toDoubleOrNull() ?: 0.0
                    currentTitle = parts.getOrNull(1)?.trim()?.removeSurrounding("\"")
                }

                line.startsWith("#") -> {
                    // 忽略其他标签
                }

                else -> {
                    // 这是一个分片 URL
                    val segmentUrl = resolveUrl(baseUrl, line)
                    segments.add(
                        M3u8Segment(
                            url = segmentUrl,
                            duration = currentDuration,
                            title = currentTitle,
                            encryption = currentEncryption,
                        )
                    )
                    currentDuration = 0.0
                    currentTitle = null
                }
            }

            i++
        }

        return M3u8Playlist(
            version = version,
            targetDuration = targetDuration,
            segments = segments,
            isEndList = isEndList,
        )
    }

    /**
     * 解析加密信息
     */
    private fun parseEncryption(line: String, baseUrl: String): M3u8Encryption {
        val attrs = parseAttributes(line)
        val method = attrs["METHOD"] ?: "NONE"
        val uri = attrs["URI"]?.removeSurrounding("\"")?.let { resolveUrl(baseUrl, it) }
        val iv = attrs["IV"]?.let { parseIV(it) }

        return M3u8Encryption(
            method = method,
            uri = uri,
            iv = iv,
        )
    }

    /**
     * 解析属性
     */
    private fun parseAttributes(line: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val content = line.substringAfter(":")
        val parts = content.split(",")

        for (part in parts) {
            val eqIndex = part.indexOf("=")
            if (eqIndex > 0) {
                val key = part.substring(0, eqIndex).trim()
                val value = part.substring(eqIndex + 1).trim()
                result[key] = value
            }
        }

        return result
    }

    /**
     * 解析 IV
     */
    private fun parseIV(iv: String): ByteArray? {
        val hex = iv.removePrefix("0x").removePrefix("0X")
        return try {
            hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析相对 URL（纯 Kotlin 实现）
     */
    private fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl
        }

        if (relativeUrl.startsWith("/")) {
            // 绝对路径
            val schemeEnd = baseUrl.indexOf("://")
            if (schemeEnd == -1) return relativeUrl
            val hostStart = schemeEnd + 3
            val hostEnd = baseUrl.indexOf("/", hostStart)
            val host = if (hostEnd == -1) baseUrl else baseUrl.substring(0, hostEnd)
            return "$host$relativeUrl"
        }

        // 相对路径
        val lastSlash = baseUrl.lastIndexOf("/")
        if (lastSlash == -1) return "$baseUrl/$relativeUrl"
        return "${baseUrl.substring(0, lastSlash + 1)}$relativeUrl"
    }
}
