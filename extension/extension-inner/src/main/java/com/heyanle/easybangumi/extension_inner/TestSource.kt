package com.heyanle.easybangumi.extension_inner

import com.heyanle.bangumi_source_api.api2.IconSource
import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.extension_api.ExtensionIconSource

/**
 * Created by HeYanLe on 2023/2/23 0:14.
 * https://github.com/heyanLE
 */
class TestSource: Source, ExtensionIconSource {
    override fun getIconResourcesId(): Int? {
        return R.mipmap.app_logo
    }

    override val key: String
        get() = "test"
    override val label: String
        get() = "测试"
    override val version: String
        get() = "测试 1.0"
    override val versionCode: Int
        get() = 0
}