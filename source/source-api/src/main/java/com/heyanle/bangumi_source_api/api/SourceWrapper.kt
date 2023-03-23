package com.heyanle.bangumi_source_api.api

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/27 22:44.
 * https://github.com/heyanLE
 */
@Keep
open class SourceWrapper(
    val source: Source
): Source {
    override val key: String
        get() = source.key
    override val label: String
        get() = source.label
    override val version: String
        get() = source.version
    override val versionCode: Int
        get() = source.versionCode
    override val describe: String?
        get() = source.describe
}