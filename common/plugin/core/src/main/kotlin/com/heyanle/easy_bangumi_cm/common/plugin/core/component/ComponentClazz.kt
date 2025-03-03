package com.heyanle.easy_bangumi_cm.common.plugin.core.component

import com.heyanle.easy_bangumi_cm.common.plugin.core.inner.InnerSource
import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.DetailedComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.MediaEventComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.PlayComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.SearchComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomeComponent
import com.heyanle.easy_bangumi_cm.plugin.api.component.pref.PrefComponent
import com.heyanle.easy_bangumi_cm.plugin.api.source.MediaSource
import com.heyanle.easy_bangumi_cm.plugin.api.source.MetaSource
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.utils.PreferenceHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.StringHelper
import com.heyanle.easy_bangumi_cm.plugin.utils.WebViewHelper
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/3/3.
 */
object ComponentClazz {

    // Source 里的接口
    val sourceClazz: Set<KClass<*>> = setOf(
        Source::class,
        MetaSource::class,
        MediaSource::class,
        InnerSource::class,
    )

    // 工具类接口
    val utilsClazz: Set<KClass<*>> = setOf(
        PreferenceHelper::class,
        WebViewHelper::class,
        StringHelper::class,
    )

    // Component 接口
    val componentClazz: Set<KClass<out Component>> = setOf(
        DetailedComponent::class,
        MediaEventComponent::class,
        PlayComponent::class,
        SearchComponent::class,
        HomeComponent::class,
        PrefComponent::class,
    )

}