package org.easybangumi.next.shared.cartoon.radar.v1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.lib.utils.safeCancel
import org.easybangumi.next.lib.utils.safeMutableMapOf
import org.easybangumi.next.shared.cartoon.radar.CartoonRadarStrategy
import org.easybangumi.next.shared.cartoon.radar.editDistance
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.snackbar.moeSnackBar
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.FinderComponentPair
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheck

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
 *    关键词搜索 + 播放线路搜索
 *    1. 搜索一页番剧并进行名称编辑距离排序
 *    2. 对前 N 个结果进行播放器线路搜索
 */
class CartoonRadarStrategyV1(
    private val keyword: String,
    // 每个源搜索后前 N 个结果搜索播放器线路
    private val playerLineSearchLimitPreSource: Int = 3,
    private val sourceCase: SourceCase,
): CartoonRadarStrategy<CartoonRadarStrategyV1.ResultState> {


    data class ResultState(
        val searchingKeyword: String = "",
        val isSearching: Boolean = false,
        val sourceSearchResMap : Map<FinderComponentPair, DataState<List<CartoonCoverResult>>> = emptyMap(),
    )

    data class CartoonCoverResult(
        val cover: CartoonCover,
        val businessPair: FinderComponentPair,
        // 名称编辑距离
        val nameDistance: Int,
        // 前 N 个结果才会进行
        val playerLine: List<PlayerLine>? = null,
    )



    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun start(): Flow<ResultState> {
        return channelFlow {
            this.channel.send(ResultState(searchingKeyword = keyword, isSearching = true))

            val sourceList = sourceCase.searchBusinessWithPlayFlow().first { !it.isLoading }
            val componentPairList = sourceList.business
            if (componentPairList.isEmpty()) {
                this.channel.send(
                    ResultState(
                        isSearching = false,
                        searchingKeyword = keyword,
                        sourceSearchResMap = emptyMap()
                    )
                )
                return@channelFlow
            }

            // 开始搜索
            val sourceSearchResMap: MutableMap<FinderComponentPair, DataState<List<CartoonCoverResult>>> =
                safeMutableMapOf()

            componentPairList.forEach {
                sourceSearchResMap[it] = DataState.Loading()
            }
            this.channel.send(ResultState(searchingKeyword = keyword, isSearching = true))

            componentPairList.map { pair ->
                scope.async {
                    val result = pair.first.run {
                        search(keyword, firstKey())
                    }.map {
                        it.second.map {
                            val distance = it.name.editDistance(keyword)
                            CartoonCoverResult(
                                cover = it,
                                businessPair = pair,
                                nameDistance = distance
                            )
                        }.sortedBy { it.nameDistance }
                            .mapIndexed { index, result ->
                                if (index >= playerLineSearchLimitPreSource) {
                                    result
                                } else {
                                    val lineList = pair.second.run {
                                        getPlayLines(result.cover.toCartoonIndex())
                                    }.okOrNull()
                                    result.copy(playerLine = lineList)
                                }
                            }
                    }
                    result.mapError {
                        if (it.throwable is NeedWebViewCheck) {
                            "${pair.first.source.manifest.label} 需要手动验证，功能开发中……".moeSnackBar()
                        }
                    }
                    sourceSearchResMap[pair] = result
                    this@channelFlow.channel.send(
                        ResultState(
                            isSearching = true,
                            searchingKeyword = keyword,
                            sourceSearchResMap = sourceSearchResMap.toMap()
                        )
                    )
                }
            }.awaitAll()

            this.channel.send(
                ResultState(
                    isSearching = false,
                    searchingKeyword = keyword,
                    sourceSearchResMap = sourceSearchResMap.toMap()
                )
            )

        }
    }

    override fun cancel() {
        scope.safeCancel()
    }





}