package com.heyanle.easy_bangumi_cm.shared.ui.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heyanle.easy_bangumi_cm.common.compose.image.AnimationImage
import com.heyanle.easy_bangumi_cm.common.resources.Res

/**
 * Created by heyanlin on 2025/2/27.
 */
@Composable
fun Debug() {

    Box(Modifier.fillMaxSize()) {
        AnimationImage(
            Res.assets.loading_ryo_gif,
            "",
            Modifier.size(200.dp)
        )
    }




}