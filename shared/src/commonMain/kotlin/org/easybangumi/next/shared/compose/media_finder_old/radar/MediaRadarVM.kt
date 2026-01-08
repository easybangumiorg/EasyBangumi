package org.easybangumi.next.shared.compose.media_finder_old.radar

import androidx.lifecycle.viewModelScope
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.store.preference.PreferenceStore
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.lib.utils.safeCancel
import org.easybangumi.next.shared.cartoon.radar.editDistance
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.FinderComponentPair
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.koin.core.component.inject
import kotlin.getValue
import kotlin.to

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
 *
 *  雷达扫描模式
 *  - 每个源固定搜索前 N 个结果，聚合展示同时搜索播放线路
 */
class MediaRadarVM(
    private var suggestKeyword: List<String>,
): StateViewModel<MediaRadarVM.UiState>(UiState(), true) {

    // ========  UI 状态  ============================

    // 纯 UI 状态，不包含逻辑
    data class UiState(
        val popup: Popup? = null,

        val searchKeyword: String = "",
        // 先保证源列表数据装载前有 loading 态

        val selectionSourceTab: SourceTabUiState? = null,

        // 以下数据只允许来自 logicState
        val isSourceLoading: Boolean = true,
        val sourceTabList: List<SourceTabUiState> = emptyList(),
        val searchItemList: List<SearchResultUiItem> = listOf(),
    )


    data class SourceTabUiState(
        val sourceManifest: SourceManifest,
        val loading: Boolean,
        val isError: Boolean,
        val count: Int,
    )

    data class SearchResultUiItem(
        val cover: CartoonCover,
        val sourceManifest: SourceManifest,
        val playerLineState: DataState<List<PlayerLine>>,
        val indexForSource: Int,
        val nameDistance: Int,
        val businessPair: FinderComponentPair,
    )

    sealed class Popup {
        data class EditState(
            val keywordList: List<String>,
        ): Popup()

    }

    private val singleDispatcher = coroutineProvider.singleForTag("RadarVM")

    private val preferenceStore: PreferenceStore by inject()
    // 每个源扫描前 n 个
    private val sourceSearchLimitPref = preferenceStore.getInt("radar_source_search_limit", 10)
    private val sourceCase: SourceCase by inject()

    // 最外层搜索关键词刷新任务
    private var sourceKeywordRefreshJob: Job? = null

    // 每个源的搜索任务
    // sourceKey to job
    private var sourceSearchTaskMap = mutableMapOf<String, SourceSearchTask>()
    data class SourceSearchTask(
        val business: ComponentBusinessPair<SearchComponent, PlayComponent>,
        val keyword: String,
        val job: Job,
    )


    // 每个源的播放线路搜索任务
    // identify to job
    private var coverPlayerLineSearchTaskMap = mutableMapOf<String, CoverPlayerLineSearchTask>()
    data class CoverPlayerLineSearchTask(
        val cover: CartoonCover,
        val businessPair: FinderComponentPair,
        val job: Job,
    )

    data class LogicState(
        val searchingKeyword: String = "",
        val sourceSearchResMap : Map<FinderComponentPair, DataState<List<CartoonCoverResult>>> = emptyMap(),
        // identify to playerLine
        val coverPlayerLineStateMap: Map<String, DataState<List<PlayerLine>>> = emptyMap(),
    )

    data class CartoonCoverResult(
        val cover: CartoonCover,
        val businessPair: FinderComponentPair,
        // 名称编辑距离
        val nameDistance: Int,
    )

    val realLogicFlow = MutableStateFlow(LogicState())

    private val firstSearch = atomic(false)



    init {
        // 排序交给 io 线程
        viewModelScope.launch(coroutineProvider.io()) {
            realLogicFlow.collectLatest {
                val logicState = it
                logger.info("radar logic state update: searchingKeyword=${logicState.searchingKeyword}, sourceSearchResMap size=${logicState.sourceSearchResMap.size}, coverPlayerLineStateMap size=${logicState.coverPlayerLineStateMap.size}")
                val res = logicState.sourceSearchResMap.flatMap { entity ->
                    entity.value.map { it.mapIndexed { index, result ->
                        val cover = result.cover
                        SearchResultUiItem(
                            cover,
                            entity.key.first.source.manifest,
                            logicState.coverPlayerLineStateMap[cover.toIdentify()]?: DataState.none(),
                            index,
                            result.nameDistance,
                            entity.key,
                        ) }}.okOrNull() ?:emptyList()
                }.sortedBy { it.nameDistance }
                val sourceTabList = logicState.sourceSearchResMap.map {
                    val sourceRes = it.value
                    val count = sourceRes.okOrNull()?.size
                    SourceTabUiState(
                        sourceManifest = it.key.first.source.manifest,
                        loading = false,
                        isError = sourceRes.isError(),
                        count = count ?: 0,
                    )
                }
                update { uiState ->
                    uiState.copy(
                        sourceTabList = sourceTabList,
                        searchItemList = res,
                        searchKeyword = logicState.searchingKeyword,
                    )
                }
            }
        }


    }



    fun search(
        keyword: String,
    ) {
        firstSearch.value = true
        sourceKeywordRefreshJob?.safeCancel()
        // 逻辑太复杂了，调度直接 singleDispatcher 后续在考虑优化吧
        sourceKeywordRefreshJob = viewModelScope.launch(singleDispatcher) {
            // 这里直接获取播放源，按理说走到这了播放源应该加载成功了，后续有问题再改吧
            val findSearchBusinessResp = sourceCase.findSearchBusiness(true)
            update { sta ->
                sta.copy(isSourceLoading = findSearchBusinessResp.isLoading)
            }
            realLogicFlow.update {
                it.copy(searchingKeyword = keyword)
            }
            sourceSearchTaskMap.forEach {
                it.value.job.safeCancel()
            }
            sourceSearchTaskMap.clear()
            coverPlayerLineSearchTaskMap.forEach {
                it.value.job.safeCancel()
            }
            coverPlayerLineSearchTaskMap.clear()
            val map = mutableMapOf<FinderComponentPair, DataState<List<CartoonCoverResult>>>()
            findSearchBusinessResp.business.map {
                map[it] = DataState.none()
            }
            realLogicFlow.update {
                it.copy(
                    sourceSearchResMap = map
                )
            }
            update {
                it.copy(searchKeyword = keyword)
            }
            findSearchBusinessResp.business.forEach {
                val sourceKey = it.first.source.key

                val newTask = newSourceSearchTask(it, keyword)
                sourceSearchTaskMap[sourceKey] = newTask
            }

        }
    }

    private fun newSourceSearchTask(
        business: ComponentBusinessPair<SearchComponent, PlayComponent>,
        keyword: String,
    ): SourceSearchTask {
        realLogicFlow.update { logic ->
            val map = logic.sourceSearchResMap.toMutableMap()
            map[business] = DataState.loading()
            logic.copy(
                sourceSearchResMap = map
            )
        }
        val job = viewModelScope.launch {
            val limit = sourceSearchLimitPref.get()
            val res = business.first.run {
                var key: String? = firstKey()
                val res = arrayListOf<CartoonCover>()
                var isError = false
                var errorMsg: String? = null
                while (res.size < limit && key != null && !isError) {
                    val page = search(keyword, key)
                    val data = page.okOrNull()
                    if (page.isOk() && data != null) {
                        res.addAll(data.second)
                        key = data.first
                    } else {
                        isError = true
                        errorMsg = page.mapError { it.errorMsg }?:"unknown error"
                    }
                }
                // 如果第二页错误了但是第一页有数据，也算成功
                if (!isError || res.isNotEmpty()) {
                    DataState.ok(res.toList())
                } else {
                    DataState.error(errorMsg ?: "unknown error")
                }
            }
            updateSourceSearchRes(res, business)
        }
        return SourceSearchTask(
            business = business,
            keyword = keyword,
            job = job,
        )
    }

    private fun updateSourceSearchRes(
        coverListState: DataState<List<CartoonCover>>,
        business: ComponentBusinessPair<SearchComponent, PlayComponent>,
    ) {
        realLogicFlow.update { logic ->
            val map = logic.sourceSearchResMap.toMutableMap()
            map[business] = coverListState.map {
                it.map { cover ->
                    CartoonCoverResult(
                        cover = cover,
                        businessPair = business,
                        nameDistance = cover.name.editDistance(logic.searchingKeyword),
                    )
                }
            }
            logic.copy(
                sourceSearchResMap = map
            )
        }

        val list = coverListState.okOrNull()
        if (list != null) {
            viewModelScope.launch(singleDispatcher) {
                list.forEach {
                    coverPlayerLineSearchTaskMap[it.toIdentify()]?.job?.safeCancel()
                    val task = newPlayLineSearchTask(business, it)
                    coverPlayerLineSearchTaskMap[it.toIdentify()] = task
                }
            }
        }

    }

    private fun newPlayLineSearchTask(
        business: ComponentBusinessPair<SearchComponent, PlayComponent>,
        cover: CartoonCover,
    ): CoverPlayerLineSearchTask {
        realLogicFlow.update { logic ->
            logic.copy(
                coverPlayerLineStateMap = logic.coverPlayerLineStateMap.map {
                    if (it.key == cover.toIdentify()) {
                        it.key to DataState.loading()
                    } else {
                        it.key to it.value
                    }
                }.toMap()
            )
        }
        val job = viewModelScope.launch {
            val res = business.second.run {
                getPlayLines(cover.toCartoonIndex())
            }
            realLogicFlow.update { logic ->
                logic.copy(
                    coverPlayerLineStateMap = logic.coverPlayerLineStateMap.map {
                        if (it.key == cover.toIdentify()) {
                            it.key to res
                        } else {
                            it.key to it.value
                        }
                    }.toMap()
                )
            }
        }
        return CoverPlayerLineSearchTask(
            cover = cover,
            businessPair = business,
            job = job,
        )
    }


    fun updateSuggestKeywordChangeIfNeed(
        suggestKeywordList: List<String>,
    ){
        suggestKeyword = suggestKeywordList
        update {
            it.copy(
                popup = if (it.popup is Popup.EditState) {
                    it.popup.copy(
                        keywordList = suggestKeywordList
                    )
                } else {
                    it.popup
                }
            )
        }
    }

    fun showEditPopup(){
        update {
            it.copy(
                popup = Popup.EditState(
                    keywordList = suggestKeyword
                )
            )
        }
    }



}