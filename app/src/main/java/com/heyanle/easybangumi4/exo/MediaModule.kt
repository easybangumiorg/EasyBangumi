package com.heyanle.easybangumi4.exo

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import com.heyanle.easybangumi4.base.utils.getCachePath
import com.heyanle.easybangumi4.base.utils.getFilePath
import com.heyanle.easybangumi4.utils.loge
import com.heyanle.easybangumi4.setting.SettingPreferences
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

/**
 * Created by HeYanLe on 2023/8/13 14:23.
 * https://github.com/heyanLE
 */

enum class NeedCache{
    TRUE, FALSE
}

@UnstableApi
val mediaModule = module {
    single {
        StandaloneDatabaseProvider(get())
    } bind DatabaseProvider::class

    single {
        MediaCacheDB(get())
    }
    single {
        StandaloneDatabaseProvider(get())
    }
    single {
        ExoPlayer.Builder(get())
            //.setRenderersFactory(MixRenderersFactory(app))
            .build().also {
                it.loge("ExoPlayer-----")
            }
    }

    single {
        DefaultHttpDataSource.Factory()
    } bind HttpDataSource.Factory::class

    single {
        HeaderDataSourceFactory(get())
    }

    single {
        MediaSourceFactory(get<Cache>(named<NeedCache>(NeedCache.FALSE)))
    }

    single(named(NeedCache.TRUE)) {
        val downloadFolder = File(get<Context>().getFilePath(), "download")
        SimpleCache(downloadFolder, NoOpCacheEvictor(), get<StandaloneDatabaseProvider>())
    }

    single(named(NeedCache.FALSE)) {
        val settingPreferences: SettingPreferences = get()
        val cacheSize = settingPreferences.cacheSize.get()
        val lruEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        SimpleCache(
            File(get<Context>().getCachePath("media")),
            lruEvictor,
            get<MediaCacheDB>()
        )
    }
}