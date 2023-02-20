package com.heyanle.bangumi_source_api.api2.configuration

/**
 * Created by HeYanLe on 2023/2/20 16:27.
 * https://github.com/heyanLE
 */
interface ConfigurationField <T> {
    val sourceKey: String
    val value: T
    val defValue: T
}