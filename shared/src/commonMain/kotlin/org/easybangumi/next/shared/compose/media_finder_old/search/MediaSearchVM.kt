package org.easybangumi.next.shared.compose.media_finder_old.search

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.component.search.createPagingSource
import org.koin.core.component.inject

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
class MediaSearchVM(
    private var suggestKeyword: List<String>,
) : StateViewModel<MediaSearchVM.State>(
    initState = State()
) {

    private val disableSourceFileHelper = JsonlFileHelper<String>(
        pathProvider.getFilePath("radar"),
        "disable_source",
        String::class,
    )
    private val disableSourceSet by lazy {
        disableSourceFileHelper.flow()
    }

    data class State(
        val suggestShowAll: Boolean = false,            // 是否显示全部建议
        val fieldText: String = "",                     // 搜索框显示的文字
        val searchKeyword: String? = null,              // 真正搜索的关键字
        val playSourceLoading: Boolean = true,          // 加载中
        val disableSourceSet: Set<String> = setOf(),    // 禁用的搜索源
        val lineState: List<LineState> = listOf(),      // 各搜索源的状态
        val popup: Popup? = null,
    )

    sealed class Popup {
        data class EditState(
            val keywordList: List<String>,
        ): Popup()

    }

    data class SelectionResult(
        val playCover: CartoonCover,
        val businessPair: ComponentBusinessPair<SearchComponent, PlayComponent>,
    ) {
        val searchBusiness = businessPair.first
        val playBusiness = businessPair.second
    }

    data class SearchItem(
        val businessPair: ComponentBusinessPair<SearchComponent, PlayComponent>,
        val cover: CartoonCover,
        val playerLineState: DataState<List<PlayerLine>>,
    )


    data class LineState(
        val businessPair: ComponentBusinessPair<SearchComponent, PlayComponent>,
        val pagingFlow: DataState<PagingFlow<CartoonCover>> = DataState.Companion.none(),
        val expandedMap: Map<String, DataState<List<PlayerLine>>> = mapOf(),
    ) {
        val searchBusiness = businessPair.first
        val playBusiness = businessPair.second
    }

    private val sourceCase: SourceCase by inject()

    private var pagingTemp: Pair<String, Map<String, PagingFlow<CartoonCover>>>? = null


    init {
        viewModelScope.launch {
            combine(
                state.map { it.searchKeyword }.distinctUntilChanged(),
                sourceCase.searchBusinessWithPlayFlow().distinctUntilChanged()
            ) { keyword, playBusiness ->

                if (playBusiness.isLoading) {
                    update {
                        it.copy(playSourceLoading = true)
                    }
                } else if (keyword == null) {
                    update {
                        it.copy(
                            playSourceLoading = false,
                            lineState = listOf(),
                        )
                    }
                } else {
                    val map = hashMapOf<String, PagingFlow<CartoonCover>>()
                    val temp = pagingTemp
                    if (temp != null && temp.first == keyword) {
                        map.putAll(temp.second)
                    }
                    val res = playBusiness.business.map {
                        val t = map[it.first.source.key]
                        if (t != null) {
                            LineState(it, DataState.Companion.ok(t))
                        } else {
                            val pagingSource = it.first.createPagingSource(keyword)

                            val pagingFlow = pagingSource.newPagingFlow().cachedIn(viewModelScope)
                            map[it.first.source.key] = pagingFlow
                            LineState(it, DataState.Companion.ok(pagingFlow))
                        }
                    }
                    pagingTemp = keyword to map
                    update {
                        it.copy(
                            playSourceLoading = false,
                            lineState = res,
                        )
                    }
                }
            }.collect()
        }
    }

    fun onSourceDisableChange(
        key: String,
    ){
        viewModelScope.launch {
            disableSourceFileHelper.update {
                val set = it.toMutableSet()
                if (set.contains(key)) {
                    set.remove(key)
                } else {
                    set.add(key)
                }
                set.toList()
            }
        }

    }



    fun onFieldChange(
        text: String,
    ){
        update {
            it.copy(fieldText = text)
        }
    }

    fun onSearchKeywordChange(){
        update {
            it.copy(searchKeyword = it.fieldText)
        }
    }

    fun suggestShowAll(showAll: Boolean){
        update {
            it.copy(suggestShowAll = showAll)
        }
    }

    fun updateSuggestKeywordChangeIfNeed(
        suggestKeywordList: List<String>,
    ){
        update {
            it.copy(
                popup = if (it.popup is MediaSearchVM.Popup.EditState) {
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
                popup = MediaSearchVM.Popup.EditState(
                    keywordList = suggestKeyword
                )
            )
        }
    }
}