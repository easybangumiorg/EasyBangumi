package com.heyanle.bangumi_source_api.api.component.search

import androidx.annotation.Keep
import com.heyanle.bangumi_source_api.api.SourceResult
import com.heyanle.bangumi_source_api.api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/27 21:42.
 * https://github.com/heyanLE
 */
@Keep
interface SearchComponent {

    /**
     * 获取首页页码
     * @param keyword 关键字
     * @return 页码
     */
    fun getFirstSearchKey(keyword: String): Int

    /**
     * 搜索番剧
     * @param pageKey 页码
     * @param keyword 关键字
     * @return 下一页页码（没有下一页则为 null）， 番剧列表
     */
    suspend fun search(pageKey: Int, keyword: String): SourceResult<Pair<Int?, List<CartoonCover>>>

}