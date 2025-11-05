package org.easybangumi.next.shared.cartoon.collection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.cartoon.collection.CartoonLocalCollectionController.LocalCollectionState
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag

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
 *  1. 本地收藏
 *  2. Bangumi 收藏（待定）
 */
class CartoonCollectionController(
    private val localCollectionController: CartoonLocalCollectionController,
) {

    private val dispatcher = coroutineProvider.newSingle()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)


    data class CollectionState(
        val tagList: List<CartoonTag> = emptyList(),
        val collectionDataMap: Map<CartoonTag, CollectionData> = emptyMap(),
    )
    val flow = localCollectionController.cartoonTagFlow.map { localState ->
        val tagList = localState.tagList
        val collectionDataMap = mutableMapOf<CartoonTag, CollectionData>()
        tagList.forEach { tag ->
            val cartoonInfoList = localState.tag2Cartoon[tag] ?: emptyList()
            collectionDataMap[tag] = CollectionData.LocalCollection(cartoonInfoList)
        }
        CollectionState(
            tagList = tagList,
            collectionDataMap = collectionDataMap,
        )
    }.stateIn(scope, SharingStarted.Lazily, CollectionState())


    sealed class CollectionData {
        data class LocalCollection(
            val cartoonInfoList: List<CartoonInfo>,
        ) : CollectionData()

        data class BangumiCollection(
            val type: String,
        ) : CollectionData()

        fun localOrNull(): List<CartoonInfo>? {
            return when (this) {
                is LocalCollection -> this.cartoonInfoList
                else -> null
            }
        }
    }


}