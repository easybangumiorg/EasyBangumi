package com.heyanle.bangumi_source_api.api2.configuration

/**
 * Created by HeYanLe on 2023/2/20 16:32.
 * https://github.com/heyanLE
 */
interface ConfigEditor <T> {
    var field: ConfigurationField<T>
}