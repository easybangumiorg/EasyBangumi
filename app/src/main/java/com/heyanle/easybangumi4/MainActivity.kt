package com.heyanle.easybangumi4

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi4.source.SourcesHost
import com.heyanle.easybangumi4.theme.EasyTheme
import com.heyanle.easybangumi4.ui.common.LoadingImage
import com.heyanle.easybangumi4.ui.common.MoeDialog
import com.heyanle.easybangumi4.ui.common.MoeSnackBar
import com.heyanle.easybangumi4.utils.AnnoHelper
import com.heyanle.easybangumi4.utils.MediaUtils
import com.heyanle.easybangumi4.utils.ReleaseDialog
import com.heyanle.okkv2.core.okkv

/**
 * Created by HeYanLe on 2023/10/29 21:20.
 * https://github.com/heyanLE
 */

val LocalWindowSizeController = staticCompositionLocalOf<WindowSizeClass> {
    error("AppNavController Not Provide")
}

class MainActivity : ComponentActivity() {

    var first by okkv("first_visible", def = true)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
        Scheduler.runOnMainActivityCreate(this, first)
        MediaUtils.setIsDecorFitsSystemWindows(this, false)
        setContent {
            val isMigrating by Migrate.isMigrating.collectAsState()
            val windowClazz = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalWindowSizeController provides windowClazz) {
                EasyTheme {
                    if(isMigrating){
                        AlertDialog(
                            text = {
                                Row {
                                    LoadingImage()
                                    Text(text = stringResource(id = com.heyanle.easy_i18n.R.string.migrating))
                                }
                            },
                            confirmButton = {},
                            onDismissRequest = {  },
                        )
                    }
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
                        }
                        MoeSnackBar(Modifier.statusBarsPadding())
                        MoeDialog()
                        ReleaseDialog()
                        AnnoHelper.ComposeDialog()
                    }

                }
            }

        }

    }
}