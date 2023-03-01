package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by HeYanLe on 2023/3/1 15:41.
 * https://github.com/heyanLE
 */


fun <T : Any> LazyGridScope.pagingCommon(items: LazyPagingItems<T>){
    when(items.loadState.refresh){
        is LoadState.Loading -> {
            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {
                LoadingPage(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is LoadState.Error -> {
            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {
                val errorMsg =
                    (items.loadState.refresh as? LoadState.Error)?.error?.message
                        ?: stringRes(
                            R.string.net_error
                        )
                ErrorPage(
                    modifier = Modifier.fillMaxWidth(),
                    errorMsg = errorMsg,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    },
                    onClick = {
                        items.refresh()
                    }
                )
            }
        }

        else -> {

        }
    }

    when (items.loadState.append) {
        is LoadState.Loading -> {
            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {
                LoadingPage(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is LoadState.Error -> {
            item(
                span = {
                    // LazyGridItemSpanScope:
                    // maxLineSpan
                    GridItemSpan(maxLineSpan)
                }
            ) {
                val errorMsg =
                    (items.loadState.append as? LoadState.Error)?.error?.message
                        ?: stringRes(
                            R.string.net_error
                        )
                ErrorPage(
                    modifier = Modifier.fillMaxWidth(),
                    errorMsg = errorMsg,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    },
                    onClick = {
                        items.retry()
                    }
                )
            }
        }

        else -> {

        }
    }
}

fun <T : Any> LazyListScope.pagingCommon(items: LazyPagingItems<T>){
    when(items.loadState.refresh){
        is LoadState.Loading -> {
            item() {
                LoadingPage(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is LoadState.Error -> {
            item() {
                val errorMsg =
                    (items.loadState.refresh as? LoadState.Error)?.error?.message
                        ?: stringRes(
                            R.string.net_error
                        )
                ErrorPage(
                    modifier = Modifier.fillMaxWidth(),
                    errorMsg = errorMsg,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    },
                    onClick = {
                        items.refresh()
                    }
                )
            }
        }

        else -> {

        }
    }

    when (items.loadState.append) {
        is LoadState.Loading -> {
            item() {
                LoadingPage(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        is LoadState.Error -> {
            item() {
                val errorMsg =
                    (items.loadState.append as? LoadState.Error)?.error?.message
                        ?: stringRes(
                            R.string.net_error
                        )
                ErrorPage(
                    modifier = Modifier.fillMaxWidth(),
                    errorMsg = errorMsg,
                    clickEnable = true,
                    other = {
                        Text(text = stringResource(id = R.string.click_to_retry))
                    },
                    onClick = {
                        items.retry()
                    }
                )
            }
        }

        else -> {

        }
    }
}