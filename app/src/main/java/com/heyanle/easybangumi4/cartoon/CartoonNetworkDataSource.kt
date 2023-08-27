package com.heyanle.easybangumi4.cartoon

import com.heyanle.bangumi_source_api.api.entity.Cartoon
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.toDataResult
import com.heyanle.easybangumi4.source.SourceController

/**
 * Created by HeYanLe on 2023/8/13 16:35.
 * https://github.com/heyanLE
 */
class CartoonNetworkDataSource(
    private val sourceController: SourceController
) {

    suspend fun getCartoonWithPlayLines(
        id: String,
        source: String,
        url: String
    ): DataResult<Pair<Cartoon, List<PlayLine>>> {
        val result = sourceController.bundleIfEmpty().detailed(source)
            ?.getAll(CartoonSummary(id, source, url))
            ?: return DataResult.error("没有番剧源")
        return result.toDataResult()
    }

    suspend fun getPlayLines(id: String, source: String, url: String): DataResult<List<PlayLine>> {
        val result = sourceController.bundleIfEmpty().detailed(source)
            ?.getPlayLine(CartoonSummary(id, source, url))
            ?: return DataResult.error("没有番剧源")
        return  result.toDataResult()
    }

}