package org.easybangumi.next.shared.compose.detail.bangumi

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.cachedIn
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.shared.RouterPage
import org.easybangumi.next.shared.cartoon.collection.BgmCollectInfoVM
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.compose.bangumi.comment.BangumiCommentVM
import org.easybangumi.next.shared.data.CartoonInfoCase
import org.easybangumi.next.shared.data.bangumi.*
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.source.SourceCase
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

class BangumiDetailVM(
    val cartoonIndex: CartoonIndex,
    val panelMode: Boolean = false,
): StateViewModel<BangumiDetailVM.State>(State()) {

    enum class DetailTab (
        val index: Int,
        val title: ResourceOr,
    ) {
        DETAIL(0, Res.strings.detailed),
        EPISODE(1, Res.strings.episode),
        COMMENT(2, Res.strings.comment),
    }
//    private val logger = logger()

    val isDetailShowAll =  mutableStateOf(false)
    val isTabShowAll = mutableStateOf(false)

    val bgmCollectInfoVM: BgmCollectInfoVM by childViewModel {
        BgmCollectInfoVM(cartoonIndex)
    }


    val bangumiCase: BangumiCase by inject()
    val sourceCase: SourceCase by inject()
    val bangumiDetailBusiness = sourceCase.getBangumiDetailBusiness()

    val subjectRepository = bangumiCase.getSubjectRepository(cartoonIndex)
    val characterRepository = bangumiCase.getCharacterListRepository(cartoonIndex)
    val personRepository = bangumiCase.getPersonListRepository(cartoonIndex)

    val coverUrl  = bangumiCase.coverUrl(cartoonIndex)

    val bangumiCommentVM: BangumiCommentVM by childViewModel {
        BangumiCommentVM(cartoonIndex)
    }


    val detailTabList = DetailTab.entries.toList()

    data class State(
        val currentTab: DetailTab = DetailTab.DETAIL,
        val subjectState: DataState<BgmSubject> = DataState.none(),
        val characterState: DataState<List<BgmCharacter>> = DataState.none(),
        val personState: DataState<List<BgmPerson>> = DataState.none(),

        val collectionState: DataState<BgmCollectResp> = DataState.none(),
        val cartoonInfo: CartoonInfo? = null,

        val commentPaging: PagingFlow<BgmReviews>? = null,
        val episodePaging: PagingFlow<BgmEpisode>? = null,

        val hasBgmAccountInfo: Boolean = false,

        val dialog: Dialog? = null
    )

    sealed class Dialog {
        data class CollectDialog(val cartoonCover: CartoonCover): Dialog()
    }

    fun openCollectDialog() {
        state.value.subjectState.okOrNull()?.cartoonCover?.let { cover ->
            update {
                it.copy(
                    dialog = Dialog.CollectDialog(cover)
                )
            }
        }
    }

    fun dialogDismiss() {
        update {
            it.copy(
                dialog = null
            )
        }
    }


    private val lowPriorityDataInit = atomic(false)
    private val episodeInit = atomic(false)

    init {
        // 如果没有来自网络的数据则加载
        subjectRepository.refreshIfNoneOrCache()

        // bangumi 非登录态相关数据流绑定
        // 详情 角色 声优
        viewModelScope.launch {
            combine(
                subjectRepository.flow,
                characterRepository.flow,
                personRepository.flow,
            ) { subject, character, person ->
                if (subject.isOk()) {
                    viewModelScope.launch {
                        logger.info("try init low priority data and episode data")
                        tryInitLowPriorityData()
                        tryInitEpisode()
                    }
                }
                update {
                    it.copy(
                        subjectState = subject,
                        characterState = character,
                        personState = person,
                    )
                }
            }.collect()
        }

        // bgm 收藏状态 & cartoonInfo 数据
        viewModelScope.launch {
            bgmCollectInfoVM.logic.collectLatest { state ->
                update {
                    it.copy(
                        collectionState = state.collectionState,
                        hasBgmAccountInfo = state.hasBgmAccountInfo,
                        cartoonInfo = state.cartoonInfo,
                    )
                }
            }

        }

    }

    fun loadSubject(cacheAvailableMs : Long = -1) {
        if (cacheAvailableMs <= 0) {
            subjectRepository.refresh()
        } else {
            subjectRepository.refreshIfNoneOrExpired(cacheAvailableMs)
        }
    }

    private val firstLoad = atomic(false)
    fun loadSubjectIfFirst() {
        if (firstLoad.compareAndSet(false, true)) {
            loadSubject()
        }
    }

    fun loadCharacter() {
        characterRepository.refreshIfNone()
    }

    fun loadPerson() {
        personRepository.refreshIfNone()
    }



    private fun getCommentPagingFlow(): PagingFlow<BgmReviews> {
        return bangumiDetailBusiness.runDirect {
            val pagingSource = createCommentPagingSource(cartoonIndex)
            pagingSource.newPagingFlow()
        }
    }

    private fun getEpisodePagingFlow(): PagingFlow<BgmEpisode> {
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

    fun tryInitLowPriorityData() {
        if (lowPriorityDataInit.compareAndSet(expect = false, update = true)) {
            loadCharacter()
            loadPerson()
        }
    }

    fun tryInitEpisode() {
        if (episodeInit.compareAndSet(expect = false, update = true)) {
            refreshEpisodeList()
        }
    }

    fun refreshEpisodeList() {
        val pagingFlow = getEpisodePagingFlow().cachedIn(viewModelScope)
        update {
            it.copy(episodePaging = pagingFlow)
        }
    }


    fun onEpisodeClick(
        episode: BgmEpisode,
        navController: NavController,
    ) {
        val sub = state.value.subjectState.okOrNull() ?: return

        navController.navigate(
            RouterPage.Media.from(
                cartoonIndex = sub.cartoonIndex,
                cartoonCover = sub.cartoonCover,
                suggestEpisode = episode.ep?.toInt(),
            )
        )

    }


    fun onPlayClick(
        navController: NavController,
    ) {
        val sub = state.value.subjectState.okOrNull() ?: return

        navController.navigate(
            RouterPage.Media.from(
                cartoonIndex = sub.cartoonIndex,
                cartoonCover = sub.cartoonCover,
                suggestEpisode = null,
            )
        )
    }




}