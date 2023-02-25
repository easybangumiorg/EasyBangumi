package com.heyanle.bangumi_source_api.api2.component

import com.heyanle.bangumi_source_api.api2.Source


/**
 * Created by HeYanLe on 2023/2/25 14:30.
 * https://github.com/heyanLE
 */
interface Component {

    val source: Source

}

class ComponentBuilderScope(
    val source: Source,
) {
    val components: ArrayList<Component> = arrayListOf()

}