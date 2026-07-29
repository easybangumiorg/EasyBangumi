package com.heyanle.easybangumi4.danmaku

import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.inject.api.InjectModule
import com.heyanle.inject.api.InjectScope
import com.heyanle.inject.api.addAlias
import com.heyanle.inject.api.addSingletonFactory
import com.heyanle.inject.api.get

class DanmakuModule : InjectModule {
    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            DanDanPlayCredentials(
                appId = BuildConfig.DANDANPLAY_APP_ID,
                appSecret = BuildConfig.DANDANPLAY_APP_SECRET,
            )
        }
        addSingletonFactory {
            DanmakuPreferences(get())
        }
        addSingletonFactory {
            DanmakuDisplayPreferences(get())
        }
        addSingletonFactory {
            DanmakuStorage(get())
        }
        addSingletonFactory {
            DanDanPlaySource(get())
        }
        addSingletonFactory {
            InnerDanmakuSourceRegistry.create(get())
        }
        addSingletonFactory {
            DanmakuRepository(get(), get(), get())
        }
        addAlias<DanmakuRepository, DanmakuPlaybackRepository>()
        addAlias<DanmakuRepository, DanmakuPlaybackStore>()
        addSingletonFactory {
            DanmakuRequestCoordinator(get())
        }
    }
}
