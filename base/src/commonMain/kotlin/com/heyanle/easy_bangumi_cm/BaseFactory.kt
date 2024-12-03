package com.heyanle.easy_bangumi_cm

import com.heyanle.easy_bangumi_cm.base.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.Logger
import com.heyanle.easy_bangumi_cm.base.Platform
import com.heyanle.easy_bangumi_cm.base.path_provider.PathProvider
import com.heyanle.easy_bangumi_cm.base.preference.PreferenceStore
import org.koin.core.definition.Definition

/**
 * Created by heyanlin on 2024/12/3.
 */
interface BaseFactory {

    val makePathProvider: Definition<PathProvider>

    val makeCoroutineProvider: Definition<CoroutineProvider>

    val makeLogger: Definition<Logger>

    val makePreferenceStore: Definition<PreferenceStore>

    val makePlatform: Definition<Platform>

}