package com.heyanle.easybangumi4.cartoon

import com.heyanle.easybangumi4.cartoon.download.req.CartoonDownloadReqController
import com.heyanle.easybangumi4.cartoon.download.runtime.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalInfo
import com.heyanle.easybangumi4.cartoon.local.CartoonLocalController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/12.
 * https://github.com/heyanLE
 */
class CartoonLocalDownloadController(
    private val cartoonDownloadDispatcher: CartoonDownloadDispatcher,
    private val cartoonDownloadReqController: CartoonDownloadReqController,
    private val cartoonLocalController: CartoonLocalController,
) {

    private val scope = MainScope()

    private val _downloadInfo = MutableStateFlow<List<CartoonDownloadInfo>>(emptyList())
    val downloadInfo = _downloadInfo.asStateFlow()


    data class CartoonLocalInfoState(
        val loading: Boolean = true,
        val errorMsg: String? = null,
        val error: Throwable? = null,
        val localCartoonInfo: List<CartoonLocalInfo> = listOf()
    )

    private val _cartoonLocalInfo = MutableStateFlow<CartoonLocalInfoState>(CartoonLocalInfoState())
    val cartoonLocalInfo = _cartoonLocalInfo.asStateFlow()

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


        scope.launch {
            combine(
                _downloadInfo,
                cartoonLocalController.flowState
            ) { download, local ->
                if (local.loading) {
                    _cartoonLocalInfo.update {
                        it.copy(loading = true)
                    }
                } else if (local.errorMsg != null || local.error != null) {
                    _cartoonLocalInfo.update {
                        it.copy(
                            loading = false,
                            errorMsg = local.errorMsg,
                            error = local.error
                        )
                    }
                } else {
                    val d = local.localCartoonItem.values.map {
                        CartoonLocalInfo(
                            it,
                            download.filter { d -> d.req.toLocalItemId == it.itemId }
                        )
                    }
                    _cartoonLocalInfo.update {
                        it.copy(
                            loading = false,
                            errorMsg = null,
                            error = null,
                            localCartoonInfo = d
                        )
                    }
                }

            }.collect()

        }
    }


}