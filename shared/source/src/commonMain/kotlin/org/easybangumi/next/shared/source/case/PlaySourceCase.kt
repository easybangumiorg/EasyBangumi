package org.easybangumi.next.shared.source.case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.core.source.SourceController

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

class PlaySourceCase(
    private val sourceController: SourceController
) {

    data class FindPlayBusinessResp(
        val businessList: List<ComponentBusiness<PlayComponent>>,
        val isLoading: Boolean,
    )

    suspend fun findPlayBusiness(): FindPlayBusinessResp {
        return playBusinessFlow().first()
    }

    fun playBusinessFlow(): Flow<FindPlayBusinessResp> {
        return sourceController.flow.map {
            val isLoading = it.isLoading
            val res = arrayListOf<ComponentBusiness<PlayComponent>>()
            it.sourceInfoList.filterIsInstance<SourceInfo.Loaded>().forEach {
                it.componentBundle.getBusiness(PlayComponent::class)?.let {
                    res.add(it)
                }
            }
            FindPlayBusinessResp(
                businessList = res,
                isLoading = isLoading,
            )
        }
    }

    data class FindSearchBusinessResp(
        val business: List<ComponentBusinessPair<SearchComponent, PlayComponent>>,
        val isLoading: Boolean,
    )

    suspend fun findSearchBusiness(): FindSearchBusinessResp {
        return searchBusinessWithPlayFlow().first()
    }

    fun searchBusinessWithPlayFlow(): Flow<FindSearchBusinessResp> {
        return sourceController.flow.map {
            val isLoading = it.isLoading
            val res = arrayListOf<ComponentBusinessPair<SearchComponent, PlayComponent>>()
            it.sourceInfoList.filterIsInstance<SourceInfo.Loaded>().forEach {
                val playComponent = it.componentBundle.getBusiness(PlayComponent::class)
                if (playComponent != null) {
                    it.componentBundle.getBusiness(SearchComponent::class)?.let {
                        res.add(it to playComponent)
                    }
                }

            }
            FindSearchBusinessResp(
                business = res,
                isLoading = isLoading,
            )
        }
    }



}