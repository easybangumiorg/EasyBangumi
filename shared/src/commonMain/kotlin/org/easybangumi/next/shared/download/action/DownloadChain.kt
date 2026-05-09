package org.easybangumi.next.shared.download.action

import org.easybangumi.next.shared.data.cartoon.PlayInfo

/**
 * 下载链配置
 * 根据 PlayInfo.type 选择不同的下载链
 */
object DownloadChain {

    const val ACTION_PARSE = "parse"
    const val ACTION_KTOR_HTTP = "ktor_http_download"
    const val ACTION_ARIA_M3U8 = "aria_m3u8"
    const val ACTION_FFMPEG_M3U8 = "ffmpeg_m3u8"
    const val ACTION_TRANSCODE = "transcode"
    const val ACTION_COPY_AND_NFO = "copy_and_nfo"

    /**
     * 平台类型
     */
    enum class Platform {
        ANDROID,
        DESKTOP,
        IOS,
    }

    /**
     * 根据 PlayInfo.type 和平台选择下载链
     */
    fun getChain(playInfoType: String): List<String> {
        return when (playInfoType) {
            PlayInfo.TYPE_NORMAL -> listOf(
                ACTION_PARSE,
                ACTION_KTOR_HTTP,
                ACTION_COPY_AND_NFO,
            )

            PlayInfo.TYPE_HLS -> when (getPlatform()) {
                Platform.ANDROID -> listOf(
                    ACTION_PARSE,
                    ACTION_ARIA_M3U8,
                    ACTION_TRANSCODE,
                    ACTION_COPY_AND_NFO,
                )

                Platform.DESKTOP -> listOf(
                    ACTION_PARSE,
                    ACTION_FFMPEG_M3U8,
                    ACTION_COPY_AND_NFO,
                )

                Platform.IOS -> listOf(
                    ACTION_PARSE,
                    ACTION_KTOR_HTTP,
                    ACTION_COPY_AND_NFO,
                )
            }

            else -> throw UnsupportedOperationException("Unsupported play info type: $playInfoType")
        }
    }
}

/**
 * 获取当前平台（跨平台实现）
 */
expect fun getPlatform(): DownloadChain.Platform
