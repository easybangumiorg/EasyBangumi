package com.heyanle.easy_bangumi_cm.plugin.api.source


/**
 * Created by HeYanLe on 2024/12/8 21:27.
 * https://github.com/heyanLE
 */

interface MediaSource: Source {

    override val type: String
        get() = Source.TYPE_MEDIA


}