package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.CartoonLocalDownloadController
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.local.CartoonLocalController
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    private val cartoonLocalController: CartoonLocalController by Inject.injectLazy()
    private val cartoonLocalDownloadController: CartoonLocalDownloadController by Inject.injectLazy()

    data class State(
        val localState: CartoonLocalDownloadController.CartoonLocalInfoState = CartoonLocalDownloadController.CartoonLocalInfoState(),

        val keyword: String? = null,
        val localWithKeyword: List<CartoonLocalInfo> = listOf(),

        val targetLocalInfo: CartoonLocalInfo? = null,
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
        scope.launch {
            combine(
                _state.map { it.localState.localCartoonInfo }.distinctUntilChanged(),
                _state.map { it.keyword }.distinctUntilChanged(),
            ) { local, keyword ->
                if (keyword == null) {
                    local
                } else {
                    local.filter {
                        it.cartoonLocalItem.matches(keyword)
                    }
                }
            }.collect { li ->
                _state.update {
                    it.copy(
                        localWithKeyword = li
                    )
                }

            }
        }
    }

    sealed class Dialog {
        data class NewLocalReq(
            val localMsg: CartoonLocalMsg,
        ) : Dialog()

        data object LoadingNewLocal : Dialog()
    }


    fun retry(){
        cartoonLocalController.refresh()
    }
    fun changeKeyword(keyword: String?) {
        _state.update {
            it.copy(
                keyword = keyword
            )
        }
    }

    fun targetLocalItem(localItem: CartoonLocalInfo?) {
        _state.update {
            it.copy(
                targetLocalInfo = localItem
            )
        }
    }

    fun showNewLocalDialog(){
        _state.update {
            it.copy(
                dialog = Dialog.NewLocalReq(CartoonLocalMsg.fromCartoonInfo(cartoonInfo))
            )
        }
    }


    fun addLocalCartoon(localMsg: CartoonLocalMsg){
        scope.launch {
            _state.update {
                it.copy(
                    dialog = Dialog.LoadingNewLocal
                )
            }
            cartoonLocalController.newLocal(localMsg) {
                _state.update {
                    it.copy(
                        dialog = null
                    )
                }
            }
        }
    }

    fun dismissDialog(){
        _state.update {
            it.copy(
                dialog = null
            )
        }
    }

}