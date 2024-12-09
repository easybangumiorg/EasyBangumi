package com.heyanle.easy_bangumi_cm.plugin.api.component

import com.heyanle.easy_bangumi_cm.plugin.api.source.Source

/**
 * Created by heyanlin on 2024/12/9.
 */
open class ComponentWrapper : Component {

    var innerSource: Source? = null

    override val source: Source
        get() = innerSource!!

}