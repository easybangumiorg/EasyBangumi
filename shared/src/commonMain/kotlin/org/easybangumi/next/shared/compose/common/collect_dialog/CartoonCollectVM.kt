package org.easybangumi.next.shared.compose.common.collect_dialog

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.bangumi.data.repository.BangumiCollectionRepository
import org.easybangumi.next.shared.cartoon.collection.CartoonCollectionController
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
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
class CartoonCollectVM(
    private val cartoonCover: CartoonCover,
): StateViewModel<CartoonCollectVM.State>(State(), true) {

    val collectController: CartoonCollectionController by inject()
    val bangumiCase: BangumiCase by inject()
    val bgmUserDataProvider = bangumiCase.flowUserDataProvider()
    val bangumiCollectionRepositoryFlow = bgmUserDataProvider
        .map {
                provider ->
            // provider 为空代表没有登录，返回空的收藏状态
            provider?.getCollectRepository(cartoonCover.toCartoonIndex())
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    data class State(
        val localState: LocalState = LocalState(),
        // bangumi 板块可能不展示
        val bangumiState: BangumiState? = null,
    )


    data class LocalState(
        val isLoading: Boolean = false,
        val tagList: List<CartoonTag> = listOf(),
        val selection: Set<CartoonTag> = setOf(),
    )

    data class BangumiState(
        val isLoading: Boolean = true,

        // 是否在更新
        val updatingSelection: BangumiConst.BangumiCollectType? = null,
        val updateErrorMsg: String? = null,

        val isError: Boolean = false,
        val errMsg: String? = null,
        val typeList: List<BangumiConst.BangumiCollectType> = BangumiConst.collectTypeList,
        val selection: BangumiConst.BangumiCollectType? = null,
    )

    init {

        // 1. local 模块
        viewModelScope.launch {
            combine(
                // 预处理一下，优化性能
                collectController.collectionFlow.map {
                    val localTagList = it.tagList.filter { it.isLocal }
                    val info = it.cartoonInfo2LocalTag.keys.firstOrNull { it.fromSourceKey == cartoonCover.source && it.fromId == cartoonCover.id }
                    val realTags = (if (info != null) {
                        it.cartoonInfo2LocalTag[info]?.toSet() ?: setOf()
                    } else {
                        setOf()
                    }).toMutableSet()
                    realTags to localTagList
                }.distinctUntilChanged(),
                collectController.collectionFlow.map {
                    it.bangumiCollectState
                }
            ) {  localPair, bangumiCollectState ->
                val realTags = localPair.first
                val localTagList = localPair.second

                // 如果没有任何标签（没有本地收藏） && 本地只有一个标签 && Bangumi 未登录，帮用户手动选择第一个标签，优化一下体验
                // 虽然一般这种情况不会弹出收藏对话框（
                if (realTags.isEmpty() && localTagList.size == 1 && !bangumiCollectState.isLogin) {
                    val firstTag = localTagList.first()
                    realTags += firstTag
                }

                val localState = LocalState(
                    isLoading = false,
                    tagList = localTagList,
                    selection = realTags,
                )

                update {
                    it.copy(
                        localState = localState,
                    )
                }



            }.collect()
        }

        // 2. bangumi 模块
        viewModelScope.launch {
            bangumiCollectionRepositoryFlow.flatMapLatest { repository ->
                repository?.refreshIfNoneOrCache()
                repository?.flow ?: flowOf(null)
            }.distinctUntilChanged().collectLatest { collectState ->
                if (collectState == null) {
                    // 没登录，直接不展示 Bangumi 板块
                    update {
                        it.copy(
                            bangumiState = null
                        )
                    }
                } else {
                    val data = collectState.okOrCache()
                    when (collectState) {
                        is DataState.Ok -> {
                            val selection = data?.dataOrNull()?.type?.let { BangumiConst.getTypeDataById(it.toInt()) }
                            update {
                                it.copy(
                                    bangumiState = it.bangumiState?.copy(
                                        isError = false,
                                        isLoading = false,
                                        typeList = BangumiConst.collectTypeList,
                                        selection = selection,
                                    )?:BangumiState(
                                        isError = false,
                                        isLoading = false,
                                        typeList = BangumiConst.collectTypeList,
                                        selection = selection,
                                    ),
                                )
                            }
                        }
                        is DataState.Error -> {
                            update {
                                it.copy(
                                    bangumiState = it.bangumiState?.copy(
                                        isError = true,
                                        isLoading = false,
                                        errMsg = collectState.errorMsg,
                                        typeList = BangumiConst.collectTypeList,
                                        selection = null,
                                    ) ?: BangumiState(
                                        isError = true,
                                        isLoading = false,
                                        errMsg = collectState.errorMsg,
                                        typeList = BangumiConst.collectTypeList,
                                        selection = null,
                                    ),
                                )
                            }
                        }
                        is DataState.Loading -> {
                            update {
                                it.copy(
                                    bangumiState = it.bangumiState?.copy(
                                        isLoading = true,
                                        isError = false,
                                    ) ?: BangumiState(
                                        isLoading = true,
                                        isError = false,
                                    )
                                )
                            }
                        }
                        else -> {}
                    }
                }

            }
        }
    }

    fun onBangumiTypeChange(
        type: BangumiConst.BangumiCollectType
    ) {
        viewModelScope.launch {
            val provider = bgmUserDataProvider.value
            if (provider == null) {
                return@launch
            }
            update {
                it.copy(
                    bangumiState = it.bangumiState?.copy(
                        updatingSelection = type,
                    )
                )
            }
            val resp = bgmUserDataProvider.value?.changeBangumiCollectType(
                type, cartoonCover.toCartoonIndex()
            )
            if (resp?.isOk() == true) {
                update {
                    it.copy(
                        bangumiState = it.bangumiState?.copy(
                            updatingSelection = null,
                            selection = type,
                        )
                    )
                }
            } else {
                // 失败
                update {
                    it.copy(
                        bangumiState = it.bangumiState?.copy(
                            updateErrorMsg =  resp?.mapError { it.errorMsg },
                            updatingSelection = null,
                        )
                    )
                }
            }
        }

    }

    fun onLocalTagChange(
        tag: CartoonTag
    ) {
        viewModelScope.launch {
            update {
                it.copy(
                    localState = it.localState.copy(
                        selection = if (it.localState.selection.contains(tag)) {
                            it.localState.selection - tag
                        } else {
                            it.localState.selection + tag
                        }
                    )
                )
            }
            collectController.collectionFlow
        }
    }


}