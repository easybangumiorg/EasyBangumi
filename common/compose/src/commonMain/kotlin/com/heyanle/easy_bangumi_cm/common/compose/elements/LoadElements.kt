package com.heyanle.easy_bangumi_cm.common.compose.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.heyanle.easy_bangumi_cm.base.utils.resources.ResourceOr
import com.heyanle.easy_bangumi_cm.common.compose.image.AnimationImage
import com.heyanle.easy_bangumi_cm.common.compose.image.AsyncImage
import com.heyanle.easy_bangumi_cm.common.resources.Res
import dev.icerock.moko.resources.compose.stringResource


/**
 * Created by HeYanLe on 2025/3/2 14:43.
 * https://github.com/heyanLE
 */
@Composable
fun LoadingElements(
    modifier: Modifier,
    isRow: Boolean = false,
    loadingMsg: String = stringResource(Res.strings.loading),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    other: @Composable () -> Unit = { },
) {
    ImageElements(
        modifier,
        Res.assets.loading_anon_gif,
        "loading",
        isRow,
        true,
        loadingMsg,
        msgColor,
        other
    )
}

@Composable
fun EmptyElements(
    modifier: Modifier,
    isRow: Boolean = false,
    emptyMsg: String = stringResource(Res.strings.is_empty),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    onClick: (() -> Unit)? = null,
    other: @Composable () -> Unit = { },
) {
    ImageElements(
        modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it },
        Res.images.empty_soyolin,
        "empty",
        isRow,
        false,
        emptyMsg,
        msgColor,
        other
    )
}

@Composable
fun ErrorElements(
    modifier: Modifier,
    isRow: Boolean = false,
    errorMsg: String = stringResource(Res.strings.net_error),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    onClick: (() -> Unit)? = null,
    other: @Composable () -> Unit = { },
) {
    ImageElements(
        modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it },
        Res.assets.error_tomorin_gif,
        "error",
        isRow,
        true,
        errorMsg,
        msgColor,
        other
    )
}

@Composable
fun ImageElements(
    modifier: Modifier,
    model: ResourceOr,
    contentDescription: String,
    isRow: Boolean = false,
    isAnimation: Boolean = false,
    message: String = stringResource(Res.strings.loading),
    msgColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    other: @Composable () -> Unit = { },
) {
    if (isRow) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isAnimation) {
                AnimationImage(
                    modifier = Modifier.size(64.dp),
                    model = model,
                    contentDescription = contentDescription,
                )
            } else {
                AsyncImage(
                    model = model,
                    modifier = Modifier.size(64.dp),
                    contentDescription = contentDescription,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = msgColor, fontStyle = FontStyle.Italic)
            other()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isAnimation) {
                AnimationImage(
                    modifier = Modifier.size(64.dp),
                    model = model,
                    contentDescription = contentDescription,
                )
            } else {
                AsyncImage(
                    model = model,
                    modifier = Modifier.size(64.dp),
                    contentDescription = contentDescription,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = msgColor, fontStyle = FontStyle.Italic)
            other()
        }
    }
}