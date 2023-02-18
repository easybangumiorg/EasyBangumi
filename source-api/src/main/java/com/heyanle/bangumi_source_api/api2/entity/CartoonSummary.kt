package com.heyanle.bangumi_source_api.api2.entity

import java.io.Serializable

/**
 * Created by HeYanLe on 2023/2/18 21:30.
 * https://github.com/heyanLE
 */
class CartoonSummary : Serializable {

    lateinit var id: String              // 标识，由源自己支持，用于区分番剧

    lateinit var source: String

    lateinit var url: String

}