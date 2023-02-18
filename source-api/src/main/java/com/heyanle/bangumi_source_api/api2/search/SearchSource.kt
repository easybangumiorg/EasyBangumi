package com.heyanle.bangumi_source_api.api2.search

import com.heyanle.bangumi_source_api.api2.Source
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover
import com.heyanle.bangumi_source_api.api2.home.HomePage

/**
 * Created by HeYanLe on 2023/2/18 21:39.
 * https://github.com/heyanLE
 */
interface SearchSource: Source {

    val firstKey: Int

    suspend fun search(
        keyword: String,
        pageKey: Int
    ): Source.SourceResult<Pair<Int?, List<CartoonCover>>>

}