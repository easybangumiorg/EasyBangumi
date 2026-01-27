package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.cartoon.radar.v1.CartoonRadarStrategyV1
import org.easybangumi.next.shared.cartoon.radar.v1.CartoonRadarV1VM
import org.easybangumi.next.shared.cartoon.search.CartoonSearchVM
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.api.component.getManifest
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
): StateViewModel<MediaFinderVM.State>(State()) {

    // 普通搜索
    val searchVM: CartoonSearchVM by childViewModel {
        CartoonSearchVM()
    }

    // 智能搜索
    val radarV1VM: CartoonRadarV1VM by childViewModel {
        CartoonRadarV1VM()
    }

    data class SelectionResult(
        val playCover: CartoonCover,
        val manifest: SourceManifest,
        val suggestPlayerLine: PlayerLine? = null,
    )


    data class State(

        val keyword: String? = null,
        val panelShow: Boolean = false,
        // 静默搜索结果
        val silentFinding: Boolean = false,
        val silentNotResult: Boolean = false,

        val result: SelectionResult? = null,

        val radarUIState: RadarUIState = RadarUIState.EMPTY,
        val searchUIState: SearchUIState = SearchUIState(),

        val popup: Popup? = null,
    )

    sealed class Popup {
        data class KeywordEdit(
            val keywordList: List<String>,
            val currentKeyword: String,
        ): Popup()

    }

    data class RadarUIState(
        val radarSourceTabList: List<RadarSourceTab> = emptyList(),
        val selectionSource: SourceManifest? = null,
        val result: List<CartoonRadarStrategyV1.CartoonCoverResult> = emptyList(),
    ) {
        companion object {
            val EMPTY = RadarUIState()
        }
        val resultTab: List<RadarSourceTab> by lazy {
            radarSourceTabList.filter { !it.loading && !it.error && it.count > 0  }
        }
        val resultTabCount get() = resultTab.size
    }

    data class RadarSourceTab(
        val sourceManifest: SourceManifest,
        val count: Int = 0,
        val loading: Boolean = false,
        val error: Boolean = false,
    )


    data class SearchUIState(
        val loading: Boolean = false,
        val searchLineStateList: List<SearchLineState> = emptyList(),
    )
    data class SearchLineState(
        val sourceManifest: SourceManifest,
        val flow: PagingFlow<CartoonCover>
    )

    val labelList = listOf(
        "聚合搜索",
        "手动搜索",
    )

    val pagerState = PagerState { 2 }


    var silentFindingJob: Job? = null

    init {
//        viewModelScope.launch {
//            state.map { it.keyword }.collectLatest {
//                searchVM.changeKeyword(it)
//                radarV1VM.changeKeyword(it)
//            }
//        }
        // 排序交给 io 线程
        viewModelScope.launch(coroutineProvider.io()) {
            radarV1VM.logic.collectLatest {
                val tabList = it.result.sourceSearchResMap.map {
                    RadarSourceTab(
                        sourceManifest = it.key.getManifest(),
                        count = it.value.okOrNull()?.size ?: 0,
                        loading = it.value.isLoading(),
                        error = it.value.isError(),
                    )
                }
                val resultList = it.result.sourceSearchResMap.flatMap {
                    it.value.okOrNull() ?: emptyList()
                }.sortedBy { it.nameDistance }
                update {
                    it.copy(
                        radarUIState = it.radarUIState.copy(
                            radarSourceTabList = tabList,
                            result = resultList,
                        )
                    )
                }

            }
        }

        viewModelScope.launch {
            searchVM.logic.collectLatest { searchState ->
                val searchLineList = mutableListOf<SearchLineState>()
                searchState.searchBusiness.forEach {
                    val flow = it.source.key.let { key ->
                        searchState.pagingFlowMap[key]
                    }
                    if (flow != null) {
                        searchLineList.add(
                            SearchLineState(
                                sourceManifest = it.source.manifest,
                                flow = flow,
                            )
                        )
                    }
                }
                update {
                    it.copy(
                        searchUIState = SearchUIState(
                            loading = false,
                            searchLineStateList = searchLineList,
                        )
                    )
                }
            }
        }



    }

    // 用户手动点击的，为最高优先级
    fun onUserResultSelect(result: SelectionResult) {
        silentFindingJob?.cancel()
        update {
            it.copy(
                result = result,
                silentFinding = false,
            )
        }
    }

    fun changeKeywordSuggest(list: List<String>) {
        keywordSuggest = list
        update {
            it.copy(
                popup = when (val popup = it.popup) {
                    is Popup.KeywordEdit -> {
                        popup.copy(
                            keywordList = list,
                        )
                    }
                    else -> null
                }
            )
        }
    }

    fun changeKeyword(keyword: String?) {
        update {
            it.copy(keyword = keyword,)
        }
    }


    fun silentFind(keyword: String){
        silentFindingJob?.cancel()
        silentFindingJob = viewModelScope.launch {
            update {
                it.copy(
                    silentFinding = true,
                    keyword = keyword
                )
            }
            radarV1VM.changeKeyword(keyword)
            state.collectLatest {
                if (it.radarUIState != RadarUIState.EMPTY && it.silentFinding
                    && it.result == null) {

                    val res = it.radarUIState.result.firstOrNull() {
                        it.playerLine?.isNotEmpty() == true && it.nameDistance < 3
                    }
                    if (res == null) {
                        if( !it.radarUIState.radarSourceTabList.any { it.loading }) {
                            update {
                                it.copy(
                                    silentFinding = false,
                                    silentNotResult = true,
                                )
                            }
                            silentFindingJob?.cancel()
                        }

                    } else {
                        update {
                            // 兜底判断，只有用户没选择时才使用静默搜索的结果
                            it.copy(
                                result = it.result
                                    ?: SelectionResult(
                                        playCover = res.cover,
                                        manifest = res.businessPair.getManifest(),
                                        suggestPlayerLine = res.playerLine?.firstOrNull(),
                                    ),
                                silentFinding = false,
                            )
                        }
                        silentFindingJob?.cancel()
                    }

                }
            }

        }
    }

    fun showKeywordEditPopup() {
        val currentList = keywordSuggest
        update {
            it.copy(
                popup = Popup.KeywordEdit(
                    keywordList = currentList,
                    currentKeyword = state.value.keyword ?: currentList.firstOrNull() ?: "",
                )
            )
        }
    }
    fun dismissPopup() {
        update {
            it.copy(
                popup = null,
            )
        }
    }

    fun hidePanel() {
        update {
            it.copy(
                panelShow = false,
            )
        }
    }

    fun showPanel() {
        update {
            it.copy(
                panelShow = true,
            )
        }
    }


}

