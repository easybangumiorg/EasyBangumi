package org.easybangumi.next.shared.plugin.api

import org.easybangumi.next.shared.plugin.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.plugin.api.component.filter.FilterComponent
import org.easybangumi.next.shared.plugin.api.component.play.PlayComponent
import org.easybangumi.next.shared.plugin.api.component.pref.PrefComponent
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.utils.PreferenceHelper
import org.easybangumi.next.shared.plugin.api.utils.StringHelper
import org.easybangumi.next.shared.plugin.api.utils.WebViewHelper
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2025/3/3.
 */
object ConstClazz {

    // Source 里的接口
    val sourceClazz: Set<KClass<*>> = setOf(
        Source::class,
    )

    // 工具类接口
    val utilsClazz: Set<KClass<*>> = setOf(
        PreferenceHelper::class,
        WebViewHelper::class,
        StringHelper::class,
    )

    // Component 接口，纯业务
    val componentClazz: Set<KClass<*>> = setOf(
        DiscoverComponent::class,
        FilterComponent::class,
        PlayComponent::class,
        PrefComponent::class,
    )

}