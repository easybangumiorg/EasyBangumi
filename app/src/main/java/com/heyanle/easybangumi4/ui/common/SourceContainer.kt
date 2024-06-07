package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.ABOUT
import com.heyanle.easybangumi4.C
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.navigationSourceManager
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source.SourceController
import com.heyanle.easybangumi4.source.bundle.SourceBundle
import com.heyanle.easybangumi4.source_api.Source
import com.heyanle.easybangumi4.source_api.component.detailed.DetailedComponent
import com.heyanle.easybangumi4.source_api.component.page.SourcePage
import com.heyanle.easybangumi4.utils.logi
import com.heyanle.easybangumi4.utils.openUrl
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toast
import com.heyanle.injekt.core.Injekt

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
    SourceContainerBase(modifier, {it.searches().isNotEmpty()}, errorContainerColor, content)
}

@Composable
fun SourceContainerBase(
    modifier: Modifier = Modifier,
    hasSource: (SourceBundle) -> Boolean,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle) -> Unit,
) {
    val animSources = LocalSourceBundleController.current
    val anim = animSources

    val sourceStateCase: SourceStateCase by Injekt.injectLazy()
    val state by sourceStateCase.flowState().collectAsState()
    LaunchedEffect(state){
        state.logi("SourceController")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        when(state){
            is SourceController.SourceInfoState.Loading -> {
                LoadingPage(
                    modifier = Modifier
                        .fillMaxSize(),
                    loadingMsg = stringResource(id = R.string.source_loading)
                )
            }
            is SourceController.SourceInfoState.Info -> {
                if (!hasSource(anim)) {
                    val nav = LocalNavController.current
                    ErrorPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(errorContainerColor),
                        errorMsg = stringResource(id = R.string.no_source),
                        clickEnable = false,
                        other = {
                        }
                    )
                } else {
                    content(anim)
                }
            }
        }
    }
}

@Composable
fun PageContainer(
    sourceKey: String,
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle, Source, List<SourcePage>) -> Unit,
) {
    SourceContainerBase(modifier, {
        it.page(sourceKey)!= null
    }, errorContainerColor){ bundle ->
        bundle.page(sourceKey)?.let {
            content(bundle, it.source, it.getPages())
        }

    }
}

@Composable
fun DetailedContainer(
    sourceKey: String,
    modifier: Modifier = Modifier,
    errorContainerColor: Color = Color.Transparent,
    content: @Composable (SourceBundle, Source, DetailedComponent) -> Unit,
) {
    SourceContainerBase(modifier, {
        it.detailed(sourceKey)!= null
    }, errorContainerColor){ bundle ->
        bundle.detailed(sourceKey)?.let {
            content(bundle, it.source, it)
        }

    }
}