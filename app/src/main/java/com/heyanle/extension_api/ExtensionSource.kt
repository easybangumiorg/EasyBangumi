package com.heyanle.extension_api

import com.heyanle.easybangumi4.source_api.Source


/**
 * Created by HeYanLe on 2023/3/1 18:30.
 * https://github.com/heyanLE
 */
abstract class ExtensionSource: Source {
    override val key: String
        get() = "$packageName-$sourceKey"


    abstract val sourceKey: String

    /**
     * 加载的时候本体赋值
     */
    var packageName: String = ""
}