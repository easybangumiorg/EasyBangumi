package com.heyanle.bangumi_source_api.api.entity

/**
 * Created by HeYanLe on 2023/2/18 21:34.
 * https://github.com/heyanLE
 */
class CartoonCoverImpl : CartoonCover {

    override lateinit var id: String
    override lateinit var source: String
    override lateinit var url: String
    override lateinit var title: String
    override var intro: String? = null
    override var coverUrl: String? = null
}