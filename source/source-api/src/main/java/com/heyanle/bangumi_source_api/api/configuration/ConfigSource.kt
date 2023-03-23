package com.heyanle.bangumi_source_api.api.configuration

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.Source

/**
 * Created by HeYanLe on 2023/2/22 21:20.
 * https://github.com/heyanLE
 */
@Keep
interface ConfigSource : Source {
    fun getConfigs(): List<ConfigField<*>>

}