package com.heyanle.bangumi_source_api.api.entity

/**
 * Created by HeYanLe on 2023/2/18 21:32.
 * https://github.com/heyanLE
 */
class CartoonImpl(
    override var id: String,
    override var source: String,
    override var url: String,
    override var title: String,
    override var genre: String? = null,
    override var coverUrl: String? = null,

    override var intro: String? = null,
    override var description: String? = null,

    override var updateStrategy: Int = Cartoon.UPDATE_STRATEGY_ALWAYS,
    override var isUpdate: Boolean = false,
    override var status: Int = Cartoon.STATUS_UNKNOWN,
) : Cartoon {


}