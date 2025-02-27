package com.heyanle.easy_bangumi_cm.preferences

import com.heyanle.easy_bangumi_cm.base.service.provider.IPathProvider
import com.heyanle.easy_bangumi_cm.base.utils.HeKV
import com.heyanle.easy_bangumi_cm.base.utils.preference.PreferenceStore
import com.heyanle.easy_bangumi_cm.base.utils.preference.hekv.HeKVPreferenceStore
import com.heyanle.lib.inject.api.*

/**
 * Created by heyanlin on 2025/2/27.
 */
class PreferenceModule : InjectModule {

    override fun InjectScope.registerInjectables() {
        addSingletonFactory {
            HeKV(get<IPathProvider>().getFilePath("global"), "global")
        }

        addSingletonFactory {
            HeKVPreferenceStore(get())
        }
        addAlias<HeKVPreferenceStore, PreferenceStore>()
    }
}