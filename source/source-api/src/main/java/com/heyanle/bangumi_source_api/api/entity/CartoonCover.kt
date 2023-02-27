package com.heyanle.bangumi_source_api.api.entity

import java.io.Serializable

/**
 * Created by HeYanLe on 2023/2/18 21:29.
 * https://github.com/heyanLE
 */
interface CartoonCover : Serializable {

    var id: String              // 标识，由源自己支持，用于区分番剧

    var source: String

    var url: String

    var title: String

    var coverUrl: String?

    var intro: String?

}