package com.heyanle.easybangumi4.source_api.component

import com.heyanle.easybangumi4.source_api.Source


/**
 * Created by HeYanLe on 2023/10/29 21:44.
 * https://github.com/heyanLE
 */
open class ComponentWrapper() : Component {

    lateinit var innerSource: Source

    override val source: Source
        get() = innerSource
}
