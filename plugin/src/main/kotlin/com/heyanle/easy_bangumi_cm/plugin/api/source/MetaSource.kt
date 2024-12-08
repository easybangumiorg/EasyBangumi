package com.heyanle.easy_bangumi_cm.plugin.api.source


/**
 * Created by HeYanLe on 2024/12/8 21:31.
 * https://github.com/heyanLE
 */

interface MetaSource: Source {

    override val type: String
        get() = Source.TYPE_META

}