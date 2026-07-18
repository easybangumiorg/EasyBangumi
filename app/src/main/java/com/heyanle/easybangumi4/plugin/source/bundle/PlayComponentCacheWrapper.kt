package com.heyanle.easybangumi4.plugin.source.bundle

import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.plugin.api.SourceResult
import com.heyanle.easybangumi4.plugin.api.component.ComponentWrapper
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import com.heyanle.easybangumi4.plugin.api.component.play.PlayComponent
import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.api.entity.CartoonSummary
import com.heyanle.easybangumi4.plugin.api.entity.Episode
import com.heyanle.easybangumi4.plugin.api.entity.PlayLine
import com.heyanle.easybangumi4.plugin.api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.getMD5
import com.heyanle.easybangumi4.utils.logi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PlayComponentCacheWrapper(
    private val delegate: PlayComponent,
    private val cacheFolder: File = File(APP.getFilePath("play_info_cache")),
) : ComponentWrapper(), PlayComponent {

    private val cacheRoot = cacheFolder.apply {
        mkdirs()
    }
    private val cacheAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(CachedPlayerInfo::class.java)

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
                logCache("hit", summary, playLine, episode, cacheKey, it.uri)
                return SourceResult.Complete(it, isCache = true)
            }
            logCache("miss", summary, playLine, episode, cacheKey)
        } else {
            logCache("invalidate", summary, playLine, episode, cacheKey)
            delete(cacheKey)
        }
        val result = delegate.getPlayInfo(summary, playLine, episode, canCache = false)
        if (result is SourceResult.Complete && !result.isCache) {
            write(cacheKey, result.data)
            logCache("write", summary, playLine, episode, cacheKey, result.data.uri)
        }
        return result
    }

    override suspend fun getPlayInfo(
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        verificationResult: VerificationResult,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        val cacheKey = cacheKey(summary, playLine, episode)
        if (!canCache) {
            logCache("invalidate", summary, playLine, episode, cacheKey)
            delete(cacheKey)
        }
        val result = delegate.getPlayInfo(
            summary = summary,
            playLine = playLine,
            episode = episode,
            verificationResult = verificationResult,
            canCache = false,
        )
        if (result is SourceResult.Complete && !result.isCache) {
            write(cacheKey, result.data)
            logCache("write", summary, playLine, episode, cacheKey, result.data.uri)
        }
        return result
    }

    override suspend fun getPlayInfo(
        cartoon: Cartoon,
        playLine: PlayLine,
        episode: Episode,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        val summary = CartoonSummary(cartoon.id, cartoon.source)
        val cacheKey = cacheKey(summary, playLine, episode)
        if (canCache) {
            read(cacheKey)?.let {
                logCache("hit", summary, playLine, episode, cacheKey, it.uri)
                return SourceResult.Complete(it, isCache = true)
            }
            logCache("miss", summary, playLine, episode, cacheKey)
        } else {
            logCache("invalidate", summary, playLine, episode, cacheKey)
            delete(cacheKey)
        }
        val result = delegate.getPlayInfo(cartoon, playLine, episode, canCache = false)
        if (result is SourceResult.Complete && !result.isCache) {
            write(cacheKey, result.data)
            logCache("write", summary, playLine, episode, cacheKey, result.data.uri)
        }
        return result
    }

    override suspend fun getPlayInfo(
        cartoon: Cartoon,
        playLine: PlayLine,
        episode: Episode,
        verificationResult: VerificationResult,
        canCache: Boolean,
    ): SourceResult<PlayerInfo> {
        val summary = CartoonSummary(cartoon.id, cartoon.source)
        val cacheKey = cacheKey(summary, playLine, episode)
        if (!canCache) {
            logCache("invalidate", summary, playLine, episode, cacheKey)
            delete(cacheKey)
        }
        val result = delegate.getPlayInfo(
            cartoon = cartoon,
            playLine = playLine,
            episode = episode,
            verificationResult = verificationResult,
            canCache = false,
        )
        if (result is SourceResult.Complete && !result.isCache) {
            write(cacheKey, result.data)
            logCache("write", summary, playLine, episode, cacheKey, result.data.uri)
        }
        return result
    }

    private fun cacheKey(summary: CartoonSummary, playLine: PlayLine, episode: Episode): String {
        return "${summary.source}|${summary.id}|${playLine.id}|${episode.id}".getMD5()
    }

    private fun logCache(
        action: String,
        summary: CartoonSummary,
        playLine: PlayLine,
        episode: Episode,
        key: String,
        uri: String? = null,
    ) {
        runCatching {
            "play-cache action=$action source=${summary.source} cartoonId=${summary.id} lineId=${playLine.id} episodeId=${episode.id} key=$key uri=${uri.orEmpty()}".logi(TAG)
        }
    }

    private suspend fun read(key: String): PlayerInfo? = withContext(Dispatchers.IO) {
        val file = File(cacheRoot, "$key.json")
        if (!file.isFile || !file.canRead()) return@withContext null
        runCatching {
            cacheAdapter.fromJson(file.readText())?.toPlayerInfo()
        }.getOrNull()
    }

    private suspend fun write(key: String, playerInfo: PlayerInfo) = withContext(Dispatchers.IO) {
        val file = File(cacheRoot, "$key.json")
        runCatching {
            file.writeText(cacheAdapter.toJson(CachedPlayerInfo.from(playerInfo)))
        }
    }

    private suspend fun delete(key: String) = withContext(Dispatchers.IO) {
        val file = File(cacheRoot, "$key.json")
        runCatching {
            if (file.isFile) {
                file.delete()
            }
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

    private companion object {
        const val TAG = "PlayInfoCache"
    }
}
