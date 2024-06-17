package com.heyanle.easybangumi4.exo

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultAssetLoaderFactory
import androidx.media3.transformer.DefaultDecoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExoPlayerAssetLoader
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMuxer
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationException
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.TransformationResult
import androidx.media3.transformer.Transformer
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.source_api.entity.PlayerInfo
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

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

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(playerInfo.header ?: emptyMap())
        val dataSourceFactory = DefaultDataSource.Factory(APP, httpDataSourceFactory)
        val streamDataSinkFactory = CacheDataSink.Factory().setCache(normalCache)
        val normalCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(normalCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(streamDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

//        val d = MediaItem.Builder().setMimeType(MimeTypes.APPLICATION_M3U8).setUri(playerInfo.uri).build()
//        EditedMediaItem.Builder(d)
//            .build()
//        val transformer = Transformer.Builder(APP)
//            .setAssetLoaderFactory(ExoPlayerAssetLoader.Factory(
//                APP, DefaultDecoderFactory(APP),   Clock.DEFAULT,  HlsMediaSource.Factory(normalCacheDataSourceFactory)
//            ))
//            .setVideoMimeType(MimeTypes. VIDEO_H264)
//            .setMuxerFactory(InAppMuxer.Factory.Builder().setOutputFragmentedMp4(false).setFragmentDurationMs(C.TIME_UNSET).build())
//            .build()
//        transformer.start(d, File(APP.getFilePath("test"), "test.mp4").absolutePath)
//        val progressHolder = ProgressHolder()
////        MainScope().launch {
////            while (isActive) {
////                delay(1000)
////                val d = transformer.getProgress(progressHolder)
////                d.logi("progress")
////
////            }
////        }


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