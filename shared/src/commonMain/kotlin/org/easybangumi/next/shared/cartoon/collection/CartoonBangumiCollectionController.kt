package org.easybangumi.next.shared.cartoon.collection

import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.newPagingFlow
import org.easybangumi.next.lib.utils.safeCancel
import org.easybangumi.next.shared.case.BangumiCase
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.source.bangumi.source.BangumiCollectComponent
import kotlin.reflect.typeOf

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
class CartoonBangumiCollectionController(
    private val detailCase: BangumiCase,
) {

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    data class BangumiCollectionState(
        val isLoading: Boolean = true,
        val isLogin: Boolean = false,
        val pagingSource: Map<BangumiConst.BangumiCollectType, BangumiCollectComponent.CollectionsPagingSource> = mapOf(),
        val type2Collect: Map<BangumiConst.BangumiCollectType, PagingFlow<BgmCollect>> = emptyMap(),
        val type2Scope: Map<BangumiConst.BangumiCollectType, CoroutineScope> = emptyMap()
    )
    private val _flow = MutableStateFlow<BangumiCollectionState>(BangumiCollectionState())
    val flow = _flow.asStateFlow()

    init {
        // 监听用户登录状态，维护 PagingSource 和 PagingFlow
        scope.launch {
            detailCase.flowUserDataProvider().collectLatest { provider ->
                // 先取消之前的 scope，清理旧的 flow 数据
                _flow.value.type2Scope.values.forEach { it.safeCancel() }

                if (provider == null) {
                    // 未登录，清空所有状态
                    _flow.update { BangumiCollectionState(
                        isLoading = false,
                        isLogin = false
                    ) }
                } else {
                    // 已登录，为每个类型创建 PagingSource 和 PagingFlow
                    val pagingSourceMap = mutableMapOf<BangumiConst.BangumiCollectType, BangumiCollectComponent.CollectionsPagingSource>()
                    val type2CollectMap = mutableMapOf<BangumiConst.BangumiCollectType, PagingFlow<BgmCollect>>()
                    val type2ScopeMap = mutableMapOf<BangumiConst.BangumiCollectType, CoroutineScope>()

                    BangumiConst.collectTypeList.forEach { type ->
                        val pagingSource = provider.getCollectPagingSource(type)
                        // 为每个类型创建独立的子 scope
                        val childScope = CoroutineScope(SupervisorJob() + dispatcher)
                        val pagingFlow = pagingSource.newPagingFlow().cachedIn(childScope)
                        pagingSourceMap[type] = pagingSource
                        type2CollectMap[type] = pagingFlow
                        type2ScopeMap[type] = childScope
                    }

                    _flow.update {
                        BangumiCollectionState(
                            isLoading = false,
                            isLogin = true,
                            pagingSource = pagingSourceMap,
                            type2Collect = type2CollectMap,
                            type2Scope = type2ScopeMap
                        )
                    }
                }
            }
        }
    }

    /**
     * 刷新所有 PagingFlow
     * 使用当前的 PagingSource 重新创建 PagingFlow
     * 会取消之前的 scope，确保旧的 flow 数据被清理
     */
    fun refresh() {
        val currentState = _flow.value
        if (currentState.pagingSource.isEmpty()) {
            return
        }

        scope.launch {
            // 取消之前的 scope，清理旧的 flow 数据
            currentState.type2Scope.values.forEach { it.safeCancel() }

            val type2CollectMap = mutableMapOf<BangumiConst.BangumiCollectType, PagingFlow<BgmCollect>>()
            val type2ScopeMap = mutableMapOf<BangumiConst.BangumiCollectType, CoroutineScope>()

            currentState.pagingSource.forEach { (type, pagingSource) ->
                // 为每个类型创建新的子 scope
                val childScope = CoroutineScope(SupervisorJob() + dispatcher)
                val pagingFlow = pagingSource.newPagingFlow().cachedIn(childScope)
                type2CollectMap[type] = pagingFlow
                type2ScopeMap[type] = childScope
            }

            _flow.update {
                it.copy(
                    isLoading = false,
                    type2Collect = type2CollectMap,
                    type2Scope = type2ScopeMap
                )
            }
        }

    }






}