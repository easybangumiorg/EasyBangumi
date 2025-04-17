package org.easybangumi.next.shared.plugin.api

import org.easybangumi.next.shared.plugin.api.component.Component
import org.easybangumi.next.shared.plugin.api.source.MetaSource
import org.easybangumi.next.shared.plugin.api.source.PlaySource
import org.easybangumi.next.shared.plugin.api.source.Source
import org.easybangumi.next.shared.plugin.api.utils.PreferenceHelper
import org.easybangumi.next.shared.plugin.api.utils.StringHelper
import org.easybangumi.next.shared.plugin.api.utils.WebViewHelper
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/3/3.
 */
object ConstClazz {

    // Source 里的接口
    val sourceClazz: Array<KClass<*>> = arrayOf(
        Source::class,
        MetaSource::class,
        PlaySource::class,
    )

    // 工具类接口
    val utilsClazz: Array<KClass<*>> = arrayOf(
        PreferenceHelper::class,
        WebViewHelper::class,
        StringHelper::class,
    )

    // Component 接口，纯业务
    val componentClazz: Array<KClass<*>> = arrayOf(
//        DetailedComponent::class,
//        MediaEventComponent::class,
//        PlayComponent::class,
//        SearchComponent::class,
//        HomeComponent::class,
//        PrefComponent::class,
    )

}