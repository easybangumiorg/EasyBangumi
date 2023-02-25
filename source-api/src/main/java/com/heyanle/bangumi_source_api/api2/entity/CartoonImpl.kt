package com.heyanle.bangumi_source_api.api2.entity

/**
 * Created by HeYanLe on 2023/2/18 21:32.
 * https://github.com/heyanLE
 */
class CartoonImpl : Cartoon {

    override lateinit var id: String
    override lateinit var source: String
    override lateinit var url: String
    override lateinit var title: String
    override var genre: String? = null
    override var coverUrl: String? = null
    override var updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS
    override var isUpdate: Boolean = false
    override var status: Int = Cartoon.UNKNOWN

}