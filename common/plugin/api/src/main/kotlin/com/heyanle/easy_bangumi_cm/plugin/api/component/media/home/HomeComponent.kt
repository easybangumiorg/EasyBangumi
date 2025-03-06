package com.heyanle.easy_bangumi_cm.plugin.api.component.media.home

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent


/**
 * Created by HeYanLe on 2024/12/8 21:33.
 * https://github.com/heyanLE
 */

interface HomeComponent : MediaComponent {

    suspend fun home(): SourceResult<HomeContent>

}

fun ComponentBundle.homeComponent(): HomeComponent? = getComponent(HomeComponent::class)