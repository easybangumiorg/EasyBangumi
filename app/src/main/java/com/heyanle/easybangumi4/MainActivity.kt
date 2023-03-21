package com.heyanle.easybangumi4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.heyanle.easybangumi4.source.SourceMaster
import com.heyanle.easybangumi4.theme.EasyTheme
import com.heyanle.easybangumi4.ui.common.MoeSnackBar
import com.heyanle.eplayer_core.EasyPlayerManager
import com.heyanle.eplayer_core.utils.MediaHelper

/**
 * Created by HeYanLe on 2023/2/19 13:08.
 * https://github.com/heyanLE
 */
class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initUtils(BangumiApp.INSTANCE)
        // networkHelper.defaultUA = WebView(this).getDefaultUserAgentString()
        MediaHelper.setIsDecorFitsSystemWindows(this, false)
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
                        MoeSnackBar(Modifier.statusBarsPadding())
                    }

                }
            }
        }
        EasyPlayerManager.enableOrientation = false
    }
}
