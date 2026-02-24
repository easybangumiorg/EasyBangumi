package com.heyanle.easybangumi4.source_api.entity

import androidx.annotation.Keep

/**
 * Created by HeYanLe on 2023/2/18 21:34.
 * https://github.com/heyanLE
 */
@Keep
class CartoonCoverImpl(
    override var id: String,
    override var source: String,
    override var url: String,
    override var title: String,
    override var intro: String? = null,
    override var coverUrl: String? = null,
) : CartoonCover {


}