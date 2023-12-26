package com.heyanle.easybangumi4.getter

import com.heyanle.bangumi_source_api.api.entity.PlayLine
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.CartoonRepository

/**
 * Created by heyanlin on 2023/10/2.
 */
class CartoonInfoGetter(
    private val cartoonRepository: CartoonRepository
) {

    suspend fun awaitCartoonInfoWithPlayLines(
        id: String,
        source: String,
        url: String
    ): DataResult<Pair<CartoonInfo, List<PlayLine>>> {
        return cartoonRepository.awaitCartoonInfoWithPlayLines(id, source, url)
    }

}