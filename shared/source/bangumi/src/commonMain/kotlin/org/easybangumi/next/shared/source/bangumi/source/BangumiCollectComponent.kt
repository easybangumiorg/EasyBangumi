package org.easybangumi.next.shared.source.bangumi.source

import kotlinx.coroutines.async
import kotlinx.datetime.Clock
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.bangumi.BgmCollect
import org.easybangumi.next.shared.data.bangumi.BgmCollectResp
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.collect.CollectComponent
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.model.BgmRsp
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
class BangumiCollectComponent: CollectComponent, BaseComponent() {

    private val api: BangumiApi by inject()

    // 404 表示没有收藏，也是正常返回的一种，这里做特殊处理
    suspend fun getCollection(
        username: String,
        token: String,
        cartoonIndex: CartoonIndex,
    ): DataState<BgmCollectResp> {
        checkCartoonIndex(cartoonIndex)
        return source.scope.async {
            val resp = api.getCollect(username, token, cartoonIndex.id).await()
            if (resp is BgmRsp.Error<BgmCollect> && resp.code == 404) {
                return@async DataState.ok<BgmCollectResp>(BgmCollectResp.BgmCollectNone)
            } else {
                return@async  resp.toDataState().map<BgmCollect, BgmCollectResp> { BgmCollectResp.BgmCollectData(it) }
            }
        }.await()
    }

    class CollectionsPagingSource(
        private val bangumiApi: BangumiApi,
        private val username: String,
        private val token: String,
        private val type: Int,
    ) : EasyPagingSource<BgmCollect> {
        override val initKey: String = "1"

        override suspend fun load(key: String): DataState<PagingFrame<BgmCollect>> {
            val page = key.toIntOrNull() ?: 1
            val rsp = bangumiApi.getCollectList(username, token, type, (page-1)*100, 100).await()
            return rsp.toDataState().map {
                var nextKey: String? = (page + 1).toString()
                val total = it.total ?: 0L
                val offset = it.offset ?: ((page - 1) * 100L)
                if (total <= (offset + it.data.size) || it.data.isEmpty()) {
                    nextKey = null
                }

                PagingFrame(nextKey, it.data)
            }
        }
    }

    fun createCollectionsPagingSource(
        username: String,
        token: String,
        type: Int,
    ): CollectionsPagingSource {
        return CollectionsPagingSource(api, username, token, type)
    }

    private fun checkCartoonIndex(cartoonIndex: CartoonIndex) {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_KEY) {
            throw IllegalArgumentException("BangumiDetailComponent only supports Bangumi CartoonIndex")
        }
    }

    suspend fun changeCollectType(
        username: String,
        token: String,
        type: String,
        subjectId: String,
    ): DataState<BgmRsp<String?>> {
        return source.scope.async {api.changeCollectType(
            username = username,
            token = token,
            subjectId = subjectId,
            type = type.toIntOrNull() ?: 0,
        ).await().wrapDataState()}.await()
    }


}