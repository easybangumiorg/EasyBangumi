package com.heyanle.easy_bangumi_cm.plugin.api.component.media.home

import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentContainer
import com.heyanle.easy_bangumi_cm.plugin.api.component.MediaComponent


/**
 * Created by HeYanLe on 2024/12/8 21:33.
 * https://github.com/heyanLE
 */

interface HomeComponent : MediaComponent {

    class NonLabelSingleHomePage(
        private val page: HomePage
    ) : List<HomePage> by listOf(page)

    suspend fun home(): SourceResult<List<HomePage>>


}

fun ComponentContainer.homeComponent(): HomeComponent? = getComponent(HomeComponent::class)