package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.source.bangumi.model.BgmCharacter
import org.easybangumi.next.shared.source.bangumi.model.BgmEpisode
import org.easybangumi.next.shared.source.bangumi.model.BgmPerson
import org.easybangumi.next.shared.source.bangumi.model.BgmReviews
import org.easybangumi.next.shared.source.bangumi.model.BgmSubject
import org.easybangumi.next.shared.source.case.DetailSourceCase
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

class BangumiDetailViewModel(
    val cartoonIndex: CartoonIndex,
): StateViewModel<BangumiDetailViewModel.State>(State()) {

    enum class DetailTab (
        val index: Int,
        val title: ResourceOr,
    ) {
        DETAIL(0, Res.strings.detailed),
        EPISODE(1, Res.strings.episode),
        COMMENT(2, Res.strings.comment),
    }

    val detailSourceCase: DetailSourceCase by inject()
    val bangumiDetailBusiness = detailSourceCase.getBangumiDetailBusiness()

    val subjectRepository = bangumiDetailBusiness.runDirect { getOrCreateSubjectRepository(cartoonIndex) }
    val characterRepository = bangumiDetailBusiness.runDirect { getOrCreateCharacterRepository(cartoonIndex) }
    val personRepository = bangumiDetailBusiness.runDirect { getOrCreatePersonRepository(cartoonIndex) }

    val detailTabList = DetailTab.entries.toList()

    data class State(
        val currentTab: DetailTab = DetailTab.DETAIL,
        val subjectState: DataState<BgmSubject> = DataState.none(),
        val characterState: DataState<List<BgmCharacter>> = DataState.none(),
        val personState: DataState<List<BgmPerson>> = DataState.none(),


        val commentPaging: PagingFlow<BgmReviews>? = null,
        val episodePaging: PagingFlow<BgmEpisode>? = null,
    )


    val coverUrl  = bangumiDetailBusiness.runDirect {
       coverUrl(cartoonIndex)
    }

    private val episodeInit = atomic(false)

    init {
        viewModelScope.launch {
            combine(
                subjectRepository.flow,
                characterRepository.flow,
                personRepository.flow,
            ) { subject, character, person ->
                if (subject.isOk()) {
                    tryInitEpisode()
                }
                update {
                    it.copy(
                        subjectState = subject,
                        characterState = character,
                        personState = person
                    )
                }
            }.collect()
        }

        subjectRepository.refresh()
        characterRepository.refresh()
        personRepository.refresh()


//        refreshComment()
//        refreshEpisodeList()
    }

    private fun getCommentPagingFlow(): PagingFlow<BgmReviews> {
        return bangumiDetailBusiness.runDirect {
            val pagingSource = createCommentPagingSource(cartoonIndex)
            pagingSource.newPagingFlow()
        }
    }

    private fun getEpisodePagingFlow(): PagingFlow<org.easybangumi.next.shared.source.bangumi.model.BgmEpisode> {
        return bangumiDetailBusiness.runDirect {
            val pagingSource = createEpisodePagingSource(cartoonIndex)
            pagingSource.newPagingFlow()
        }
    }

    fun refreshComment() {
        val pagingFlow = getCommentPagingFlow().cachedIn(viewModelScope)
        update {
            it.copy(commentPaging = pagingFlow)
        }
    }

    fun tryInitEpisode() {
        if (episodeInit.compareAndSet(false, update = true)) {
            refreshEpisodeList()
        }
    }

    fun refreshEpisodeList() {
        val pagingFlow = getEpisodePagingFlow().cachedIn(viewModelScope)
        update {
            it.copy(episodePaging = pagingFlow)
        }
    }





}