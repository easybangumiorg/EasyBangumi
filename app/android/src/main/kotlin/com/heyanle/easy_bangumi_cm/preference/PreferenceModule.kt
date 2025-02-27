package com.heyanle.easy_bangumi_cm.preference

import com.heyanle.easy_bangumi_cm.EasyApplication
import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.utils.HeKV
import com.heyanle.easy_bangumi_cm.base.utils.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.base.utils.preference.hekv.HeKVPreferenceStore
import com.heyanle.lib.inject.api.*

/**
 * Created by heyanlin on 2025/2/27.
 */
class PreferenceModule(
    private val application: EasyApplication
) : InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            HeKV(get<IPathProvider>().getFilePath("global"), "global")
        }

        addSingletonFactory {
            AndroidPreferenceStore(application)
        }
        addAlias<AndroidPreferenceStore, PreferenceStore>()
    }
}