package com.heyanle.easybangumi4.exo

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.heyanle.bangumi_source_api.api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/8/13 21:21.
 * https://github.com/heyanLE
 */
@UnstableApi
class MediaSourceFactory(
    private val normalCache: Cache,
) {

    /**
     * Http <- 缓存区
     */
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun get(playerInfo: PlayerInfo): MediaSource {

        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())

        val streamDataSinkFactory = CacheDataSink.Factory().setCache(normalCache)
        val normalCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(normalCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(streamDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val media = when (playerInfo.decodeType) {
            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(normalCacheDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(playerInfo.uri))

            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(normalCacheDataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(playerInfo.uri)
                )

            else -> ProgressiveMediaSource.Factory(normalCacheDataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(playerInfo.uri)
                )
        }
        return media
    }

}