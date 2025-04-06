package org.easybangumi.next.shared.foundation.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.resources.Res

/**
 * Created by heyanlin on 2025/3/20.
 */
@Composable
fun <T> LoadScaffold(
    modifier: Modifier,
    data: DataState<T>,
    checkEmpty: Boolean = true,
    onNone : @Composable () -> Unit = {  },
    onLoading: @Composable (DataState.Loading<T>) -> Unit = {
        LoadingElements(
            Modifier.fillMaxSize(),
            loadingMsg = it.loadingMsg.ifEmpty { stringResource(Res.strings.loading) })
    },
    errorRetry: ((DataState.Error<T>) -> Unit)? = null,
    onError: @Composable (DataState.Error<T>) -> Unit = { errState ->
        ErrorElements(
            Modifier.fillMaxSize(),
            errorMsg = errState.errorMsg.ifEmpty { stringResource(Res.strings.net_error) },
            onClick = errorRetry?.let { { it.invoke(errState) } },
            other = {
                if (errorRetry != null) {
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = stringResource(Res.strings.retry),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        )
    },
    onEmptyIfCheck: @Composable (DataState.Error<T>) -> Unit = { errState ->
        EmptyElements(
            Modifier.fillMaxSize(),
            emptyMsg = errState.errorMsg.ifEmpty { stringResource(Res.strings.is_empty) },
            onClick = errorRetry?.let { { it.invoke(errState) } },
            other = {
                if (errorRetry != null) {
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text = stringResource(Res.strings.retry),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        )
    },
    container: @Composable (modifier: Modifier, content: @Composable ()->Unit) -> Unit = { m, c ->
        Box(m) { c() }
    },
    onOk: @Composable (DataState.Ok<T>) -> Unit
) {
    val content = @Composable {
        when (data) {
            is DataState.None -> { onNone() }
            is DataState.Loading -> onLoading(data)
            is DataState.Ok -> onOk(data)
            is DataState.Error -> {
                if (checkEmpty && data.isEmpty) {
                    onEmptyIfCheck(data)
                } else {
                    onError(data)
                }
            }
        }
    }
    container.invoke(modifier, content)
}