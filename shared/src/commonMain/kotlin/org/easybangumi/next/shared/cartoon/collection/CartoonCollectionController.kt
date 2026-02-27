package org.easybangumi.next.shared.cartoon.collection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.PagingFlow
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.data.CartoonInfoCase
import org.easybangumi.next.shared.data.bangumi.BangumiConst
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.cartoon.CartoonCover
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import org.easybangumi.next.shared.data.store.StoreProvider

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
 *  1. 本地收藏             from cartoonInfoDao
 *  2. bangumi 收藏        from bangumiCollectionController
 */
class CartoonCollectionController(
    private val cartoonInfoCase: CartoonInfoCase,
    private val bangumiCollectionController: CartoonBangumiCollectionController,
) {

    private val collectionTagFileHelper: JsonlFileHelper<CartoonTag> = StoreProvider.cartoonTag

    private val dispatcher = coroutineProvider.io()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    data class CollectionState(
        val tagList: List<CartoonTag> = emptyList(),
        val collectionDataMap: Map<CartoonTag, CollectionData> = emptyMap(),

        val cartoonInfo2LocalTag: Map<CartoonInfo, List<CartoonTag>> = emptyMap(),
        val bangumiCollectState: CartoonBangumiCollectionController.BangumiCollectionState = CartoonBangumiCollectionController.BangumiCollectionState()
    )


    val collectionFlow = combine(
        cartoonInfoCase.flowCollectionLocal().distinctUntilChanged(),
        collectionTagFileHelper.flow(),
        bangumiCollectionController.flow
    ) { cartoonInfoList, tagListRes, bangumiCollectionState ->
        val tagBuffer = tagListRes.toMutableList()

        fun ensureTag(label: String, type: String = CartoonTag.TYPE_LOCAL): CartoonTag {
            val exist = tagBuffer.firstOrNull { it.label == label }
            if (exist != null) {
                return exist
            }
            val created = CartoonTag.create(label, type)
            tagBuffer += created
            return created
        }

        // 确保默认分类和 Bangumi 分类存在
        ensureTag(CartoonTag.DEFAULT_TAG_LABEL)
        if (bangumiCollectionState.isLogin) {
            ensureTag(CartoonTag.BANGUMI_TAG_LABEL, CartoonTag.TYPE_BANGUMI)
        } else {
            // 移除 Bangumi 分类
            tagBuffer.removeAll { it.label == CartoonTag.BANGUMI_TAG_LABEL }
        }


        // 确保所有 CartoonInfo 使用到的标签都存在
        cartoonInfoList.forEach { info ->
            if (info.tagList.isEmpty()) {
                ensureTag(CartoonTag.DEFAULT_TAG_LABEL)
            } else {
                info.tagList.forEach { label ->
                    ensureTag(label)
                }
            }
        }

        val tabList = tagBuffer
            .distinctBy { it.label }
            .sortedWith(compareBy<CartoonTag> { it.order }.thenBy { it.label })

        val label2Tag = tabList.associateBy { it.label }
        val localTag2Cartoon = HashMap<CartoonTag, MutableList<CartoonInfo>>()
        val cartoon2LocalTag = HashMap<CartoonInfo, MutableList<CartoonTag>>()

        fun appendToTag(tagLabel: String, info: CartoonInfo) {
            val tag = label2Tag[tagLabel] ?: return
            if (tag.label == CartoonTag.BANGUMI_TAG_LABEL) {
                return
            }
            localTag2Cartoon.getOrPut(tag) { mutableListOf() }.add(info)
            cartoon2LocalTag.getOrPut(info) { mutableListOf() }.add(tag)
        }

        cartoonInfoList.forEach { info ->
            val labels = if (info.tagList.isEmpty()) {
                listOf(CartoonTag.DEFAULT_TAG_LABEL)
            } else {
                info.tagList
            }
            labels.forEach { appendToTag(it, info) }
        }

        val bangumiData = CollectionData.BangumiCollection(
            typeList = BangumiConst.collectTypeList,
            type2Collect = bangumiCollectionState.type2Collect
        )

        val collectionDataMap = tabList.associateWith { tag ->
            if (tag.label == CartoonTag.BANGUMI_TAG_LABEL) {
                bangumiData
            } else {
                CollectionData.LocalCollection(
                    localTag2Cartoon[tag].orEmpty()
                )
            }
        }

        CollectionState(
            tagList = tabList,
            collectionDataMap = collectionDataMap,
            cartoonInfo2LocalTag = cartoon2LocalTag,
            bangumiCollectState = bangumiCollectionState
        )
    }.stateIn(scope, SharingStarted.Lazily, CollectionState())



    sealed class CollectionData {
        data class LocalCollection(
            val cartoonInfoList: List<CartoonInfo>,
        ) : CollectionData()

        // Bangumi 为特殊页面，分页加载 + 二级收藏类型
        data class BangumiCollection(
            val typeList: List<BangumiConst.BangumiCollectType>,
            val type2Collect: Map<BangumiConst.BangumiCollectType, PagingFlow<BgmCollect>>
        ): CollectionData()

        fun localOrNull(): List<CartoonInfo>? {
            return when (this) {
                is LocalCollection -> this.cartoonInfoList
                else -> null
            }
        }
    }

    fun refreshBangumiCollectionIfNeed() {
        bangumiCollectionController.refreshIfNeed()
    }

    suspend fun changeCartoonTag(cartoonCover: CartoonCover, newTagSet: Set<CartoonTag>?) {
        cartoonInfoCase.changeCartoonInfoTag(cartoonCover, newTagSet)
    }

    suspend fun removeCartoonCoverCollection(cartoonCoverList: List<CartoonCover>) {
        cartoonInfoCase.removeCartoonCoverCollection(cartoonCoverList)
    }

    suspend fun removeCartoonCollection(cartoonInfoList: List<CartoonInfo>) {
        cartoonInfoCase.removeCartoonInfoCollection(cartoonInfoList)
    }

    // ========== Tag CRUD（供 TagsManager 使用）==========

    suspend fun addTag(label: String) {
        val current = collectionTagFileHelper.get()
        if (current.any { it.label == label }) return
        val newTag = CartoonTag.create(label)
        collectionTagFileHelper.setAndWait(current + newTag)
    }

    suspend fun removeTag(tag: CartoonTag) {
        val current = collectionTagFileHelper.get()
        collectionTagFileHelper.setAndWait(current.filter { it.label != tag.label })
    }

    suspend fun renameTag(tag: CartoonTag, newLabel: String) {
        val current = collectionTagFileHelper.get()
        if (current.any { it.label != tag.label && it.label == newLabel }) return
        val updated = current.map {
            if (it.label == tag.label) it.copy(label = newLabel) else it
        }
        collectionTagFileHelper.setAndWait(updated)
    }

    suspend fun setTagOrder(tagList: List<CartoonTag>) {
        val reordered = tagList.mapIndexed { index, tag -> tag.copy(order = index) }
        collectionTagFileHelper.setAndWait(reordered)
    }

    suspend fun setTagShow(tag: CartoonTag, show: Boolean) {
        val current = collectionTagFileHelper.get()
        var needAppend = true
        val updated = current.map {
            if (it.label == tag.label) it.copy(show = show).apply {
                needAppend = false
            } else it
        }
        if (needAppend) {
            collectionTagFileHelper.setAndWait(updated + tag)
        } else {
            collectionTagFileHelper.setAndWait(updated)
        }

    }

}