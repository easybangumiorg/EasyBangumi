package com.heyanle.easybangumi

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.heyanle.easybangumi.player.TinyStatusController
import com.heyanle.easybangumi.theme.EasyTheme
import com.heyanle.easybangumi.ui.common.MoeSnackBar
import com.heyanle.easybangumi.ui.player.BangumiPlayController
import com.heyanle.eplayer_core.utils.MediaHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    Nav()
                    MoeSnackBar(Modifier.statusBarsPadding())
                }
            }
        }
        MediaHelper.setIsDecorFitsSystemWindows(this, false)
    }

    override fun finish() {
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            TinyStatusController.onActResume()
        }
    }

    override fun onPause() {
        super.onPause()
        kotlin.runCatching {
            TinyStatusController.onActPause()
        }
        kotlin.runCatching {
            BangumiPlayController.onPause()
        }
    }
}