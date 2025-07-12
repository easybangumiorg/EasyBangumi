package org.easybangumi.next.shared.source.bangumi.component

import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.source.api.component.BaseComponent
import org.easybangumi.next.shared.source.api.component.detail.DetailComponent
import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.model.Character
import org.easybangumi.next.shared.source.bangumi.model.Episode
import org.easybangumi.next.shared.source.bangumi.model.Person
import org.easybangumi.next.shared.source.bangumi.model.Reviews
import org.easybangumi.next.shared.source.bangumi.model.Subject
import org.koin.core.component.inject
import kotlin.Pair
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

class BangumiDetailComponent: DetailComponent, BaseComponent() {


    private val api: BangumiApi by inject()


    suspend fun getSubject(
        cartoonIndex: CartoonIndex,
    ): DataState<Subject> {
        checkCartoonIndex(cartoonIndex)
        return api.getSubject(cartoonIndex.id).await().toDataState()
    }

    suspend fun getCharacter(
        cartoonIndex: CartoonIndex,
    ): DataState<List<Character>> {
        checkCartoonIndex(cartoonIndex)
        return api.getCharacterList(cartoonIndex.id).await().toDataState()
    }

    suspend fun getPerson(
        cartoonIndex: CartoonIndex,
    ): DataState<List<Person>> {
        checkCartoonIndex(cartoonIndex)
        return api.getPersonList(cartoonIndex.id).await().toDataState()
    }


    class EpisodeListPagingSource(
        private val bangumiApi: BangumiApi,
        private val cartoonIndex: CartoonIndex,
    ) : EasyPagingSource<Episode> {
        override val initKey: String = "1"

        override suspend fun load(key: String): DataState<PagingFrame<Episode>> {
            val page = key.toIntOrNull() ?: 1
            val rsp = bangumiApi.getEpisodeList(cartoonIndex.id, (page-1)*100, 100).await()
            return rsp.toDataState().map {
                val nextKey = if (it.data.isEmpty()) null else (page + 1).toString()
                PagingFrame(nextKey, it.data)
            }
        }
    }

    fun createEpisodePagingSource(
        cartoonIndex: CartoonIndex,
    ): EpisodeListPagingSource {
        checkCartoonIndex(cartoonIndex)
        return EpisodeListPagingSource(api, cartoonIndex)
    }

    class CommentListPagingSource(
        private val bangumiApi: BangumiApi,
        private val cartoonIndex: CartoonIndex,
    ) : EasyPagingSource<Reviews> {
        override val initKey: String = "1"

        override suspend fun load(key: String): DataState<PagingFrame<Reviews>> {
            val page = key.toIntOrNull() ?: 1
            val rsp = bangumiApi.getComments(cartoonIndex.id, page).await()
            return rsp.toDataState().map {
                val nextKey = if (it.isEmpty()) null else (page + 1).toString()
                PagingFrame(nextKey, it)
            }
        }
    }

    fun createCommentPagingSource(
        cartoonIndex: CartoonIndex,
    ): CommentListPagingSource {
        checkCartoonIndex(cartoonIndex)
        return CommentListPagingSource(api, cartoonIndex)
    }

    fun coverUrl(
        cartoonIndex: CartoonIndex,
        type: String = "common",
    ): String {
        checkCartoonIndex(cartoonIndex)
        return api.coverUrl(cartoonIndex.id, type)
    }

    private fun checkCartoonIndex(cartoonIndex: CartoonIndex) {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_KEY) {
            throw IllegalArgumentException("BangumiDetailComponent only supports Bangumi CartoonIndex")
        }
    }

}