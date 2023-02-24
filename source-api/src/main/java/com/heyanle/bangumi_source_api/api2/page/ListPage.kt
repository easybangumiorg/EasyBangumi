package com.heyanle.bangumi_source_api.api2.page

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/20 16:03.
 * https://github.com/heyanLE
 */
interface ListPage: CartoonPage {

    val firstKey: Int

    suspend fun getCartoons(
        pageKey: Int
    ): Source.SourceResult<Pair<Int?, List<CartoonCover>>>

}