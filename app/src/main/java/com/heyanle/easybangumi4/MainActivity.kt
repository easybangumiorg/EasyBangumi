package com.heyanle.easybangumi4

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.theme.EasyTheme
import com.heyanle.easybangumi4.compose.common.MoeSnackBar
import com.heyanle.easybangumi4.source.SourcesHost
import com.heyanle.easybangumi4.source.utils.initUtils
import com.heyanle.easybangumi4.utils.AnnoHelper
import com.heyanle.easybangumi4.utils.MediaUtils
import com.heyanle.extension_load.ExtensionInit
import com.heyanle.extension_load.IconFactoryImpl
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2023/2/19 13:08.
 * https://github.com/heyanLE
 */

val LocalWindowSizeController = staticCompositionLocalOf<WindowSizeClass> {
    error("AppNavController Not Provide")
}

class MainActivity : ComponentActivity() {

    var first by okkv("first_visible", def = true)

    private fun init(){
        ExtensionInit.init(this, IconFactoryImpl())
        kotlin.runCatching {
            initUtils(this)
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Migrate.tryUpdate(this)
        init()
        // initUtils(BangumiApp.INSTANCE)
        // networkHelper.defaultUA = WebView(this).getDefaultUserAgentString()
        MediaUtils.setIsDecorFitsSystemWindows(this, false)

//        androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
        setContent {
            val windowClazz = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalWindowSizeController provides windowClazz) {
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
                        SourcesHost {
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
                                    Text(text = stringResource(id = R.string.confirm))
                                }
                            }
                        )
                    }

                    AnnoHelper.ComposeDialog()
                }
            }


        }
//        EasyPlayerManager.enableOrientation = false
    }
}
