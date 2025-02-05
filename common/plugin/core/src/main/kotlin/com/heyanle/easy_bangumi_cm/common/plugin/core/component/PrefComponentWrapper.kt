package com.heyanle.easy_bangumi_cm.common.plugin.core.component

import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.PrefComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.SourcePreference
import kotlinx.coroutines.runBlocking

/**
 * Created by heyanlin on 2025/2/5.
 */
class PrefComponentWrapper(
    private val prefComponent: PrefComponent
): PrefComponent by prefComponent {

    val prefList: List<SourcePreference> by lazy {
        runBlocking {
            register()
        }
    }

}