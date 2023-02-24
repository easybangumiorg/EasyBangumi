package com.heyanle.easybangumi.extension_inner.cycplus

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.easybangumi.extension_inner.R
import com.heyanle.extension_api.ExtensionIconSource

/**
 * Created by HeYanLe on 2023/2/19 23:14.
 * https://github.com/heyanLE
 */
class CycplusSource: Source, ExtensionIconSource {

    override val key: String
        get() = "cycplus"
    override val label: String
        get() = "次元城+"
    override val version: String
        get() = "1.0"
    override val versionCode: Int
        get() = 0

    override fun getIconResourcesId(): Int? {
        return R.drawable.cycplus
    }

    init {

    }
}