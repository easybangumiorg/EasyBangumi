package com.heyanle.easybangumi4.exo

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo

/**
 * Created by HeYanLe on 2023/8/13 21:21.
 * https://github.com/heyanLE
 */
@UnstableApi
class CartoonMediaSourceFactory(
    private val normalCache: Cache,
) {

    fun getMediaItem(playerInfo: PlayerInfo): MediaItem {
        return MediaItem.fromUri(playerInfo.uri)
    }

    /**
     * Http <- 缓存区
     */
    fun getMediaSourceFactory(playerInfo: PlayerInfo): MediaSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)
        val streamDataSinkFactory = CacheDataSink.Factory().setCache(normalCache)
        val normalCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(normalCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(streamDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return when (playerInfo.decodeType) {
            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(normalCacheDataSourceFactory)
            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(normalCacheDataSourceFactory)
            else -> ProgressiveMediaSource.Factory(normalCacheDataSourceFactory)
        }
    }

    fun getClipMediaSourceFactory(playerInfo: PlayerInfo, clippingConfiguration: ClippingConfiguration): ClippingConfigMediaSourceFactory {
        return ClippingConfigMediaSourceFactory(getMediaSourceFactory(playerInfo), clippingConfiguration)
    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun get(playerInfo: PlayerInfo): MediaSource {
        return getMediaSourceFactory(playerInfo).createMediaSource(getMediaItem(playerInfo))
    }

}