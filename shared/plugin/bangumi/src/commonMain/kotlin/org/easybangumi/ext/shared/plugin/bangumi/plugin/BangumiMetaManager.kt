package org.easybangumi.ext.shared.plugin.bangumi.plugin

import org.easybangumi.next.shared.source.bangumi.business.BangumiApi
import org.easybangumi.next.shared.source.bangumi.model.Character
import org.easybangumi.next.shared.source.bangumi.model.Episode
import org.easybangumi.next.shared.source.bangumi.model.Person
import org.easybangumi.next.shared.source.bangumi.model.Reviews
import org.easybangumi.next.shared.source.bangumi.model.Subject
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.lib.utils.EasyPagingSource
import org.easybangumi.next.lib.utils.PagingFrame
import org.easybangumi.next.lib.utils.map
import org.easybangumi.next.shared.data.cartoon.CartoonIndex
import org.easybangumi.next.shared.plugin.api.SourceResult
import org.easybangumi.next.shared.plugin.api.component.meta.MetaManager
import org.easybangumi.next.shared.plugin.api.toDataState

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

class BangumiMetaManager(
    private val bangumiApi: BangumiApi
): MetaManager {
    override val fromSourceKey: String = BangumiInnerSource.SOURCE_ID

    suspend fun getSubject(
        cartoonIndex: CartoonIndex,
    ): SourceResult<Subject> {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return bangumiApi.getSubject(cartoonIndex.id).await().toSourceResult()
    }

    suspend fun getCharacter(
        cartoonIndex: CartoonIndex,
    ): SourceResult<List<Character>> {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return bangumiApi.getCharacterList(cartoonIndex.id).await().toSourceResult()
    }

    suspend fun getPerson(
        cartoonIndex: CartoonIndex,

    ): SourceResult<List<Person>> {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return bangumiApi.getPersonList(cartoonIndex.id).await().toSourceResult()
    }


    class EpisodeListPagingSource(
        private val bangumiApi: BangumiApi,
        private val cartoonIndex: CartoonIndex,
    ) : EasyPagingSource<Episode> {
        override val initKey: String = "1"

        override suspend fun load(key: String): DataState<PagingFrame<Episode>> {
            if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
                throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
            }
            val page = key.toIntOrNull() ?: 1
            val rsp = bangumiApi.getEpisodeList(cartoonIndex.id, (page-1)*100, 100).await()
            return rsp.toSourceResult().toDataState().map {
                val nextKey = if (it.data.isEmpty()) null else (page + 1).toString()
                PagingFrame(nextKey, it.data)
            }
        }
    }

    fun createEpisodePagingSource(
        cartoonIndex: CartoonIndex,
    ): EpisodeListPagingSource {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return EpisodeListPagingSource(bangumiApi, cartoonIndex)
    }

    class CommentListPagingSource(
        private val bangumiApi: BangumiApi,
        private val cartoonIndex: CartoonIndex,
    ) : EasyPagingSource<Reviews> {
        override val initKey: String = "1"

        override suspend fun load(key: String): DataState<PagingFrame<Reviews>> {
            if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
                throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
            }
            val page = key.toIntOrNull() ?: 1
            val rsp = bangumiApi.getComments(cartoonIndex.id, page).await()
            return rsp.toSourceResult().toDataState().map {
                val nextKey = if (it.isEmpty()) null else (page + 1).toString()
                PagingFrame(nextKey, it)
            }
        }
    }

    fun createCommentPagingSource(
        cartoonIndex: CartoonIndex,
    ): CommentListPagingSource {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return CommentListPagingSource(bangumiApi, cartoonIndex)
    }

    fun coverUrl(
        cartoonIndex: CartoonIndex,
        type: String = "large",
    ): String {
        if (cartoonIndex.source != BangumiInnerSource.SOURCE_ID) {
            throw IllegalArgumentException("BangumiMateManager only supports BangumiInnerSource")
        }
        return bangumiApi.coverUrl(cartoonIndex.id, type)
    }



}