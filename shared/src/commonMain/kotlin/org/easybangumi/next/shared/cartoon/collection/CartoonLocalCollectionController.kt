package org.easybangumi.next.shared.cartoon.collection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.data.cartoon.FilterState
import org.easybangumi.next.shared.data.room.cartoon.dao.CartoonInfoDao

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
class CartoonLocalCollectionController(
    private val cartoonInfoDao: CartoonInfoDao,
    private val collectionTagFileHelper: JsonlFileHelper<CartoonTag>,
) {

    private val dispatcher = coroutineProvider.newSingle()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // 这里只有 type 为 local 的 tag
    data class LocalCollectionState(
        val tagList: List<CartoonTag> = emptyList(),
        val tag2Cartoon: Map<CartoonTag, List<CartoonInfo>> = emptyMap(),
        val cartoonInfoList : List<CartoonInfo> = emptyList()
    )

    // 1. 保证有内部 tag （全部，更新），如果 tagList 里没有会补充
    // 2. 保证所有 cartoonInfo 里的 tag 都有对应的 CartoonTag，如果 tagList 里没有会补充
    val cartoonTagFlow = combine(
        cartoonInfoDao.flowCollectionLocal().distinctUntilChanged(),
        collectionTagFileHelper.flow().map { it.filter { it.isLocal } }.distinctUntilChanged()
    ) { cartoonInfoList, tagListRes ->
        tagListRes
            .process(cartoonInfoList)
    }.stateIn(scope, SharingStarted.Lazily, LocalCollectionState())

    suspend fun remove(cartoonTag: CartoonTag) {
        if (cartoonTag.isInner) {
            return
        }
        collectionTagFileHelper.update {
            it.filter { it.label != cartoonTag.label }
        }
    }

    suspend fun insert(label: String) {
        collectionTagFileHelper.update {
            it + CartoonTag.create(label)
        }
    }

    suspend fun modifier(cartoonTag: CartoonTag) {
        collectionTagFileHelper.update {
            var i = it
            if(!it.any { it.label == cartoonTag.label }) {
                i = it + cartoonTag
            }
            if (cartoonTag.isCustomSetting)
                i.map {
                    if (it.label == cartoonTag.label) {
                        cartoonTag
                    } else {
                        it
                    }
                }
            else {
                i.map {
                    when (it.label) {
                        CartoonTag.ALL_TAG_LABEL -> {
                            it.copy(
                                sortId = cartoonTag.sortId,
                                isReverse = cartoonTag.isReverse,
                                filterState = cartoonTag.filterState
                            )
                        }
                        cartoonTag.label -> {
                            cartoonTag
                        }
                        else -> {
                            it
                        }
                    }
                }

            }
        }
    }
    suspend fun modifier(cartoonTag: List<CartoonTag>) {
        collectionTagFileHelper.setAndWait(cartoonTag)
    }

    private fun List<CartoonTag>.process(
        cartoonInfoList: List<CartoonInfo>
    ) : LocalCollectionState {

        val oriInnerTag = CartoonTag.innerLabel
        val innerTag = oriInnerTag.toMutableSet()
        // 确保 tag 里有内置 tag 和所有 cartoonInfo 中的 tag
        val label2Tag = HashMap<String, CartoonTag>()

        for (cartoonTag in this) {
            if (innerTag.contains(cartoonTag.label)) {
                innerTag.remove(cartoonTag.label)
            } else if (cartoonTag.isInner) {
                continue
            }
            label2Tag[cartoonTag.label] = cartoonTag
        }
        // 补充缺少的内部 tag
        innerTag.forEach {
            label2Tag[it] = CartoonTag.create(it)
        }

        cartoonInfoList.forEach {
            it.tagList.forEach { tag ->
                if (!label2Tag.containsKey(tag)) {
                    label2Tag[tag] = CartoonTag.create(tag)
                }
            }
        }

        val defaultTag = label2Tag[CartoonTag.DEFAULT_TAG_LABEL] ?: CartoonTag.create(CartoonTag.DEFAULT_TAG_LABEL)
        val allTag = label2Tag[CartoonTag.ALL_TAG_LABEL] ?: CartoonTag.create(CartoonTag.ALL_TAG_LABEL)

        // 打包
        val pack = HashMap<CartoonTag, MutableList<CartoonInfo>>()

        for (entry in label2Tag.entries) {
            pack[entry.value] = arrayListOf()
        }


        for (cartoonInfo in cartoonInfoList) {
            for (tag in cartoonInfo.tagList) {
                // 内部 tag 额外处理
                if (oriInnerTag.contains(tag)) {
                    continue
                }
                val cartoonTag = label2Tag[tag]
                if (cartoonTag != null) {
                    pack[cartoonTag]?.add(cartoonInfo)
                }
            }

            // 内部 tag 处理

            // 全部
            pack[allTag]?.add(cartoonInfo)

            // 默认
            if (cartoonInfo.tagList.isEmpty()) {
                pack[defaultTag]?.add(cartoonInfo)
            }


        }

        val res = HashMap<CartoonTag, List<CartoonInfo>>()

        // 打包完毕，后面是排序，过滤，置顶

        for (entry in pack.entries) {

            val pinList = arrayListOf<CartoonInfo>()
            val normalList = arrayListOf<CartoonInfo>()

            val tag = label2Tag[entry.key.label] ?: continue

            val sortId = if (tag.isCustomSetting) tag.sortId else allTag.sortId
            val currentSort = CartoonInfoSortFilterConst.sortByList.firstOrNull() {
                it.id == sortId
            } ?: CartoonInfoSortFilterConst.sortByStarTime

            val isSortReverse = if (tag.isCustomSetting) tag.isReverse else allTag.isReverse

            val filterState = if (tag.isCustomSetting) tag.filterState else allTag.filterState

            val onFilter = CartoonInfoSortFilterConst.filterWithList.filter {
                filterState[it.id] == FilterState.STATUS_ON
            }
            val excludeFilter = CartoonInfoSortFilterConst.filterWithList.filter {
                filterState[it.id] == FilterState.STATUS_EXCLUDE
            }

            for (cartoonInfo in entry.value) {
                var check = true
                for (filterWith in onFilter) {
                    if (!filterWith.filter(cartoonInfo)) {
                        check = false
                        break
                    }
                }
                if (!check) {
                    continue
                }
                for (filterWith in excludeFilter) {
                    if (filterWith.filter(cartoonInfo)) {
                        check = false
                        break
                    }
                }
                if (!check) {
                    continue
                }

                if (cartoonInfo.isPin()){
                    pinList.add(cartoonInfo)
                } else {
                    normalList.add(cartoonInfo)
                }
            }

            pinList.sortBy {
                it.pinTime
            }

            normalList.sortWith { o1, o2 ->
                val r = currentSort.comparator.compare(o1, o2)
                if (isSortReverse) -r else r
            }

            res[entry.key] = pinList + normalList
        }
        return LocalCollectionState(
            tagList = label2Tag.values.toList().map {
                if (!it.isInner && !it.isCustomSetting) {
                    it.copy(
                        sortId = allTag.sortId,
                        isReverse = allTag.isReverse,
                        filterState = allTag.filterState
                    )
                } else {
                    it
                }
            }.sortedBy { it.order },
            tag2Cartoon = res,
            cartoonInfoList = cartoonInfoList,
        )
    }

}