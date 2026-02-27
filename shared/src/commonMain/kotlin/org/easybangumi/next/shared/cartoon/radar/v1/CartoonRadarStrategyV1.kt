package org.easybangumi.next.shared.cartoon.radar.v1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.lib.utils.safeCancel
import org.easybangumi.next.lib.utils.safeMutableMapOf
import org.easybangumi.next.lib.webview.IWebView
import org.easybangumi.next.shared.cartoon.radar.CartoonRadarStrategy
import org.easybangumi.next.shared.cartoon.radar.editDistance
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.PlayerLine
import org.easybangumi.next.shared.foundation.snackbar.moeSnackBar
import org.easybangumi.next.shared.source.SourceCase
import org.easybangumi.next.shared.source.api.component.FinderComponentPair
import org.easybangumi.next.shared.source.api.component.NeedWebViewCheckException
import org.easybangumi.next.shared.source.api.component.WebViewCheckParam

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
    val iWebProvider: ((Pair<String, FinderComponentPair>) -> IWebView?)? = null
) : CartoonRadarStrategy<CartoonRadarStrategyV1.ResultState> {

    data class ResultState(
        val searchingKeyword: String = "",
        val sourceSearchResMap: Map<FinderComponentPair, DataState<List<CartoonCoverResult>>> = emptyMap(),
    )

    data class CartoonCoverResult(
        val cover: CartoonCover,
        val businessPair: FinderComponentPair,
        val checkParam: WebViewCheckParam? = null,
        // 名称编辑距离
        val nameDistance: Int,
        // 前 N 个结果才会进行
        val playerLine: List<PlayerLine>? = null,
    )

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _resultStateFlow = MutableStateFlow(
        ResultState(searchingKeyword = keyword)
    )
    override val resultStateFlow: StateFlow<ResultState> = _resultStateFlow.asStateFlow()

    private val sourceJobMap: MutableMap<FinderComponentPair, Job> = safeMutableMapOf()

    override fun start() {
        scope.launch(dispatcher) {
            _resultStateFlow.value = ResultState(
                searchingKeyword = keyword,
                sourceSearchResMap = emptyMap()
            )

            val sourceList = sourceCase.searchBusinessWithPlayFlow().first { !it.isLoading }
            val componentPairList = sourceList.business
            if (componentPairList.isEmpty()) {
                _resultStateFlow.value = ResultState(
                    searchingKeyword = keyword,
                    sourceSearchResMap = emptyMap()
                )
                return@launch
            }

            sourceJobMap.values.forEach { it.cancel() }
            sourceJobMap.clear()

            _resultStateFlow.update {
                it.copy(
                    sourceSearchResMap = emptyMap(),
                )
            }

            componentPairList.forEach { pair ->
                searchSource(pair)
            }
        }
    }

    private fun searchSource(pair: FinderComponentPair) {
        sourceJobMap[pair]?.cancel()
        val job = scope.launch(dispatcher) {
            val result = pair.first.run(allowRetry = false) {
                val iWebView = iWebProvider?.invoke(
                    keyword to pair
                )
                if (iWebView == null) {
                    search(keyword, firstKey())
                } else {
                    searchWithCheck(keyword, firstKey(), iWebView)
                }

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
                if (it.throwable is NeedWebViewCheckException) {
                    "${pair.first.source.manifest.label} 需要手动验证，功能开发中……".moeSnackBar()
                }
            }
            _resultStateFlow.update {
                it.copy(
                    sourceSearchResMap = it.sourceSearchResMap.toMutableMap().apply {
                        this[pair] = result
                    }
                )
            }

        }
        sourceJobMap[pair] = job
    }


    fun retrySource(pair: FinderComponentPair) {
        searchSource(pair)
    }

    fun refreshAll() {
        scope.launch(dispatcher) {
            sourceJobMap.values.forEach { it.cancel() }
            sourceJobMap.clear()
            start()
        }
    }

    override fun cancel() {
        scope.safeCancel()
        sourceJobMap.clear()
    }
}
