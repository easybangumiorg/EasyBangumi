package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonDetailed
import com.heyanle.easy_bangumi_cm.model.cartoon.CartoonIndex
import com.heyanle.easy_bangumi_cm.model.cartoon.PlayerLine
import com.heyanle.easy_bangumi_cm.plugin.api.base.SourceResult
import com.heyanle.easy_bangumi_cm.plugin.api.base.withResult
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentWrapper
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.DetailedComponent

/**
 * Created by heyanlin on 2025/2/6.
 */
class DebugDetailComponent : DetailedComponent, ComponentWrapper() {

    override suspend fun detailed(cartoonIndex: CartoonIndex): SourceResult<Pair<CartoonDetailed, List<PlayerLine>>> {
        return withResult(CoroutineProvider.io) {
            throw NotImplementedError("TODO")
        }
    }
}