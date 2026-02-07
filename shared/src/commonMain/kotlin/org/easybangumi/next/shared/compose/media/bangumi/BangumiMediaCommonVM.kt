package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.cartoon.collection.BgmCollectInfoVM
import org.easybangumi.next.shared.compose.bangumi.comment.BangumiCommentVM
import org.easybangumi.next.shared.compose.detail.bangumi.BangumiDetailVM
import org.easybangumi.next.shared.compose.media.MediaParam
import org.easybangumi.next.shared.compose.media.PlayLineIndexVM
import org.easybangumi.next.shared.compose.media_finder.MediaFinderVM
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.room.cartoon.dao.CartoonInfoDao
import org.easybangumi.next.shared.foundation.todo.easyTODO
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.koin.core.component.inject
import kotlin.getValue


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

class BangumiMediaCommonVM (
    val param: MediaParam,
): BaseViewModel() {

    val cartoonIndex: CartoonIndex = param.cartoonIndex
    var suggestEpisode: Int? = param.suggestEpisode

    // == 数据状态 =============================
    data class State(
        val radarResult: MediaFinderVM.SelectionResult? = null,
        val showDetailFromPlay: Boolean = true,
        val isFullscreen: Boolean = false,
        val isTableMode: Boolean = false,
        // 静默搜索状态
        val silentFindingState: MediaFinderVM.State? = null,


        val hasBgmAccountInfo: Boolean = false,
        val collectionState: DataState<BgmCollectResp> = DataState.none(),

        val cartoonInfo: CartoonInfo? = null,
    )

    internal val sta = MutableStateFlow(State())
    val state = sta.asStateFlow()

    // == 弹窗状态 =============================
    sealed class Popup {


        data object BangumiDetailPanel: Popup()
        class CollectionDialog(
            val cartoonCover: CartoonCover,
        ): Popup()
    }
    private val _popupState = MutableStateFlow<Popup?>(null)
    val popupState = _popupState.asStateFlow()

    // == 播放线路状态 =============================
    val playLineIndexVM: PlayLineIndexVM by childViewModel {
        PlayLineIndexVM(
//            cartoonIndex = cartoonIndex.toCartoonIndex(),
            suggestEpisode = suggestEpisode,
        )
    }
    val playIndexState = playLineIndexVM.logic


    // == 视频雷达状态 =============================
    val mediaFinderVM: MediaFinderVM by childViewModel {
        MediaFinderVM(
            param.radarKeywordSuggest.ifEmpty { listOf(param.cartoonCover?.name?:"")  }
        )
    }
    private val silentFindFirst = atomic(false)

    // == Bangumi 番剧详情状态 =============================
    val bangumiDetailVM: BangumiDetailVM by childViewModel {
        BangumiDetailVM(cartoonIndex = cartoonIndex,)
    }


    private val cartoonInfoDao: CartoonInfoDao by inject()

    val bgmCollectInfoVM: BgmCollectInfoVM = bangumiDetailVM.bgmCollectInfoVM

    val bangumiCommentVM: BangumiCommentVM = bangumiDetailVM.bangumiCommentVM


    init {
        // 媒体雷达结果 -> 播放线路状态
        viewModelScope.launch {
            state.map { it.radarResult }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    playLineIndexVM.loadPlayLine(it.playCover.toCartoonIndex())
                }
            }
        }

        // 这里只是为了展示番剧 Preview ，用最弱的刷新方式
        bangumiDetailVM.subjectRepository.refreshIfNone()

        // Bangumi 详情别名 -> 媒体雷达搜索建议
        viewModelScope.launch {
            bangumiDetailVM.subjectRepository.flow.collectLatest {
                it.cacheData?.allName?.let {
                    mediaFinderVM.changeKeywordSuggest(it)

                    it.firstOrNull()?.let {
                        // 尝试静默搜索一次
                        viewModelScope.launch {
                            if (silentFindFirst.compareAndSet(expect = false, update = true)) {
                                mediaFinderVM.silentFind(it)
                            }

                        }
                    }

                }
            }
        }

        val keyword = param.radarKeywordSuggest.firstOrNull()
            ?: bangumiDetailVM.subjectRepository.flow.value.cacheData?.allName?.firstOrNull()
            ?: param.cartoonCover?.name
        if (keyword != null) {
            // 尝试静默搜索一次
            viewModelScope.launch {
                if (silentFindFirst.compareAndSet(false, update = true)) {
                    mediaFinderVM.silentFind(keyword)
                }

            }
        }

        // 媒体雷达选择结果处理
        viewModelScope.launch {
            mediaFinderVM.logic.map { it.result }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    onMediaRadarSelect(it)
                }
            }
        }

        viewModelScope.launch {
            mediaFinderVM.logic.collectLatest { state ->
                sta.update {
                    it.copy(
                        silentFindingState = state
                    )
                }
            }
        }

        viewModelScope.launch {
            bgmCollectInfoVM.logic.collectLatest { state ->
                sta.update {
                    it.copy(
                        collectionState = state.collectionState,
                        hasBgmAccountInfo = state.hasBgmAccountInfo,
                        cartoonInfo = state.cartoonInfo,
                    )
                }
            }
        }
    }

    // state change ============================


    // popup =============================

    fun showMediaRadar() {
        mediaFinderVM.showPanel()
    }

    fun showBangumiDetailPanel() {
        easyTODO("bottomSheet")
        _popupState.update { Popup.BangumiDetailPanel }
    }
    fun dismissPopup() {
        _popupState.update { null }
    }


    fun onMediaRadarSelect(result: MediaFinderVM.SelectionResult?) {
        sta.update {
            it.copy(
                radarResult = result
            )
        }
    }


    suspend fun trySaveHistory(positionMs: Long) {
        val cover = bangumiDetailVM.subjectRepository.flow.value.okOrNull()?.cartoonCover
        val playLine = playLineIndexVM.logic.value
        cover ?: return
        cartoonInfoDao.update(cover.toCartoonIndex()) {
            val info = it ?: CartoonInfo.fromCartoonCover(cover)
            info.copy(
                lastHistoryTime = Clock.System.now().toEpochMilliseconds(),
                lastProcessTime = positionMs,
                lastPlaySourceKey = playLine.business?.source?.key ?: info.lastPlaySourceKey,
                lastPlaySourceId = playLine.cartoonIndex?.id ?: info.lastPlaySourceId,
                lastLineId = playLine.playLineOrNull?.id ?: info.lastLineId,
                lastEpisodeId = playLine.currentEpisodeOrNull?.id ?: info.lastEpisodeId,
                lastEpisodeLabel = playLine.currentEpisodeOrNull?.label ?: info.lastEpisodeLabel,
            )
        }
    }

    fun onCollectDialogShow() {
        bangumiDetailVM.subjectRepository.flow.value.okOrNull()?.cartoonCover?.let { cover ->
            _popupState.update {
                Popup.CollectionDialog(cover)
            }
        }
    }
}