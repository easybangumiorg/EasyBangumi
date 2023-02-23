package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.heyanle.bangumi_source_api.api2.SourceFactory
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.source.SourceBundle
import com.heyanle.easybangumi4.source.SourceMaster

/**
 * Created by HeYanLe on 2023/2/22 23:53.
 * https://github.com/heyanLE
 */
@Composable
fun SourceContainer(
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle) -> Unit,
) {
    val animSources by SourceMaster.animSourceFlow.collectAsState()
    val anim = animSources
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        if (anim.empty()) {
            EmptyPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(errorContainerColor),
                emptyMsg = stringResource(id = R.string.no_source)
            )
        } else {
            content(anim)
        }
    }
}