package com.heyanle.easybangumi4.ui.common

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.R

/**
 * Created by HeYanLe on 2023/1/7 18:44.
 * https://github.com/heyanLE
 */
@Composable
fun ErrorPage(
    modifier: Modifier = Modifier,
    image: Any = R.drawable.error_ikuyo,
    errorMsg: String = "",
    errorMsgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    clickEnable: Boolean = false,
    other: @Composable () -> Unit = {},
    onClick: () -> Unit = {},

    ) {
    WhitePage(
        modifier.let {
            if (clickEnable) {
                it.clickable {
                    onClick()
                }
            } else {
                it
            }
        }, image, errorMsg, errorMsgColor, other = other
    )
}

@Composable
fun EmptyPage(
    modifier: Modifier = Modifier,
    emptyMsg: String = stringResource(id = com.heyanle.easy_i18n.R.string.is_empty),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    other: @Composable () -> Unit = {},
) {
    WhitePage(modifier, R.drawable.empty_bocchi, emptyMsg, other = other, msgColor = msgColor)
}

@Composable
fun LoadingPage(
    modifier: Modifier = Modifier,
    loadingMsg: String = stringResource(id = com.heyanle.easy_i18n.R.string.loading),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    other: @Composable () -> Unit = {},
) {
    WhitePage(modifier, Uri.parse("file:///android_asset/loading_ryo.gif"), loadingMsg, other = other, msgColor = msgColor)
}

@Composable
fun WhitePage(
    modifier: Modifier = Modifier,
    image: Any,
    message: String = "",
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    other: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier.then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OkImage(modifier = Modifier.size(64.dp), image = image, contentDescription = message)

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = msgColor,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            other()
        }
    }


}
