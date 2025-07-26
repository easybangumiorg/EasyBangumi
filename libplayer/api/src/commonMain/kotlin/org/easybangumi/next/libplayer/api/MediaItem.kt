package org.easybangumi.next.libplayer.api

/**
 * Created by heyanle on 2025/5/27.
 */
data class MediaItem(
    val id: String = C.DEFAULT_ID,
    val mediaType: Int = MEDIA_TYPE_UNKNOWN,
    val uri: String,
//    val mineType: String = C.MINE_TYPE_UNKNOWN,
    // 透传给播放引擎
    val optional: Map<String, Any> = emptyMap(),
    val header: Map<String, String>? = null,
    // TODO 字幕加载
    val subtitleConfigs: List<SubtitleConfig> = emptyList(),
) {

    companion object {
        const val MEDIA_TYPE_UNKNOWN = -1
        const val MEDIA_TYPE_HLS = 0
        const val MEDIA_TYPE_NORMAL = 1
    }


    data class SubtitleConfig(
        val id: String,
        val uri: String,
        val language: String? = null,
        val isDefault: Boolean = false,
        val tag: Any? = null,
    )


}