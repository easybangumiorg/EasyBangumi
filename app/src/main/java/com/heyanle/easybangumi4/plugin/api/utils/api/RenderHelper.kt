package com.heyanle.easybangumi4.plugin.api.utils.api

interface RenderHelper {

    data class RenderedStrategy(
        val url: String,
        val callBackRegex: String = "",
        val encoding: String = "utf-8",
        val userAgentString: String? = null,
        val header: Map<String, String>? = null,
        val actionJs: String? = null,
        val isBlockBlob: Boolean = false,
        val timeOut: Long = 8000L,
        val isBlockResource: Boolean = false,
    )

    data class RenderedResult(
        val strategy: RenderedStrategy,
        val url: String,
        val isTimeout: Boolean,
        val content: String,
        val interceptResource: String,
    )

    data class VideoStrategy(
        val url: String,
        val userAgentString: String? = null,
        val header: Map<String, String>? = null,
        val actionJs: String? = null,
        val timeOut: Long = 15000L,
        val useLegacyParser: Boolean = false,
    )

    data class VideoResult(
        val strategy: VideoStrategy,
        val url: String,
        val isTimeout: Boolean,
        val isM3u8: Boolean = false,
    )

    suspend fun renderedHtml(strategy: RenderedStrategy): RenderedResult

    suspend fun renderVideo(strategy: VideoStrategy): VideoResult

    fun renderHtmlFromJs(strategy: RenderedStrategy): RenderedResult

    fun renderVideoFromJs(strategy: VideoStrategy): VideoResult
}
