package com.heyanle.easybangumi4

import android.app.Application
import com.google.gson.Gson
import com.heyanle.easybangumi4.base.db.AppDatabase
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get
import com.heyanle.injekt.core.InjektMain

/**
 * Created by HeYanLe on 2023/7/29 20:15.
 * https://github.com/heyanLE
 */
object RootModule: InjektMain() {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            Gson()
        }
    }
}

class ControllerModule(
    private val application: Application
): InjektModule {
    override fun InjektScope.registerInjectables() {
        TODO("Not yet implemented")
    }
}

class PreferencesModule(
    private val application: Application
): InjektModule {
    override fun InjektScope.registerInjectables() {
        addSingletonFactory {
            AndroidPreferenceStore(application)
        }

        addSingletonFactory {
            SettingPreferences(get())
        }

        addSingletonFactory {
            SourcePreferences(get())
        }
    }
}

class DatabaseModule(
    private val application: Application
): InjektModule {
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

    }
}