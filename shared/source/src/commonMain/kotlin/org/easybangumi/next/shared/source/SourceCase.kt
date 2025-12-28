package org.easybangumi.next.shared.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.easybangumi.next.shared.source.api.component.ComponentBusiness
import org.easybangumi.next.shared.source.api.component.ComponentBusinessPair
import org.easybangumi.next.shared.source.api.component.collect.CollectComponent
import org.easybangumi.next.shared.source.api.component.detail.DetailComponent
import org.easybangumi.next.shared.source.api.component.discover.DiscoverComponent
import org.easybangumi.next.shared.source.api.component.play.PlayComponent
import org.easybangumi.next.shared.source.api.component.search.SearchComponent
import org.easybangumi.next.shared.source.api.source.SourceInfo
import org.easybangumi.next.shared.source.api.source.SourceManifest
import org.easybangumi.next.shared.source.bangumi.source.BangumiCollectComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiDetailComponent
import org.easybangumi.next.shared.source.bangumi.source.BangumiDiscoverComponent
import org.easybangumi.next.shared.source.core.inner.InnerSourceProvider
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
class SourceCase(
    private val sourceController: SourceController,
    private val innerSourceProvider: InnerSourceProvider,
) {

    // ======== Bangumi ============================

    fun getBangumiDetailBusiness(): ComponentBusiness<BangumiDetailComponent> {
        return (innerSourceProvider.bangumiComponentBundle.getBusiness(
            DetailComponent::class,
        ) as? ComponentBusiness<BangumiDetailComponent>) ?: throw IllegalStateException("BangumiDetailComponent not found")
    }

    fun getBangumiCollectBusiness(): ComponentBusiness<BangumiCollectComponent> {
        return (innerSourceProvider.bangumiComponentBundle.getBusiness(
            CollectComponent::class,
        ) as? ComponentBusiness<BangumiCollectComponent>) ?: throw IllegalStateException("BangumiCollectComponent not found")
    }

    fun getBangumiDiscoverBusiness(): ComponentBusiness<BangumiDiscoverComponent> {
        return innerSourceProvider.bangumiComponentBundle.getBusiness(DiscoverComponent::class) as? ComponentBusiness<BangumiDiscoverComponent>
            ?: throw IllegalStateException("BangumiDiscoverComponent not found")
    }

    fun playComponentFlow(
        sourceKey: String,
    ): Flow<ComponentBusiness<PlayComponent>?> {
        return sourceController.flow.map {
            var res: ComponentBusiness<PlayComponent>? = null
            it.sourceInfoList.filterIsInstance<SourceInfo.Loaded>().forEach {
                if (it.manifest.key == sourceKey) {
                    res = it.componentBundle.getBusiness(PlayComponent::class)
                }
            }
            res
        }
    }


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

    fun searchBusiness(): Flow<List<ComponentBusiness<SearchComponent>>> {
        return sourceController.flow.map {
            val res = arrayListOf<ComponentBusiness<SearchComponent>>()
            it.sourceInfoList.filterIsInstance<SourceInfo.Loaded>().forEach {
                it.componentBundle.getBusiness(SearchComponent::class)?.let {
                    res.add(it)
                }
            }
            res
        }
    }

    fun sourceManifestFlow(): Flow<List<SourceManifest>> {
        return sourceController.flow.map {
            val res = arrayListOf<SourceManifest>()
            it.sourceInfoList.forEach {
                res.add(it.manifest)
            }
            res
        }
    }


}