package com.heyanle.easybangumi4.danmaku

/**
 * Contract for sources packaged in the application. There is deliberately no external
 * registration or script-loading API for danmaku sources.
 */
interface DanmakuSource {
    val metadata: DanmakuSourceMetadata

    fun isAvailable(): Boolean

    suspend fun searchBangumi(query: String): DanmakuResult<List<DanmakuBangumi>>

    suspend fun loadEpisodes(bangumi: DanmakuBangumi): DanmakuResult<List<DanmakuEpisode>>

    suspend fun loadComments(remoteEpisodeId: Long): DanmakuResult<List<DanmakuComment>>
}
