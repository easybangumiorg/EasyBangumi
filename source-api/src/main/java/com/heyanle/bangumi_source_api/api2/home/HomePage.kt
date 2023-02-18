package com.heyanle.bangumi_source_api.api2.home

import com.heyanle.bangumi_source_api.api2.Source

/**
 * Created by HeYanLe on 2023/2/18 21:40.
 * https://github.com/heyanLE
 */
class HomePage (
    var key: String,
    var label: String,
    var sourceKey: String,
    var firstKey: Int,
    var order: Int,
)

fun Source.newHomePage(
    pageKey: String,
    label: String,
    firstKey: Int = 0,
    order: Int = 0,
): HomePage {
    return HomePage(pageKey, label, key, firstKey, order)
}