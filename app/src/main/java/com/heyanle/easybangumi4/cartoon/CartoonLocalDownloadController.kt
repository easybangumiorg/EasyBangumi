package com.heyanle.easybangumi4.cartoon

import com.heyanle.easybangumi4.cartoon.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/12.
 * https://github.com/heyanLE
 */
class CartoonLocalDownloadController (
    private val cartoonDownloadDispatcher: CartoonDownloadDispatcher,
    private val cartoonDownloadReqController: CartoonDownloadReqController,
) {

    private val scope = MainScope()

    private val _downloadInfo = MutableStateFlow<List<CartoonDownloadInfo>>(emptyList())

    init {
        scope.launch {
            combine(
                cartoonDownloadDispatcher.runtimeMap,
                cartoonDownloadReqController.downloadItem,
            ) { runtimeMap, reqList ->
                _downloadInfo.update {
                    reqList?.map {
                        CartoonDownloadInfo(
                            it,
                            runtimeMap[it.uuid]
                        )
                    } ?: emptyList()
                }
            }.collect()
        }
    }


}