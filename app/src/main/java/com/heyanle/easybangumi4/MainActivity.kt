package com.heyanle.easybangumi4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi4.source.SourceMaster
import com.heyanle.easybangumi4.theme.EasyTheme
import com.heyanle.easybangumi4.ui.common.MoeSnackBar
import com.heyanle.easybangumi4.utils.AnnoHelper
import com.heyanle.easybangumi4.utils.MediaUtils
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2023/2/19 13:08.
 * https://github.com/heyanLE
 */
class MainActivity : ComponentActivity() {

    var first by okkv("first_visible", def = true)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initUtils(BangumiApp.INSTANCE)
        // networkHelper.defaultUA = WebView(this).getDefaultUserAgentString()
        MediaUtils.setIsDecorFitsSystemWindows(this, false)

        setContent {

            EasyTheme {
                val focusManager = LocalFocusManager.current

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { focusManager.clearFocus() })
                ) {
                    SourceMaster.SourceHost {
                        Nav()
                        EasyTheme {
                            MoeSnackBar(Modifier.statusBarsPadding())
                        }
                    }

                }
                val firstDialog = remember {
                    mutableStateOf(first)
                }
                if (firstDialog.value) {
                    LaunchedEffect(key1 = Unit) {
                        first = false
                    }

                    AlertDialog(
                        onDismissRequest = {
                            firstDialog.value = false
                            first = false
                        },
                        title = {
                            Text(text = "尝鲜版须知")
                        },
                        text = {
                            Text(text = "4.0 尝鲜版还有很多问题，很多细节没有完善，因而能与旧版共存。仅供尝鲜，请等待正式版！！")
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                firstDialog.value = false
                                first = false
                            }) {
                                Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.confirm))
                            }
                        }
                    )
                }

                AnnoHelper.ComposeDialog()


            }

        }
//        EasyPlayerManager.enableOrientation = false
    }
}
