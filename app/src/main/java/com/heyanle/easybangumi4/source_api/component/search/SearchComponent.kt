package com.heyanle.easybangumi4.source_api.component.search


import androidx.annotation.Keep
import com.heyanle.easybangumi4.plugin.source.utils.network.web.IWebProxy
import com.heyanle.easybangumi4.source_api.SourceResult
import com.heyanle.easybangumi4.source_api.component.Component
import com.heyanle.easybangumi4.source_api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/10/18 23:46.
 * https://github.com/heyanLE
 */
@Keep
interface SearchComponent: Component {

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

    suspend fun searchWithCheck(pageKey: Int, keyword: String, iWebProxy: IWebProxy): SourceResult<Pair<Int?, List<CartoonCover>>> {
        return SourceResult.Error(IllegalStateException("Not yet implemented"))
    }
}