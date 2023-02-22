package com.heyanle.easybangumi4.source

import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/22 20:41.
 * https://github.com/heyanLE
 */
class SourceBundle(
    list: List<Source>
) {

    private val sourceMap = linkedMapOf<String, Source>()

}