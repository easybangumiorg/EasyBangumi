package com.heyanle.easybangumi4

import android.app.Application
import com.google.gson.Gson
import com.heyanle.easybangumi4.base.db.AppDatabase
import com.heyanle.easybangumi4.base.db.AppLocalDatabase
import com.heyanle.easybangumi4.base.db.CacheDatabase
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.base.theme.EasyThemeController
import com.heyanle.easybangumi4.download.DownloadBus
import com.heyanle.easybangumi4.download.DownloadController
import com.heyanle.easybangumi4.download.LocalCartoonController
import com.heyanle.easybangumi4.preferences.SettingMMKVPreferences
import com.heyanle.easybangumi4.preferences.SettingPreferences
import com.heyanle.easybangumi4.preferences.SourcePreferences
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.source.SourceMigrationController
import com.heyanle.easybangumi4.source.utils.SourceProviderControllerImpl
import com.heyanle.easybangumi4.ui.cartoon_play.CartoonPlayingController
import com.heyanle.easybangumi4.ui.main.star.update.CartoonUpdateController
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addAlias
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import com.heyanle.lib_anim.utils.provider.SourceProviderController
import java.io.File

/**
 * Created by HeYanLe on 2023/7/29 20:15.
 * https://github.com/heyanLE
 */


object RootModule : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            Gson()
        }
    }

}

// Controller
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class ControllerModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            application
        }
        addSingletonFactory {
            EasyThemeController(get())
        }
        addSingletonFactory {
            CartoonUpdateController(get(), get())
        }
        addSingletonFactory {
            SourceController(get(), get())
        }
        addSingletonFactory {
            SourceMigrationController(application, get(), get())
        }
        addSingletonFactory {
            SourceProviderControllerImpl(application)
        }
        addAlias<SourceProviderControllerImpl, SourceProviderController>()

        addSingletonFactory {
            CartoonPlayingController(
                get(), get(), get(), get(), get()
            )
        }

        addSingletonFactory {
            DownloadController(
                File(application.getFilePath("download")),
                get(),
                get(),
                get()
            )
        }
        addSingletonFactory {
            DownloadBus()
        }

        addSingletonFactory {
            LocalCartoonController(application)
        }
    }
}

class PreferencesModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            AndroidPreferenceStore(application)
        }
        addSingletonFactory {
            MMKVPreferenceStore(application)
        }

        addSingletonFactory {
            HeKV(application.getFilePath(), "global")
        }
        addSingletonFactory {
            HeKVPreferenceStore(get())
        }
        // 默认使用 sp
        addAlias<AndroidPreferenceStore, PreferenceStore>()

        addSingletonFactory {
            SettingPreferences(get<AndroidPreferenceStore>())
        }
        addSingletonFactory {
            SourcePreferences(get<HeKVPreferenceStore>())
        }
        addSingletonFactory {
            SettingMMKVPreferences(get<MMKVPreferenceStore>())
        }
    }
}

class DatabaseModule(
    private val application: Application
) : InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            AppDatabase.build(application)
        }
        addSingletonFactory {
            get<AppDatabase>().cartoonStar
        }
        addSingletonFactory {
            get<AppDatabase>().cartoonHistory
        }
        addSingletonFactory {
            get<AppDatabase>().searchHistory
        }
        addSingletonFactory {
            get<AppDatabase>().cartoonTag
        }
        addSingletonFactory {
            CacheDatabase.build(application)
        }
        addSingletonFactory {
            get<CacheDatabase>().cartoonInfo
        }
        addSingletonFactory {
            AppLocalDatabase.build(application)
        }
        addSingletonFactory {
            get<AppLocalDatabase>().cartoonDownload
        }
    }
}