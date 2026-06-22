package com.heyanle.easybangumi4.plugin.source.bundle

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.getMD5
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PlayComponentCacheWrapper(
    private val delegate: PlayComponent,
) : ComponentWrapper(), PlayComponent {

    private val cacheFolder = File(APP.getFilePath("play_info_cache")).apply {
        mkdirs()
    }

    init {
        innerSource = delegate.source
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        val cacheKey = cacheKey(summary, playLine, episode)
        if (canCache) {
            read(cacheKey)?.let {
                return SourceResult.Complete(it, isCache = true)
            }
        }
        val result = delegate.getPlayInfo(summary, playLine, episode, canCache = false)
        if (canCache && result is SourceResult.Complete && !result.isCache) {
            write(cacheKey, result.data)
        }
        return result
    }

    private fun cacheKey(summary: CartoonSummary, playLine: PlayLine, episode: Episode): String {
        return "${summary.source}|${summary.id}|${playLine.id}|${episode.id}".getMD5()
    }

    private suspend fun read(key: String): PlayerInfo? = withContext(Dispatchers.IO) {
        val file = File(cacheFolder, "$key.json")
        if (!file.isFile || !file.canRead()) return@withContext null
        runCatching {
            file.readText().jsonTo<CachedPlayerInfo>()?.toPlayerInfo()
        }.getOrNull()
    }

    private suspend fun write(key: String, playerInfo: PlayerInfo) = withContext(Dispatchers.IO) {
        val file = File(cacheFolder, "$key.json")
        runCatching {
            file.writeText(CachedPlayerInfo.from(playerInfo).toJson())
        }
    }

    private data class CachedPlayerInfo(
        val decodeType: Int = PlayerInfo.DECODE_TYPE_OTHER,
        val uri: String = "",
        val header: Map<String, String>? = null,
    ) {
        fun toPlayerInfo(): PlayerInfo {
            return PlayerInfo(decodeType = decodeType, uri = uri).apply {
                header = this@CachedPlayerInfo.header
            }
        }

        companion object {
            fun from(playerInfo: PlayerInfo): CachedPlayerInfo {
                return CachedPlayerInfo(
                    decodeType = playerInfo.decodeType,
                    uri = playerInfo.uri,
                    header = playerInfo.header,
                )
            }
        }
    }
}
