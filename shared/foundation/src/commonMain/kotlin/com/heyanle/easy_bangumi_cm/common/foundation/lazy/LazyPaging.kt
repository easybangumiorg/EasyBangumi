package com.heyanle.easy_bangumi_cm.common.foundation.lazy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.heyanle.easy_bangumi_cm.common.foundation.elements.EmptyElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.ErrorElements
import com.heyanle.easy_bangumi_cm.common.foundation.elements.LoadingElements
import com.heyanle.easy_bangumi_cm.common.resources.Res
import dev.icerock.moko.resources.compose.stringResource

/**
 * Created by heyanlin on 2025/2/28.
 */
fun <T : Any> LazyGridScope.pagingCommon(
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true,
    canRetry: Boolean = true,
) {

    pagingItems.loadState.prepend
    when (val append = pagingItems.loadState.append) {
        is LoadState.Loading -> {
            if (isShowLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingElements(
                        modifier = Modifier.fillMaxWidth(),
                        isRow = true,
                        loadingMsg = stringResource(Res.strings.loading),
                    )
                }
            }
        }

        is LoadState.Error -> {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val errorMsg =
                    append.error.message ?: stringResource(Res.strings.net_error)
                ErrorElements(
                    modifier = Modifier.fillMaxWidth(),
                    isRow = true,
                    errorMsg = errorMsg,
                    onClick = if (canRetry) {{
                        pagingItems.retry()
                    }} else null,
                    other = {
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(Res.strings.retry),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                )
            }
        }

        else -> {}
    }


}

fun <T : Any> LazyListScope.pagingCommon(
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true,
    canRetry: Boolean = true,
) {

    pagingItems.loadState.prepend
    when (val append = pagingItems.loadState.append) {
        is LoadState.Loading -> {
            if (isShowLoading) {
                item {
                    LoadingElements(
                        modifier = Modifier.fillMaxWidth(),
                        isRow = true,
                        loadingMsg = stringResource(Res.strings.loading),
                    )
                }
            }
        }

        is LoadState.Error -> {
            item {
                val errorMsg =
                    append.error.message ?: stringResource(Res.strings.net_error)
                ErrorElements(
                    modifier = Modifier.fillMaxWidth(),
                    isRow = true,
                    errorMsg = errorMsg,
                    onClick = if (canRetry) {{
                        pagingItems.retry()
                    }} else null,
                    other = {
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(Res.strings.retry),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                )
            }
        }

        else -> {}
    }


}

@Composable
fun <T : Any> PagingCommon(
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true,
    canRetry: Boolean = true,
    headerWhenErrorEmpty: (@Composable ColumnScope.() -> Unit)? = null
) {
    if (pagingItems.loadState.refresh is LoadState.NotLoading &&
        pagingItems.loadState.append is LoadState.NotLoading && pagingItems.itemCount == 0
    ) {
        Column {
            headerWhenErrorEmpty?.invoke(this)
            EmptyElements(
                modifier = Modifier.fillMaxSize(),
                isRow = false,
            )
        }
    } else {
        when (val refresh = pagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                if (isShowLoading) {
                    LoadingElements(
                        modifier = Modifier.fillMaxSize(),
                        isRow = false,
                        loadingMsg = stringResource(Res.strings.loading),
                    )
                }
            }

            is LoadState.Error -> {
                val errorMsg =
                    refresh.error.message ?: stringResource(Res.strings.net_error)
                Column {
                    headerWhenErrorEmpty?.invoke(this)
                    ErrorElements(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        isRow = false,
                        errorMsg = errorMsg,
                        onClick = if (canRetry) {{
                            pagingItems.retry()
                        }} else null,
                        other = {
                            Spacer(Modifier.size(12.dp))
                            Text(
                                text = stringResource(Res.strings.retry),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    )
                }

            }

            else -> {}
        }
    }


}