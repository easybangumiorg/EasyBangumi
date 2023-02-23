package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.heyanle.bangumi_source_api.api.IHomeParser
import com.heyanle.bangumi_source_api.api.ISearchParser
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.source.AnimSourceFactory
import com.heyanle.easybangumi.source.AnimSources

/**
 * Created by HeYanLe on 2023/1/17 19:16.
 * https://github.com/heyanLE
 */
// 没番剧源阻断
@Composable
fun SourceContainer(
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (AnimSources) -> Unit,
) {
    val animSources by AnimSourceFactory.parsers().collectAsState(initial = null)
    val anim = animSources
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        if (anim == null || anim.empty()) {
            EmptyPage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(errorContainerColor),
                emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source)
            )
        } else {
            content(anim)
        }
    }
}

@Composable
fun HomeSourceContainer(
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (List<IHomeParser>) -> Unit,
) {
    SourceContainer(
        modifier,
        errorContainerColor = errorContainerColor
    ) {
        val homes = it.homeParsers()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            if (homes.isEmpty()) {
                EmptyPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(errorContainerColor),
                    emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source)
                )
            } else {
                content(homes)
            }
        }
    }
}

@Composable
fun SearchSourceContainer(
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (List<ISearchParser>) -> Unit,
) {
    SourceContainer(
        modifier,
        errorContainerColor = errorContainerColor
    ) {
        val homes = it.searchParsers()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            if (homes.isEmpty()) {
                EmptyPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(errorContainerColor),
                    emptyMsg = stringResource(id = com.heyanle.easy_i18n.R.string.no_source)
                )
            } else {
                content(homes)
            }
        }
    }
}