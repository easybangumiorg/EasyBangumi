package org.easybangumi.next.shared.cartoon.collection

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.data.CartoonInfoCase
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
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
class BgmCollectInfoVM(
    private val cartoonIndex: CartoonIndex,
): StateViewModel<BgmCollectInfoVM.State>(State()) {

    data class State (
        val loading: Boolean = true,
        val hasBgmAccountInfo: Boolean = false,
        val collectionState: DataState<BgmCollectResp> = DataState.none(),

        val cartoonInfo: CartoonInfo? = null,
    )

    val cartoonInfoCase: CartoonInfoCase by inject()
    val bangumiCase: BangumiCase by inject()
    val sourceCase: SourceCase by inject()
    val bgmUserDataProvider = bangumiCase.flowUserDataProvider()


    init {
        // bangumi 登录态相关数据流绑定
        viewModelScope.launch {
            bgmUserDataProvider
                .flatMapLatest { provider ->
                    // provider 为空代表没有登录，返回空的收藏状态
                    val collectFlow = provider?.getCollectRepository(cartoonIndex)?.apply {
                        refreshIfNoneOrCache()
                    }?.flow
                        ?: flowOf(DataState.none<BgmCollectResp>())
                    collectFlow.map { collectDataState ->
                        provider to collectDataState
                    }
                }
                .collectLatest { (provider, collectDataState) ->
                    update {
                        it.copy(
                            // provider 为空代表没有登录，直接隐藏 bgm 收藏按钮
                            hasBgmAccountInfo = provider == null,
                            collectionState = collectDataState,
                        )
                    }
                }
        }

        // 本地收藏状态
        viewModelScope.launch {
            cartoonInfoCase.flowById(cartoonIndex.source, cartoonIndex.id).collectLatest { info ->
                update {
                    it.copy(
                        cartoonInfo = info,
                    )
                }
            }
        }
    }
}