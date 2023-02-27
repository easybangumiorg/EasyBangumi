package com.heyanle.bangumi_source_api.api.search

import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/27 21:42.
 * https://github.com/heyanLE
 */
interface SearchSource: Source {

    fun getFirstSearchKey(keyword: String): Int

    fun search(key: Int, keyword: String): SourceResult<Pair<Int?, CartoonCover>>

}