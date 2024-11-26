package com.heyanle.easybangumi4.splash.step

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.splash.SplashGuildController
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.setting.AppearanceSetting
import com.heyanle.easybangumi4.ui.setting.DarkModeItem
import com.heyanle.easybangumi4.ui.setting.ThemeModeItem
import com.heyanle.okkv2.core.okkv

/**
 * Created by heyanlin on 2024/7/4.
 */
class ThemeStep : BaseStep {

    override val name: String
        get() = "Theme"
    override val version: Int
        get() = 0

    @Composable
    override fun Compose() {
        Column (
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp, 0.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(0.dp, 16.dp)
        ) {


            DarkModeItem()
            Spacer(modifier = Modifier.size(16.dp))
            ThemeModeItem()
        }
    }
}