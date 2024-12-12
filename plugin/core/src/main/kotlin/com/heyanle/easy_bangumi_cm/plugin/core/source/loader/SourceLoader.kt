package com.heyanle.easy_bangumi_cm.plugin.core.source.loader

import com.heyanle.easy_bangumi_cm.plugin.api.ExtensionInfo
import com.heyanle.easy_bangumi_cm.plugin.api.component.ComponentBundle
import com.heyanle.easy_bangumi_cm.plugin.api.source.Source
import com.heyanle.easy_bangumi_cm.plugin.core.source.entity.SourceInfo

/**
 * Created by heyanlin on 2024/12/11.
 */
interface SourceLoader<T: Source> {

    fun clazz(): Class<T>

    fun canLoad(source: T): Boolean {
        return clazz().isInstance(source)
    }

    fun load(source: Source): List<SourceInfo>

}