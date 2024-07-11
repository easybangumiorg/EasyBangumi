package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon_download.CartoonDownloadDispatcher
import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon_download.entity.CartoonDownloadReqFactory
import com.heyanle.easybangumi4.cartoon_local.CartoonLocalController
import com.heyanle.easybangumi4.cartoon_local.entity.CartoonLocalItem
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.utils.CoroutineProvider
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * B 请选择本地番源中的目标剧集 -> C
 * C 请设定各视频对应刮削数据 设定各个视频对应的目标剧集中的目标集数和标题，这里目标集数不允许重复（全局加锁统一控制）
 * Created by heyanle on 2024/7/8.
 * https://github.com/heyanLE
 */
class CartoonDownloadReqModel(
    private val cartoonInfo: CartoonInfo,
    private val playerLineWrapper: PlayLineWrapper,
    private val episodes: List<Episode>,
) {

    private val scope = MainScope()
    data class State(
        val loading: Boolean = true,
        val cartoonLocalItem: CartoonLocalItem? = null,

        val episodeSet: Set<Int> = emptySet(),
        val keyword: String = "",
        val allLocalItem: List<CartoonLocalItem> = listOf(),
        val downloadReqList: List<CartoonDownloadReq> = listOf()
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val cartoonLocalController: CartoonLocalController by Inject.injectLazy()
    private val cartoonDownloadDispatcher: CartoonDownloadDispatcher by Inject.injectLazy()

    val canReq = state.map {
        !it.loading &&
                (it.cartoonLocalItem != null) &&
                it.allLocalItem.any { e -> e.itemId == it.cartoonLocalItem.itemId } &&
                it.downloadReqList.none {e -> it.episodeSet.contains(e.toEpisode) }
    }.stateIn(scope, SharingStarted.Lazily, false)

    init {
        scope.launch(CoroutineProvider.SINGLE) {
            combine(
                cartoonLocalController.flowState,
                state.map { it.cartoonLocalItem }.distinctUntilChanged(),
                state.map { it.keyword }.distinctUntilChanged(),
            ) { localState, currentItem, keyword ->
                if (localState.loading) {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                    return@combine
                } else {
                    _state.update {  old ->
                        if (localState.localCartoonItem.any { it.key == old.cartoonLocalItem?.itemId }){
                            old.copy(
                                loading = false,
                                allLocalItem = localState.localCartoonItem.values.filter { it.matches(keyword) },
                                episodeSet = cartoonLocalController.getLocalEpisodes(old.cartoonLocalItem?.itemId ?: ""),
                                downloadReqList = CartoonDownloadReqFactory.newReqList(
                                    cartoonInfo,
                                    playerLineWrapper.playLine,
                                    episodes,
                                    old.cartoonLocalItem?.itemId ?: ""
                                )
                            )
                        } else {
                            old.copy(
                                loading = false,
                                cartoonLocalItem = null,
                                allLocalItem = localState.localCartoonItem.values.filter { it.matches(keyword) },
                                episodeSet = emptySet()
                            )
                        }
                    }
                }
            }.collect()
        }
    }

    fun createNewLocal(label: String) {
        cartoonLocalController.newLocal(cartoonInfo, label)
    }

    fun setLocalItem(localItem: CartoonLocalItem) {
        _state.update {
            it.copy(
                cartoonLocalItem = localItem,
            )
        }
    }

    fun changeEpisode( req: CartoonDownloadReq, episode: Int) {
        _state.update {
            it.copy(
                downloadReqList = it.downloadReqList.map {
                    if (it.uuid == req.uuid) {
                        it.copy(toEpisode = episode)
                    } else {
                        it
                    }
                }
            )

        }
    }

    fun changeLabel(req: CartoonDownloadReq, label: String) {
        _state.update {
            it.copy(
                downloadReqList = it.downloadReqList.map {
                    if (it.uuid == req.uuid) {
                        it.copy(toEpisodeTitle = label)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun req() {
        if (canReq.value) {
            _state.value.downloadReqList.forEach {
                cartoonDownloadDispatcher.addTask(it)
            }
        }
    }

    fun changeKeyword(keyword: String) {
        _state.update {
            it.copy(
                keyword = keyword
            )
        }
    }


}