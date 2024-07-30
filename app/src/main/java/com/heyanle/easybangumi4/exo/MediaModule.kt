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
import com.heyanle.easybangumi4.exo.download.ExoDownloadController
import com.heyanle.easybangumi4.exo.download.MediaDownloadCacheDB
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.utils.getCachePath
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addPerKeyFactory
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get
import com.heyanle.inject.api.getOrNull
import java.io.File

/**
 * Created by HeYanLe on 2023/8/13 14:23.
 * https://github.com/heyanLE
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MediaModule(
    private val application: Application
) : InjectModule {
    override fun InjectScope.registerInjectables() {


        addSingletonFactory {
            MediaCacheDB(application)
        }

        addSingletonFactory {
            MediaDownloadCacheDB(application)
        }


        addScopedFactory {
            ExoPlayer.Builder(application).apply {
                getOrNull<SettingPreferences>()?.fastSecond?.get()?.let {
                    setSeekBackIncrementMs(it*1000L)
                    setSeekForwardIncrementMs(it*1000L)
                }
            }
        }

        addSingletonFactory<HttpDataSource.Factory> {
            DefaultHttpDataSource.Factory()
        }

        addSingletonFactory {
            CartoonMediaSourceFactory(get<Cache>(false), get<Cache>(true))
        }

        addSingletonFactory {
            ExoDownloadController(application, get(), get<Cache>(true), get(), get())
        }


        // 以下实体都跟缓存上限有关，0 为无限制或下载
        // Cache
        addPerKeyFactory<Cache, Boolean> { isDownload ->
            if (isDownload) {
                val downloadFolder = File(application.getFilePath(), "download")
                SimpleCache(downloadFolder, NoOpCacheEvictor(), get<MediaDownloadCacheDB>())
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



    }
}