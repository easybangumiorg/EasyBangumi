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
import androidx.media3.exoplayer.offline.DownloadRequest
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
    private val downloadCache: Cache,
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

    /**
     * Http <- 缓存区 <- 下载区
     */
    fun getMediaSourceFactoryWithDownload(playerInfo: PlayerInfo): MediaSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)

        val streamDataSinkFactory = CacheDataSink.Factory().setCache(normalCache)
        val normalCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(normalCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(streamDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        // 下载区不许写
        val normalDownloadCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(normalCacheDataSourceFactory)
            .setCacheWriteDataSinkFactory(null)


        return when (playerInfo.decodeType) {
            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(normalDownloadCacheDataSourceFactory)
            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(normalDownloadCacheDataSourceFactory)
            else -> ProgressiveMediaSource.Factory(normalDownloadCacheDataSourceFactory)
        }
    }


    // HTTP
    fun getDataSourceFactory(playerInfo: PlayerInfo): DefaultDataSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
        return DefaultDataSource.Factory(APP, httpDataSourceFactory)
    }

    fun getMediaSourceWithoutCache(playerInfo: PlayerInfo): MediaSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)

        return when (playerInfo.decodeType) {
            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory)
        }
    }

    fun getClipMediaSourceFactory(playerInfo: PlayerInfo, clippingConfiguration: ClippingConfiguration): ClippingConfigMediaSourceFactory {
        return ClippingConfigMediaSourceFactory(getMediaSourceFactory(playerInfo), clippingConfiguration)
    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getWithCache(playerInfo: PlayerInfo): MediaSource {
        return getMediaSourceFactory(playerInfo).createMediaSource(getMediaItem(playerInfo))
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getWithoutCache(playerInfo: PlayerInfo): MediaSource {
        return getMediaSourceWithoutCache(playerInfo).createMediaSource(getMediaItem(playerInfo))
    }

}