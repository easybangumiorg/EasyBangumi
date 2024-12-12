package com.heyanle.easy_bangumi_cm.plugin.api.component.pref

import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle


/**
 * Created by HeYanLe on 2024/12/8 22:08.
 * https://github.com/heyanLE
 */

interface PrefComponent : Component {

    suspend fun register(): List<SourcePreference>

}

fun ComponentBundle.prefComponent(): PrefComponent?{
    return this.getComponent(PrefComponent::class)
}