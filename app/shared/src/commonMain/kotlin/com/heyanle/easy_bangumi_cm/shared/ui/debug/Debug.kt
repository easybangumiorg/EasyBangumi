package com.heyanle.easy_bangumi_cm.shared.ui.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heyanle.easy_bangumi_cm.common.foundation.image.AnimationImage
import com.heyanle.easy_bangumi_cm.common.foundation.image.AsyncImage
import com.heyanle.easy_bangumi_cm.common.resources.Res

/**
 * Created by heyanlin on 2025/2/27.
 */
@Composable
fun Debug() {

    Column (Modifier.fillMaxSize()) {
        AnimationImage(
            Res.assets.loading_anon_gif, "",
            Modifier.size(200.dp))
        AsyncImage(Res.images.logo, "logo",
            Modifier.size(200.dp))
        AsyncImage(
            "https://img.moegirl.org.cn/common/f/f2/BanG_Dream%21_It%27s_MyGO%21%21%21%21%21_03091601.jpg", "logo",
            Modifier.size(200.dp))
    }




}