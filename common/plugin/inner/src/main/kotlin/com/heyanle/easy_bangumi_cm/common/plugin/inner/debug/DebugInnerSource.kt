package com.heyanle.easy_bangumi_cm.common.plugin.inner.debug

import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component.DebugDetailComponent
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component.DebugHomeComponent
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component.DebugPrefComponent
import com.heyanle.easy_bangumi_cm.common.plugin.inner.debug.component.DebugSearchComponent
import com.heyanle.easy_bangumi_cm.common.resources.Res
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import kotlin.reflect.KClass

/**
 * 调试番源，Mock 用
 * Created by heyanlin on 2025/2/6.
 */
class DebugInnerSource : InnerSource() {

    companion object {
        const val ID = "inner_source:debug"
    }

    override val id: String = ID

    override val label: ResourceOr = Res.strings.debug_source
    override val icon: ResourceOr = Res.images.logo

    override val version: Int = 1
    override val componentClazz: List<KClass<out Component>> = listOf(
        DebugHomeComponent::class,
        DebugPrefComponent::class,
        DebugDetailComponent::class,
        DebugSearchComponent::class,
    )

}