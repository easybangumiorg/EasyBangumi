package org.easybangumi.next.shared.compose.media_finder_old

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media_finder_old.radar.MediaRadarVM
import org.easybangumi.next.shared.compose.media_finder_old.search.MediaSearchVM
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.FinderComponentPair
import org.easybangumi.next.shared.source.api.source.SourceManifest

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
class MediaFinderVM(
    private var keywordSuggest: List<String>,
): StateViewModel<MediaFinderVM.FinderState>(FinderState(), true) {

    val radarVM by childViewModel {
        MediaRadarVM(keywordSuggest)
    }

    val searchVM by childViewModel {
        MediaSearchVM(keywordSuggest)
    }

    val labelList = listOf(
        "聚合搜索",
        "手动搜索",
    )

    val pagerState = PagerState { 2 }

    data class SelectionResult(
        val playCover: CartoonCover,
        val businessPair: FinderComponentPair,
    ) {
        val searchBusiness = businessPair.first
        val playBusiness = businessPair.second
    }

    // 卡片状态
    data class FinderState (
        val finderResult: SelectionResult? = null,

        // 静默搜索中
        val silentFinding: Boolean = false,
        val silentNotResult: Boolean = false,
        val findingState: FindingState? = null,

        val showPanel: Boolean = false,
    )

    data class FindingState(
        val showingSource: SourceManifest? = null,
        val allSourceCount: Int = 0,
        val resultSourceCount: Int = 0,
    )

    init {
        viewModelScope.launch {
            combine(
                state.map { it.silentFinding }.distinctUntilChanged(),
                radarVM.logic
            ){ silentFinding, logic ->
                if (silentFinding && logic.sourceTabList.isNotEmpty()) {
                    var showingSource: SourceManifest? = null
                    var allSourceCount = 0
                    var resultSourceCount = 0
                    logic.sourceTabList.forEach { state ->
                        if (state.loading && showingSource == null) {
                            showingSource = state.sourceManifest
                        }
                        allSourceCount += 1
                        if (!state.loading){
                            resultSourceCount += 1
                        }

                    }
                    if (allSourceCount == resultSourceCount) {
                        var resResult: MediaRadarVM.SearchResultUiItem? = null
                        var firstLoadingPlayerLine: SourceManifest? = null
                        for (item in logic.searchItemList) {
                            if (item.playerLineState.isLoading()) {
                                firstLoadingPlayerLine = item.businessPair.first.source.manifest
                            }
                            // 编辑距离小于 3 且 有播放线路
                            if (item.nameDistance < 3 && item.playerLineState.okOrNull()?.isNotEmpty() == true) {
                                resResult = item
                                break
                            }
                        }
                        val result = resResult
                        val showPlayerLineLoadingSource: SourceManifest? = firstLoadingPlayerLine
                        if (result != null) {
                            update {
                                // 兜底 result 为空才添加
                                it.copy(
                                    finderResult = it.finderResult ?: SelectionResult(
                                        playCover = result.cover,
                                        businessPair = result.businessPair,
                                    ),
                                    silentNotResult = false,
                                    silentFinding = false,
                                    findingState = null,
                                )
                            }
                        } else if (showPlayerLineLoadingSource != null) {
                            // 静默搜索中
                            update {
                                it.copy(
                                    findingState = FindingState(
                                        showingSource = showingSource,
                                        allSourceCount = allSourceCount,
                                        resultSourceCount = resultSourceCount,
                                    )
                                )
                            }
                        } else {
                            update {
                                it.copy(
                                    silentFinding = false,
                                    silentNotResult = true,
                                    findingState = null,
                                )
                            }
                        }
                    } else {
                        // 静默搜索中
                        update {
                            it.copy(
                                findingState = FindingState(
                                    showingSource = showingSource,
                                    allSourceCount = allSourceCount,
                                    resultSourceCount = resultSourceCount,
                                )
                            )
                        }
                    }

                }
            }.collect()

        }
    }

    // 用户手动点击的，为最高优先级
    fun onUserResultSelect(result: SelectionResult) {
        update {
            it.copy(
                finderResult = result,
                silentFinding = false,
                findingState = null,
                showPanel = false
            )
        }
    }

    fun silentFinding(
        keyword: String,
    ) {

        logger.info("MediaFinderVM silentFinding: $keyword")
        viewModelScope.launch {
            update {
                it.copy(
                    silentFinding = true,
                )
            }
            radarVM.search(keyword)
        }
    }


    fun updateSuggestKeywordChange(suggestList: List<String>) {
        this.keywordSuggest = suggestList
        searchVM.updateSuggestKeywordChangeIfNeed(suggestList)
        radarVM.updateSuggestKeywordChangeIfNeed(suggestList)
    }



    fun showPanel(
        show: Boolean,
    ) {
        update {
            it.copy(
                showPanel = show
            )
        }
    }




}