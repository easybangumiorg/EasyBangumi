package com.heyanle.easybangumi4.cartoon.repository

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.toDataResult
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.source_api.entity.Cartoon
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by HeYanLe on 2023/8/13 16:35.
 * https://github.com/heyanLE
 */
class CartoonNetworkDataSource(
    private val sourceStateCase: SourceStateCase,
) {

    suspend fun awaitCartoonWithPlayLines(
        id: String,
        source: String,
        url: String
    ): DataResult<Pair<Cartoon, List<PlayLine>>> {
        val result = sourceStateCase.awaitBundle().detailed(source)
            ?.getAll(CartoonSummary(id, source, url))
            ?: return DataResult.error("没有番剧源")
        return result.toDataResult()
    }

    suspend fun awaitPlayLines(id: String, source: String, url: String): DataResult<List<PlayLine>> {
        val result = sourceStateCase.awaitBundle().detailed(source)
            ?.getPlayLine(CartoonSummary(id, source, url))
            ?: return DataResult.error("没有番剧源")
        return  result.toDataResult()
    }

}