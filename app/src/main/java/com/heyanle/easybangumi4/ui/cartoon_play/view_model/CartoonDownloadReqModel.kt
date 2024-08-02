package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.base.map
import com.heyanle.easybangumi4.cartoon.story.download_v1.req.CartoonDownloadReqFactory
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private val cartoonStoryController: CartoonStoryController by Inject.injectLazy()


    data class State(
        val storyList: DataResult<List<CartoonStoryItem>> = DataResult.Loading(),
        val pinId: String? = null,

        val keyword: String? = null,
        val localWithKeyword: List<CartoonStoryItem> = listOf(),

        val targetLocalInfo: CartoonStoryItem? = null,
        val reqList: List<CartoonDownloadReq> = emptyList(),

        val dialog: Dialog? = null,
    ) {

        // 该集合可能会缺少调用后才进入下载错误阶段的任务
        val cantEpisodeSet: Set<Int> by lazy {
            targetLocalInfo?.cantReqEpisode?.plus(
                targetLocalInfo.errorDownloadEpisode ?: emptySet()) ?: emptySet()
        }

        val reqEpisode: Set<Int> by lazy {
            reqList.map { it.toEpisode }.toSet()
        }


        val isRepeat: Boolean by lazy {
            reqEpisode.size != reqList.size || reqEpisode.any { cantEpisodeSet.contains(it) }
        }
    }

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        scope.launch {
            combine(
                cartoonStoryController.storyItemList,
                _state.map { it.pinId }.distinctUntilChanged()
            ) {result, pinId ->
                result to pinId
            }.collectLatest { (result, pinId) ->
                _state.update {
                    it.copy(
                        storyList =  result.map {
                            val newList = arrayListOf<CartoonStoryItem>()
                            it.firstOrNull { it.cartoonLocalItem.itemId == pinId }?.let {
                                newList.add(it)
                            }
                            newList.addAll(it.filter { it.cartoonLocalItem.itemId != pinId })
                            newList
                        }
                    )
                }
            }

            cartoonStoryController.storyItemList.collectLatest { result ->
                _state.update {
                    it.copy(
                        storyList = result
                    )
                }
            }
        }
        scope.launch {
            combine(
                _state.map { it.storyList }.distinctUntilChanged(),
                _state.map { it.keyword }.distinctUntilChanged(),
            ) { local, keyword ->
                if (keyword == null) {
                    local.okOrNull() ?: emptyList()
                } else {
                    local.okOrNull()?.filter {
                        it.cartoonLocalItem.matches(keyword)
                    } ?: emptyList()
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

        data class NewLocalReqWithTitle(
            val localMsg: CartoonLocalMsg,
        ) : Dialog()

        data class NewLocalReq(
            val localMsg: CartoonLocalMsg,
        ) : Dialog()

        data object LoadingNewLocal : Dialog()

        data class ChangeEpisode(
            val uuid: String,
            val title: String,
            val episode: Int
        ) : Dialog()
    }


    fun retry() {
        cartoonStoryController.refreshLocal()
    }

    fun changeKeyword(keyword: String?) {
        _state.update {
            it.copy(
                keyword = keyword
            )
        }
    }

    fun targetLocalItem(localItem: CartoonStoryItem?) {
        _state.update {
            it.copy(
                targetLocalInfo = localItem,
                reqList = localItem?.let {
                    CartoonDownloadReqFactory.newReqList(
                        cartoonInfo,
                        playerLineWrapper.playLine,
                        episodes,
                        it
                    )
                } ?: emptyList()
            )
        }
    }

    fun showNewLocalDialog() {
        _state.update {
            it.copy(
                dialog = Dialog.NewLocalReq(CartoonLocalMsg.fromCartoonInfo(cartoonInfo)),
            )
        }
    }

    fun showNewLocalDialogWithTitle() {
        _state.update {
            it.copy(
                dialog = Dialog.NewLocalReqWithTitle(CartoonLocalMsg.fromCartoonInfo(cartoonInfo))
            )
        }
    }


    fun showChangeEpisode(uuid: String, title: String, episode: Int) {
        _state.update {
            it.copy(
                dialog = Dialog.ChangeEpisode(uuid, title, episode)
            )
        }
    }

    fun changeReq(uuid: String, title: String, episode: Int) {
        _state.update { sta ->
            val old = sta.reqList.firstOrNull() { it.uuid == uuid }
            if (old == null) {
                sta
            } else {
                var current = episode
                sta.copy(
                    reqList = sta.reqList.map {
                        if (it.uuid == uuid) {
                            it.copy(
                                toEpisodeTitle = title,
                                toEpisode = episode
                            )
                        } else if (it.toEpisode < episode) {
                            it
                        } else {
                            current++
                            current = maxOf(it.toEpisode, current)
                            while (sta.cantEpisodeSet.contains(current)) {
                                current++
                            }
                            it.copy(
                                toEpisode = current,
                            )

                        }
                    }
                )
            }
        }
    }

    fun pushReq(state: State) {
        scope.launch {
            cartoonStoryController.newDownloadReq(
                state.reqList
            )
        }
    }


    fun addLocalCartoon(localMsg: CartoonLocalMsg, refresh: Boolean = false) {
        scope.launch {
            _state.update {
                it.copy(
                    dialog = Dialog.LoadingNewLocal
                )
            }
            val newId = cartoonStoryController.newStory(localMsg)
            _state.update {
                it.copy(
                    dialog = null,
                    pinId = newId
                )
            }
        }
    }

    fun dismissDialog() {
        _state.update {
            it.copy(
                dialog = null
            )
        }
    }

}