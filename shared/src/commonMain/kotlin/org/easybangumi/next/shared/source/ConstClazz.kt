package org.easybangumi.next.shared.source

import org.easybangumi.next.shared.source.api.component.collect.CollectComponent
import org.easybangumi.next.shared.source.api.component.detail.DetailComponent
import org.easybangumi.next.shared.source.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.source.api.component.event.EventComponent
import org.easybangumi.next.shared.source.api.component.filter.FilterComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.pref.PrefComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.source.Source
import org.easybangumi.next.shared.source.api.utils.EventBusHelper
import org.easybangumi.next.shared.source.api.utils.PreferenceHelper
import org.easybangumi.next.shared.source.api.utils.StringHelper
import org.easybangumi.next.shared.source.api.utils.WebViewHelper
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
        EventBusHelper::class,
    )

    // Component 接口，纯业务
    val componentClazz: Set<KClass<*>> = setOf(
        DiscoverComponent::class,
        FilterComponent::class,
        PlayComponent::class,
        PrefComponent::class,
        DetailComponent::class,
        EventComponent::class,
        CollectComponent::class,
        SearchComponent::class, // 需要在插件中实现
    )

}