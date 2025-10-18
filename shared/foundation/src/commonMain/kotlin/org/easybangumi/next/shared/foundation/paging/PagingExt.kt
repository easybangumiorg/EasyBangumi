package org.easybangumi.next.shared.foundation.paging

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

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
fun LazyPagingItems<*>.isRefreshLoading(): Boolean {
    return this.loadState.refresh is LoadState.Loading || this.loadState.source.refresh is LoadState.Loading
}

fun LazyPagingItems<*>.isAppendLoading(): Boolean {
    return this.loadState.append is LoadState.Loading || this.loadState.source.append is LoadState.Loading
}

fun LazyPagingItems<*>.isPrependLoading(): Boolean {
    return this.loadState.prepend is LoadState.Loading || this.loadState.source.prepend is LoadState.Loading
}

fun LazyPagingItems<*>.isLoading(): Boolean {
    return isRefreshLoading() || isAppendLoading() || isPrependLoading()
}


fun LazyPagingItems<*>.getRefreshError(): LoadState.Error? {
    return this.loadState.refresh as? LoadState.Error ?:
           this.loadState.source.refresh as? LoadState.Error
}

fun LazyPagingItems<*>.getAppendError(): LoadState.Error? {
    return this.loadState.append as? LoadState.Error ?:
           this.loadState.source.append as? LoadState.Error
}

fun LazyPagingItems<*>.getPrependError(): LoadState.Error? {
    return this.loadState.prepend as? LoadState.Error ?:
           this.loadState.source.prepend as? LoadState.Error
}

fun LazyPagingItems<*>.getError(): LoadState.Error? {
    return  getAppendError() ?: getRefreshError() ?: getPrependError()
}