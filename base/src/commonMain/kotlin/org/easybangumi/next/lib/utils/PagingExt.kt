package org.easybangumi.next.lib.utils

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

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
typealias PagingFlow<T> = Flow<PagingData<T>>
typealias PagingFrame<T> = Pair<String?, List<T>>

interface EasyPagingSource <T> {

    val initKey: String

    suspend fun load(key: String): DataState<PagingFrame<T>>

}


class EasyPagingWrapper<T: Any>(
    private val source: EasyPagingSource<T>,
) : PagingSource<String, T>() {
    override fun getRefreshKey(state: PagingState<String, T>): String? {
        return source.initKey
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, T> {
        val key = params.key ?: return LoadResult.Error(NullPointerException())
        source.load(key).onOK {
            return LoadResult.Page(
                data = it.second,
                prevKey = null,
                nextKey = it.first
            )
        }
            .onError {
                val err = it.throwable
                return if (err != null) {
                    LoadResult.Error(err)
                } else {
                    LoadResult.Error(Exception(it.errorMsg ?: "load error"))
                }
            }
        return LoadResult.Error(IllegalStateException())
    }
}

fun <T : Any> EasyPagingSource<T>.newPagingFlow(): PagingFlow<T> {
    return Pager<String, T>(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false,
        ),
        initialKey = this.initKey,
        pagingSourceFactory = { EasyPagingWrapper(this) }
    ).flow
}

