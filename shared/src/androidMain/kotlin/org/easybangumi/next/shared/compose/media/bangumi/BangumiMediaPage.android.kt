package org.easybangumi.next.shared.compose.media.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media.bangumi.comment.BangumiMediaCommentSubPage
import org.easybangumi.next.shared.compose.media.bangumi.detail.BangumiMediaDetailSubPageAndroid
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

actual class BangumiMediaPageParam(
    val bangumiMediaVM: AndroidBangumiMediaVM,
) {
    actual val commonVM: BangumiMediaCommonVM
        get() = bangumiMediaVM.commonVM
}

sealed class BangumiMediaSubPage(
    val label: @Composable () -> Unit,
    val content: @Composable (BangumiMediaPageParam) -> Unit,
) {
    data object Detail: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.detailed)) },
        content = {
            BangumiMediaDetailSubPageAndroid(it)
        }
    )

    data object Comment: BangumiMediaSubPage (
        label = { Text(stringRes(Res.strings.comment) )},
        content = {
            BangumiMediaCommentSubPage(it)
        }
    )
}

internal val bangumiMediaSubPageList = listOf(
    BangumiMediaSubPage.Detail,
    BangumiMediaSubPage.Comment
)


@Composable
actual fun BangumiMediaPage(
    param: BangumiMediaPageParam,
    modifier: Modifier
)  {
    val commonVM = param.commonVM
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState {
        bangumiMediaSubPageList.size
    }
    Column (modifier) {
        EasyTab(
            modifier = Modifier.fillMaxWidth(),
            scrollable = true,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            size = bangumiMediaSubPageList.size,
            selection = pagerState.currentPage,
            onSelected = {
                scope.launch {
                    pagerState.animateScrollToPage(it)
                }
            },
            tabs = { index, selected ->
                val tab = bangumiMediaSubPageList[index]
                tab.label.invoke()
            }
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            val tab = bangumiMediaSubPageList[it]
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                tab.content(param)
            }
        }
    }
}