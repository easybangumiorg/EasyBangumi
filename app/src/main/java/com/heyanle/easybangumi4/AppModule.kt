package com.heyanle.easybangumi4

import android.app.Application
import com.google.gson.Gson
import com.heyanle.easybangumi4.bus.DownloadingBus
import com.heyanle.easybangumi4.splash.SplashGuildController
import com.heyanle.easybangumi4.theme.EasyThemeController
import com.heyanle.easybangumi4.utils.MoshiArrayListJsonAdapter
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Created by heyanlin on 2023/10/30.
 */

// 这里注册的时机最早，在 Application 的 构造函数中注册
class RootModule(
    private val application: Application,
): InjectModule {
    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            Gson()
        }
        addSingletonFactory {
            Moshi.Builder()
                .add(MoshiArrayListJsonAdapter.FACTORY)
                .addLast(KotlinJsonAdapterFactory())
                .build()
        }
    }
}

class ControllerModule(
    private val application: Application
) : InjectModule {

    // 大部分 Controller 都在自己业务的 Module 里注册，这里注册一些额外的
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            application
        }
        addSingletonFactory {
            EasyThemeController(get())
        }
        addSingletonFactory {
            DownloadingBus()
        }
        addSingletonFactory {
            SplashGuildController(get(), get())
        }
    }
}