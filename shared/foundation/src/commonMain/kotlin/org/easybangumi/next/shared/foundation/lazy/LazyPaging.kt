package org.easybangumi.next.shared.foundation.lazy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import dev.icerock.moko.resources.compose.stringResource
import org.easybangumi.next.shared.foundation.elements.EmptyElements
import org.easybangumi.next.shared.foundation.elements.ErrorElements
import org.easybangumi.next.shared.foundation.elements.LoadingElements
import org.easybangumi.next.shared.foundation.paging.getError
import org.easybangumi.next.shared.foundation.paging.getPrependError
import org.easybangumi.next.shared.foundation.paging.isLoading
import org.easybangumi.next.shared.resources.Res

/**
 * Created by heyanle on 2025/2/28.
 */
fun <T : Any> LazyGridScope.pagingCommon(
    height: Dp,
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true,
    canRetry: Boolean = true,
) {

    val err = pagingItems.getError()
    if (pagingItems.isLoading() && isShowLoading) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(height),
                isRow = true,
                loadingMsg = stringResource(Res.strings.loading),
            )
        }
    } else  if (err != null) {

        item(span = { GridItemSpan(maxLineSpan) }) {
            val errorMsg =
                err.error.message ?: stringResource(Res.strings.net_error)
            ErrorElements(
                modifier = Modifier.fillMaxWidth().height(height),
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

    } else if (pagingItems.itemCount == 0) {
        val prependErr = pagingItems.getPrependError()
        if (prependErr != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val errorMsg =
                    prependErr.error.message ?: stringResource(Res.strings.net_error)
                EmptyElements(
                    modifier = Modifier.fillMaxWidth().height(height),
                )
            }
        }

    }


}

fun <T : Any> LazyListScope.pagingCommon(
    height: Dp,
    pagingItems: LazyPagingItems<T>,
    isShowLoading: Boolean = true,
    canRetry: Boolean = true,
) {

    if (pagingItems.isLoading() && isShowLoading) {
        item() {
            LoadingElements(
                modifier = Modifier.fillMaxWidth().height(height),
                isRow = true,
                loadingMsg = stringResource(Res.strings.loading),
            )
        }
    } else {
        val err = pagingItems.getError()
        if (err != null) {
            item() {
                val errorMsg =
                    err.error.message ?: stringResource(Res.strings.net_error)
                ErrorElements(
                    modifier = Modifier.fillMaxWidth().height(height),
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
        && pagingItems.loadState.source.append is LoadState.NotLoading && pagingItems.loadState.source.refresh is LoadState.NotLoading
    ) {
        Column {
            headerWhenErrorEmpty?.invoke(this)
            EmptyElements(
                modifier = Modifier.fillMaxSize(),
                isRow = true,
            )
        }
    } else {
        var refresh = pagingItems.loadState.refresh
        if (refresh is LoadState.NotLoading) {
            refresh = pagingItems.loadState.source.refresh
        }
        when (refresh) {
            is LoadState.Loading -> {
                if (isShowLoading) {
                    LoadingElements(
                        modifier = Modifier.fillMaxSize(),
                        isRow = true,
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

            else -> {

            }
        }
    }


}