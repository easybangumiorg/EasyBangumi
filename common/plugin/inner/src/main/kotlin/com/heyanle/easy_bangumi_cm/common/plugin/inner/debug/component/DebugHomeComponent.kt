package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.DebugInnerSource
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.base.withResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper

/**
 * Created by heyanlin on 2025/2/6.
 */
class DebugHomeComponent(
    private val debugInnerSource: DebugInnerSource,
    private val stringHelper: StringHelper,
): HomeComponent, ComponentWrapper() {

    override suspend fun home(): SourceResult<List<HomePage>> {
        return withResult(CoroutineProvider.io) {
            listOf()
        }
    }

}