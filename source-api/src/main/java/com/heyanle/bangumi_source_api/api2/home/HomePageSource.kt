package com.heyanle.bangumi_source_api.api2.home

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/18 21:39.
 * https://github.com/heyanLE
 */
interface HomePageSource : Source {

    suspend fun homes(): List<HomePage>

    suspend fun getCartoon(
        homePage: HomePage,
        pageKey: Int
    ): Source.SourceResult<Pair<Int?, List<CartoonCover>>>

}