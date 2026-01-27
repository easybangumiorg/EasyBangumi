package org.easybangumi.next.shared.cartoon.radar.v1

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.utils.safeCancel
import org.easybangumi.next.shared.foundation.view_model.StateViewModel
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
class CartoonRadarV1VM : StateViewModel<CartoonRadarV1VM.LogicState>(LogicState()) {

    private val sourceCase: SourceCase by inject()

    data class LogicState(
        val keyword: String? = null,
        val strategy: CartoonRadarStrategyV1? = null,
        val collectJob: Job? = null,
        val result: CartoonRadarStrategyV1.ResultState = CartoonRadarStrategyV1.ResultState(),
    )

    init {
        viewModelScope.launch {
            state.map { it.keyword }.distinctUntilChanged().collectLatest {
                if (it != null) {
                    search(it)
                }
            }
        }
    }

    private fun search(keyword: String) {
        update {
            it.collectJob?.safeCancel()
            it.strategy?.cancel()
            val strategy = CartoonRadarStrategyV1(
                keyword = keyword,
                sourceCase = sourceCase,
            )
            val job = viewModelScope.launch {
                strategy.start().collectLatest { resultState ->
                    update {
                        it.copy(
                            result = resultState
                        )
                    }
                }
            }
            it.copy(
                strategy = strategy,
                collectJob = job,
                result = CartoonRadarStrategyV1.ResultState(
                    searchingKeyword = keyword,
                    isSearching = true,
                )
            )

        }
    }

    fun changeKeyword(keyword: String?) {
        logger.info("CartoonRadarV1VM changeKeyword: $keyword")
        update {
            it.copy(keyword = keyword,)
        }
    }

}