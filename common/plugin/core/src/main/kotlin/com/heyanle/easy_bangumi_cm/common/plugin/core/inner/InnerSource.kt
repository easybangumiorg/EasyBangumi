package com.heyanle.easy_bangumi_cm.common.plugin.core.inner

import com.heyanle.easy_bangumi_cm.plugin.api.component.Component
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.entity.SourceManifest
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2025/1/29.
 */
abstract class InnerSource: Source {

    abstract val componentClazz: List<KClass<Component>>

}