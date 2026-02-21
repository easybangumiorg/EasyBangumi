package org.easybangumi.next.shared.compose.media.bangumi

import androidx.lifecycle.viewModelScope
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.libplayer.api.C
import org.easybangumi.next.libplayer.api.PlayerBridge
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
import org.easybangumi.next.shared.foundation.snackbar.moeSnackBar
import org.easybangumi.next.shared.foundation.todo.easyTODO
import org.easybangumi.next.shared.foundation.view_model.BaseViewModel
import org.easybangumi.next.shared.source.SourceCase
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
    var suggestPlayerLineId: Int? = null
    var suggestEpisode: Int? = param.suggestEpisode
    var suggestPosition: Long? = null

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

    private val sourceCase: SourceCase by inject()

    private val cartoonInfoDao: CartoonInfoDao by inject()

    val bgmCollectInfoVM: BgmCollectInfoVM = bangumiDetailVM.bgmCollectInfoVM

    val bangumiCommentVM: BangumiCommentVM = bangumiDetailVM.bangumiCommentVM

    fun attachBridge(bridge: PlayerBridge<*>) {
        viewModelScope.launch {
            bridge.errorStateFlow.collectLatest {
                if (it != null) {
                    firePlayerError()
                }
            }
        }

        viewModelScope.launch {
            bridge.playStateFlow.collectLatest {
                if (it == C.State.READY) {
                    val pos = suggestPosition
                    if (pos != null) {
                        bridge.seekTo(pos)
                        suggestPosition = null
                    }
                }
            }
        }
    }


    init {
        // 媒体雷达结果 -> 播放线路状态
        viewModelScope.launch {
            state.map { it.radarResult }.distinctUntilChanged().collectLatest { res ->
                if (res != null) {
                    playLineIndexVM.loadPlayLine(res.playCover.toCartoonIndex(), suggestPlayerLineId ?: res.suggestPlayerLine?.order, suggestEpisode)
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
                    mediaFinderVM.changeKeyword(it.firstOrNull())
                }
            }
        }

//        val keyword = param.radarKeywordSuggest.firstOrNull()
//            ?: bangumiDetailVM.subjectRepository.flow.value.cacheData?.allName?.firstOrNull()
//            ?: param.cartoonCover?.name
//        if (keyword != null) {
//            // 尝试静默搜索一次
//            viewModelScope.launch {
//                if (silentFindFirst.compareAndSet(false, update = true)) {
//                    mediaFinderVM.silentFind(keyword)
//                }
//
//            }
//        }

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

        viewModelScope.launch {
            // 先用缓存的播放源，如果错误在启动一次静默搜索
            var needSilentFind = true
            if (param.useHistory) {
                val info = cartoonInfoDao.findById(cartoonIndex.source, cartoonIndex.id)
                if (info != null) {
                    suggestPlayerLineId = info.lastLineIndex
                    if (param.suggestEpisode == null) {
                        suggestEpisode = info.lastEpisodeOrder
                    }
                    if (param.suggestPosition == null) {
                        suggestPosition = info.lastProcessTime
                    }
                    val manifest = sourceCase.sourceManifestFlow().first()
                    manifest.firstOrNull {
                        it.key == info.lastPlaySourceKey
                    }?.let {
                        needSilentFind = false
                        mediaFinderVM.onUserResultSelect(
                            MediaFinderVM.SelectionResult(
                                // 这里没有缓存播放源的封面，先暂时使用元数据源的封面信息，直接修改 key 为播放源的 key，id 为播放源的 id
                                // 如果后续需要做播放源封面的展示需要注意
                                playCover = info.toCartoonCover().copy(
                                    source = it.key,
                                    id = info.lastPlaySourceId
                                ),
                                manifest = it,
                                suggestPlayerLine = null,
                                fromUser = false
                            )
                        )
                    }
                }
            }
            if (needSilentFind) {
                trySilentFind()
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


    // 手动选择之后 suggest 全失效
    fun onMediaRadarSelect(result: MediaFinderVM.SelectionResult?) {
        if (result?.fromUser == true) {
            suggestEpisode = null
            suggestPlayerLineId = null
        }
        sta.update {
            it.copy(
                radarResult = result
            )
        }
    }


    suspend fun trySaveHistory(positionMs: Long?) {
        val cover = bangumiDetailVM.subjectRepository.flow.value.okOrNull()?.cartoonCover
        val playLine = playLineIndexVM.logic.value
        cover ?: return
        cartoonInfoDao.update(cover.toCartoonIndex()) {
            val info = it ?: CartoonInfo.fromCartoonCover(cover)
            info.copy(
                lastHistoryTime = Clock.System.now().toEpochMilliseconds(),
                lastProcessTime = positionMs?: info.lastProcessTime,
                lastPlaySourceKey = playLine.business?.source?.key ?: info.lastPlaySourceKey,
                lastPlaySourceId = playLine.cartoonIndex?.id ?: info.lastPlaySourceId,
                lastLineId = playLine.playLineOrNull?.id ?: info.lastLineId,
                lastLineIndex = playLine.currentPlayerLine,
                lastEpisodeId = playLine.currentEpisodeOrNull?.id ?: info.lastEpisodeId,
                lastEpisodeOrder = playLine.currentEpisode,
                lastEpisodeIndex = playLine.currentEpisode,
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

    fun firePlayerError() {

        if (playIndexState.value.playInfo.mapOkData { it.isCache } == true) {
            // cache 数据出问题禁用 cache 后解析一下
            playLineIndexVM.tryRefreshPlayInfo(false)
        } else {
            //　否则触发一次静默搜索，trySilentFind　中有单次判断，可以饱和调用
            trySilentFind()
        }

    }

    fun trySilentFind() {
        val keyword = param.radarKeywordSuggest.firstOrNull()
            ?: bangumiDetailVM.subjectRepository.flow.value.cacheData?.allName?.firstOrNull()
            ?: param.cartoonCover?.name
        if (keyword != null) {
            // 尝试静默搜索一次
            viewModelScope.launch {
                if (silentFindFirst.compareAndSet(false, update = true)) {
                    mediaFinderVM.onUserResultSelect(null)
                    mediaFinderVM.silentFind(keyword)
                }

            }
        }
    }
}
