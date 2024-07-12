package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.CartoonLocalDownloadController
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by heyanle on 2024/7/8.
 * https://github.com/heyanLE
 */
class CartoonDownloadReqModel(
    private val cartoonInfo: CartoonInfo,
    private val playerLineWrapper: PlayLineWrapper,
    private val episodes: List<Episode>,
) {


    private val scope = MainScope()
    private val cartoonLocalDownloadController: CartoonLocalDownloadController by Inject.injectLazy()
    data class State(
        val localState: CartoonLocalDownloadController.CartoonLocalInfoState = CartoonLocalDownloadController.CartoonLocalInfoState(),

        val keyword: String? = null,

        val targetLocalItem: CartoonLocalItem? = null,
        val reqList: List<CartoonDownloadReq> = emptyList(),

        val dialog: Dialog? = null,
    )
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            cartoonLocalDownloadController.cartoonLocalInfo.collect { sta ->
                _state.update {
                    it.copy(
                        localState = sta
                    )
                }
            }

        }
    }

    sealed class Dialog {
        data class NewLocalReq(
            val localMsg: CartoonLocalMsg,
        ): Dialog()
    }



}