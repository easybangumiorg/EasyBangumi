package org.easybangumi.next.libplayer.exoplayer

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

/**
 * Created by HeYanLe on 2023/8/13 21:21.
 * https://github.com/heyanLE
 */
@OptIn(UnstableApi::class)
class ExoMediaSourceFactory(
    private val context: Context,
) {

    fun getMediaItem(mediaItem: LibMediaItem): ExoMediaItem {
        return MediaItem.fromUri(mediaItem.uri)
    }

    /**
     * Http <- 缓存区
     */
    fun getMediaSourceFactory(mediaItem: LibMediaItem): MediaSource.Factory {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mediaItem.header ?: emptyMap())
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        // TODO 缓存
        val normalCacheDataSourceFactory = CacheDataSource.Factory()
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return when (mediaItem.mediaType) {
            LibMediaItem.MEDIA_TYPE_NORMAL -> ProgressiveMediaSource.Factory(dataSourceFactory)
            LibMediaItem.MEDIA_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            else -> {
                if (mediaItem.uri.contains(".m3u8")) {
                    HlsMediaSource.Factory(dataSourceFactory)
                } else {
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                }
            }
        }
    }

//    /**
//     * Http <- 缓存区 <- 下载区
//     */
//    fun getMediaSourceFactoryWithDownload(playerInfo: PlayerInfo): MediaSource.Factory {
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
//            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
//        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)
//
//        val streamDataSinkFactory = CacheDataSink.Factory().setCache(normalCache)
//        val normalCacheDataSourceFactory = CacheDataSource.Factory()
//            .setCache(normalCache)
//            .setUpstreamDataSourceFactory(dataSourceFactory)
//            .setCacheWriteDataSinkFactory(streamDataSinkFactory)
//            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
//
//        // 下载区不许写
//        val normalDownloadCacheDataSourceFactory = CacheDataSource.Factory()
//            .setCache(downloadCache)
//            .setUpstreamDataSourceFactory(normalCacheDataSourceFactory)
//            .setCacheWriteDataSinkFactory(null)
//
//
//        return when (playerInfo.decodeType) {
//            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(normalDownloadCacheDataSourceFactory)
//            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(normalDownloadCacheDataSourceFactory)
//            else -> ProgressiveMediaSource.Factory(normalDownloadCacheDataSourceFactory)
//        }
//    }
//
//
//    // HTTP
//    fun getDataSourceFactory(playerInfo: PlayerInfo): DefaultDataSource.Factory {
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
//            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
//        return DefaultDataSource.Factory(APP, httpDataSourceFactory)
//    }
//
//    fun getMediaSourceWithoutCache(playerInfo: PlayerInfo): MediaSource.Factory {
//        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
//            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
//        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)
//
//        return when (playerInfo.decodeType) {
//            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
//            C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
//            else -> ProgressiveMediaSource.Factory(dataSourceFactory)
//        }
//    }
//
//    fun getClipMediaSourceFactory(playerInfo: PlayerInfo, clippingConfiguration: ClippingConfiguration): ClippingConfigMediaSourceFactory {
//        return ClippingConfigMediaSourceFactory(getMediaSourceFactory(playerInfo), clippingConfiguration)
//    }
//
//
//    @OptIn(androidx.media3.common.util.UnstableApi::class)
//    fun getWithCache(playerInfo: PlayerInfo): MediaSource {
//        return getMediaSourceFactory(playerInfo).createMediaSource(getMediaItem(playerInfo))
//    }
//
//    @OptIn(androidx.media3.common.util.UnstableApi::class)
//    fun getWithoutCache(playerInfo: PlayerInfo): MediaSource {
//        return getMediaSourceWithoutCache(playerInfo).createMediaSource(getMediaItem(playerInfo))
//    }

}