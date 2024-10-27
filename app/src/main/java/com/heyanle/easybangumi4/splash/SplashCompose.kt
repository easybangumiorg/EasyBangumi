package com.heyanle.easybangumi4.splash

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.splash.step.SampleGuildHeader
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.launch

/**
 * Created by heyanlin on 2024/7/4.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Splash() {
    val controller: SplashGuildController by Inject.injectLazy()
    val pagerState = rememberPagerState {
        controller.realStep.size
    }
    val sp = LocalSplashActivity.current

    LaunchedEffect(key1 = Unit){
        if (controller.realStep.isEmpty()){
            sp.jumpToMain()
        }
    }

    val scope = rememberCoroutineScope()
    Column {
        SampleGuildHeader()
        HorizontalPager(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondViewportPageCount = controller.realStep.size,
            verticalAlignment = Alignment.Top,
            state = pagerState,
        ) {
            controller.realStep.getOrNull(it)?.let {
                it.Compose()
            }
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.size(16.dp))
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = {
                scope.launch {
                    if (pagerState.currentPage < controller.realStep.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        sp.jumpToMain()
                    }
                }
            }) {
            Text(stringResource(id = com.heyanle.easy_i18n.R.string.next))

        }
        Spacer(modifier = Modifier.size(16.dp))
    }


}
