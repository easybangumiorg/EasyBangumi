package org.easybangumi.next.libplayer.api

/**
 * Created by heyanlin on 2025/5/27.
 */
data class MediaItem(
    val id: String = C.DEFAULT_ID,
    val uri: String,
//    val mineType: String = C.MINE_TYPE_UNKNOWN,
    // 透传给播放引擎
    val optional: Map<String, Any> = emptyMap(),
    // TODO 字幕加载
    val subtitleConfigs: List<SubtitleConfig> = emptyList(),
) {


    data class SubtitleConfig(
        val id: String,
        val uri: String,
        val language: String? = null,
        val isDefault: Boolean = false,
        val tag: Any? = null,
    )

}