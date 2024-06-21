package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.clip_video.ClipVideoSeek
import kotlinx.coroutines.launch
import loli.ball.easyplayer2.ControlViewModel

/**
 * Created by heyanlin on 2024/6/21.
 */
@OptIn(UnstableApi::class)
@Composable
fun CartoonRecorded(
    controlViewModel: ControlViewModel,
    cartoonRecordedModel: CartoonRecordedModel,
    show: Boolean,
    onDismissRequire: () -> Unit
) {

}

@OptIn(UnstableApi::class)
@Composable
private fun BoxScope.CartoonRecordedContent(
    cartoonRecordedModel: CartoonRecordedModel,
    controlViewModel: ControlViewModel,
    onDismissRequire: () -> Unit
) {

    LaunchedEffect(Unit) {
        cartoonRecordedModel.onLaunch()
        launch {
            snapshotFlow {
                cartoonRecordedModel.currentPosition
            }.collect {
                cartoonRecordedModel.exoPlayer.seekTo(it)
            }
        }
    }

    DisposableEffect(Unit) {
        // exoPlayer.clearVideoSurface()
        onDispose {
            cartoonRecordedModel.exoPlayer.seekTo(cartoonRecordedModel.currentPosition)
            cartoonRecordedModel.onDispose()
        }
    }


    val configuration = cartoonRecordedModel.configuration.collectAsState()

    Column {
        CartoonRecordedTopAppBar(
            cartoonRecordedModel = cartoonRecordedModel,
            config = configuration.value,
            onBack = onDismissRequire,
            onSave = {

            }
        )

        Row {
            // Left Tool

            // Right Tool
        }

        ClipVideoSeek(cartoonRecordedModel.clipVideoModel)

    }



}

@OptIn(UnstableApi::class)
@Composable
fun CartoonRecordedTopAppBar(
    cartoonRecordedModel: CartoonRecordedModel,
    config: CartoonRecordedModel.Configuration,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                onBack()
            }
        ) {
            // 返回按钮
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "back"
            )
        }

        Row {
            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (config.currentType == 2) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            cartoonRecordedModel.changeConfigType(2)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (config.currentType == 2) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = stringResource(com.heyanle.easy_i18n.R.string.record_video),
                    fontSize = 12.sp,
                )
            }

            Spacer(Modifier.size(8.dp))

            Surface(
                shape = CircleShape,
                modifier =
                Modifier
                    .padding(2.dp, 8.dp),
                color = if (config.currentType == 1) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            ) {
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            cartoonRecordedModel.changeConfigType(1)
                        }
                        .padding(8.dp, 0.dp),
                    color = if (config.currentType == 1) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.W900,
                    text = stringResource(com.heyanle.easy_i18n.R.string.record_gif),
                    fontSize = 12.sp,
                )
            }
        }


        TextButton(
            onClick = {

            }
        ) {
            Text("保存")
        }
    }
}