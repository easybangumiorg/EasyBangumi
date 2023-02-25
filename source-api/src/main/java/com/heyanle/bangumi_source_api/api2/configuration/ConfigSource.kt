package com.heyanle.bangumi_source_api.api2.configuration

import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/22 21:20.
 * https://github.com/heyanLE
 */
interface ConfigSource : Source {
    fun getConfigs(): List<ConfigField<*>>

}