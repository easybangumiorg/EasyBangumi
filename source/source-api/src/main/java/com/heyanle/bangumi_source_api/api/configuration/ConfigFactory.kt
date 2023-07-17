package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/22 19:57.
 * https://github.com/heyanLE
 */
@Keep
lateinit var configFactory: ConfigFactory
@Keep
interface ConfigFactory {
    fun currentConfigString(field: ConfigField): String

}