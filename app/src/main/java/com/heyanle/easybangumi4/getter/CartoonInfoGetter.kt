package com.heyanle.easybangumi4.getter

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.source_api.entity.PlayLine

/**
 * Created by heyanlin on 2023/10/2.
 */
class CartoonInfoGetter(
    private val cartoonRepository: CartoonRepository
) {

    suspend fun awaitCartoonInfoWithPlayLines(
        id: String,
        source: String,
        url: String,
        time: Long = System.currentTimeMillis(),
    ): DataResult<Pair<CartoonInfo, List<PlayLine>>> {
        return cartoonRepository.awaitCartoonInfoWithPlayLines(id, source, url, time)
    }

}