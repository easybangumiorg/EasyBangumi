package com.heyanle.easybangumi4.ui.cartoon_play.cartoon_recorded.task

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.LongEditPreferenceItem
import com.heyanle.easybangumi4.ui.common.StringSelectPreferenceItem
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanle on 2024/6/23.
 * https://github.com/heyanLE
 */
@Composable
fun CartoonRecordedTaskDialog(
    cartoonRecordedTaskModel: CartoonRecordedTaskModel
) {

    DisposableEffect(Unit) {
        onDispose {
            cartoonRecordedTaskModel.stop()
            cartoonRecordedTaskModel.onDismissRequest()
        }
    }

    if (cartoonRecordedTaskModel.process == -2) {
        AlertDialog(
            onDismissRequest = {
                cartoonRecordedTaskModel.onDismissRequest()
            },
            title = {
                Text(text = stringResource(id = if (cartoonRecordedTaskModel.type == 1) com.heyanle.easy_i18n.R.string.save_gif else com.heyanle.easy_i18n.R.string.save_video))
            },
            text = {
                Column {
                    val textList = listOf("15", "30")
                    StringSelectPreferenceItem(
                        title = { Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.fps)) },
                        textList = textList,
                        select = textList.indexOf(cartoonRecordedTaskModel.fps.intValue.toString()),
                    ) {
                        cartoonRecordedTaskModel.fps.intValue = textList[it].toInt()
                    }

                    LongEditPreferenceItem(
                        title = {
                            Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.quality))
                        },
                        value = cartoonRecordedTaskModel.quality.intValue.toLong()
                    ) {
                        if (it !in 1..100) {
                            stringRes(com.heyanle.easy_i18n.R.string.quality_error).moeSnackBar()
                            return@LongEditPreferenceItem
                        }
                        cartoonRecordedTaskModel.quality.intValue = it.toInt()
                    }


                }

            },
            confirmButton = {
                TextButton(onClick = {
                    cartoonRecordedTaskModel.start()
                }) {
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.save_start))
                }
            },
        )
    } else {
        AlertDialog(
            onDismissRequest = {
                //cartoonRecordedTaskModel.onDismissRequest()
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingImage()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.saving) + " ${if (cartoonRecordedTaskModel.process != -1) "${cartoonRecordedTaskModel.process}%" else ""}")
                }
            },
            confirmButton = {}
        )
    }


}