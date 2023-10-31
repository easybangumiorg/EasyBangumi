package com.heyanle.easybangumi4.exo

import android.app.Application
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addAlias
import com.heyanle.injekt.api.addPerKeyFactory
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import java.io.File

/**
 * Created by HeYanLe on 2023/8/13 14:23.
 * https://github.com/heyanLE
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory<DatabaseProvider> {
            StandaloneDatabaseProvider(application)
        }

        addSingletonFactory {
            MediaCacheDB(application)
        }

        addSingletonFactory {
            StandaloneDatabaseProvider(application)
        }

        addSingletonFactory {
            EasyExoPlayer(
                ExoPlayer.Builder(application)
                    //.setRenderersFactory(MixRenderersFactory(app))
                    .build().also {
                        it.loge("ExoPlayer-----")
                    }
            )
        }
        // 强制使用 EasyExoPlayer
        addAlias<EasyExoPlayer, ExoPlayer>()

        addSingletonFactory<HttpDataSource.Factory> {
            DefaultHttpDataSource.Factory()
        }

        addSingletonFactory {
            HeaderDataSourceFactory(application)
        }

        addSingletonFactory {
            MediaSourceFactory(get<Cache>(false))
        }

        // 以下实体都跟缓存上限有关，0 为无限制或下载
        // Cache
        addPerKeyFactory<Cache, Boolean> { isDownload ->
            if (isDownload) {
                val downloadFolder = File(application.getFilePath(), "download")
                SimpleCache(downloadFolder, NoOpCacheEvictor(), get<StandaloneDatabaseProvider>())
            } else {
                val settingPreferences: SettingPreferences = get()
                val cacheSize = settingPreferences.cacheSize.get()
                val lruEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
                SimpleCache(
                    File(application.getCachePath("media")),
                    lruEvictor,
                    get<MediaCacheDB>()
                )
            }
        }

        // CacheDataSource.Factory
        addPerKeyFactory<CacheDataSource.Factory, Long> { cacheSize ->
            val cache = get<Cache>(false)
            if (cacheSize == 0L) {
                CacheDataSource.Factory()
                    .setCache(cache)
                    .setUpstreamDataSourceFactory(get<HttpDataSource.Factory>())
                    .setCacheWriteDataSinkFactory(null) // 只读不写，只允许下载器写
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            } else {
                val streamDataSinkFactory = CacheDataSink.Factory().setCache(cache)
                CacheDataSource.Factory()
                    .setCache(cache)
                    // 将 无限制的 下载 dataSource 组合
                    .setUpstreamDataSourceFactory(get<CacheDataSource.Factory>(0))
                    .setCacheWriteDataSinkFactory(streamDataSinkFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            }

        }

        // CacheDataSource
        addPerKeyFactory<CacheDataSource, Long> { cacheSize ->
            val factory = get<CacheDataSource.Factory>(cacheSize)
            factory.createDataSource()
        }


    }
}