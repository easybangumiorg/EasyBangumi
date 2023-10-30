package com.heyanle.easybangumi4.setting

import android.app.Application
import com.heyanle.easybangumi4.base.hekv.HeKV
import com.heyanle.easybangumi4.base.preferences.PreferenceStore
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.base.preferences.hekv.HeKVPreferenceStore
import com.heyanle.easybangumi4.base.preferences.mmkv.MMKVPreferenceStore
import com.heyanle.easybangumi4.source.SourcePreferences
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.injekt.api.InjektModule
import com.heyanle.injekt.api.InjektScope
import com.heyanle.injekt.api.addAlias
import com.heyanle.injekt.api.addSingletonFactory
import com.heyanle.injekt.api.get

/**
 * Created by heyanlin on 2023/10/30.
 */
class SettingModule(
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
            SettingPreferences(application, get<AndroidPreferenceStore>())
        }
        addSingletonFactory {
            SourcePreferences(get<HeKVPreferenceStore>())
        }
        addSingletonFactory {
            SettingMMKVPreferences(get<MMKVPreferenceStore>())
        }
    }
}