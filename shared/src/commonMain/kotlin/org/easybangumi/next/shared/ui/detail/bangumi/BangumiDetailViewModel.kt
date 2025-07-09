package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import org.easybangumi.ext.shared.plugin.bangumi.model.*
import org.easybangumi.ext.shared.plugin.bangumi.plugin.BangumiMetaComponent
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.plugin.api.component.ComponentBusiness
import org.easybangumi.next.shared.plugin.api.toDataState
import org.easybangumi.next.shared.resources.Res

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

class BangumiDetailViewModel(
    val cartoonIndex: CartoonIndex,
    val metaBusiness: ComponentBusiness<BangumiMetaComponent>,
): StateViewModel<BangumiDetailViewModel.State>(State()) {

    enum class DetailTab (
        val index: Int,
        val title: ResourceOr ,
    ) {
        DETAIL(0, Res.strings.detailed),
        COMMENT(1, Res.strings.comment),
        EPISODE(2, Res.strings.episode),
    }

    val detailTabList = DetailTab.entries.toList()

    data class State(
        val currentTab: DetailTab = DetailTab.DETAIL,
        val subjectState: DataState<Subject> = DataState.none(),
        val commentPaging: PagingFlow<Reviews>? = null,
        val episodePaging: PagingFlow<Episode>? = null,
        val characterState: DataState<List<Character>> = DataState.none(),
        val personState: DataState<List<Person>> = DataState.none(),
    )


    val coverUrl  = metaBusiness.runDirect {
        getMateManager().coverUrl(cartoonIndex)
    }

    init {
        refreshSubject()
        refreshComment()
        refreshEpisodeList()
    }

    private fun getCommentPagingFlow(): PagingFlow<Reviews> {
        return metaBusiness.runDirect {
            val manager = getMateManager()
            val pagingSource = manager.createCommentPagingSource(cartoonIndex)
            pagingSource.newPagingFlow()
        }
    }

    private fun getEpisodePagingFlow(): PagingFlow<Episode> {
        return metaBusiness.runDirect {
            val manager = getMateManager()
            val pagingSource = manager.createEpisodePagingSource(cartoonIndex)
            pagingSource.newPagingFlow()
        }
    }

    fun refreshComment() {
        val pagingFlow = getCommentPagingFlow().cachedIn(viewModelScope)
        update {
            it.copy(commentPaging = pagingFlow)
        }
    }

    fun refreshEpisodeList() {
        val pagingFlow = getEpisodePagingFlow().cachedIn(viewModelScope)
        update {
            it.copy(episodePaging = pagingFlow)
        }
    }

    fun refreshDetail() {
        update {
            it.copy(
                characterState = DataState.loading(),
                personState = DataState.loading()
            )
        }
        viewModelScope.launch {
            val characterRespDeferred = metaBusiness.async {
                val manager = getMateManager()
                manager.getCharacter(cartoonIndex)
            }
            val personRespDeferred = metaBusiness.async {
                val manager = getMateManager()
                manager.getPerson(cartoonIndex)
            }
            val personResp = personRespDeferred.await()
            val characterResp = characterRespDeferred.await()
            update {
                it.copy(
                    characterState = characterResp.toDataState(),
                    personState = personResp.toDataState()
                )
            }
        }
    }


    fun refreshSubject() {
        update {
            it.copy(subjectState = DataState.loading())
        }
        viewModelScope.launch {
            val resp = metaBusiness.run {
                val manager = getMateManager()
                manager.getSubject(cartoonIndex)
            }
            update {
                it.copy(subjectState = resp.toDataState())
            }
        }
    }




}