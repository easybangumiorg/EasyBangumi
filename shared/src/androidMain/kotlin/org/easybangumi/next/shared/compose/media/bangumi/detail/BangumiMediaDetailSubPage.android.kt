package org.easybangumi.next.shared.compose.media.bangumi.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaPageParam
import org.easybangumi.next.shared.compose.media.mediaPlayLineIndex

@Composable
fun BangumiMediaDetailSubPageAndroid(param: BangumiMediaPageParam) {
    val commonVM = param.commonVM
    val state = commonVM.state.collectAsState()
    val sta = state.value
    val playLineIndexState = commonVM.playIndexState.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp, 8.dp),
        ) {
            // Bangumi 详情卡片
            bangumiDetailCard(commonVM, sta)
            space(Modifier.height(8.dp))
            // 操作按钮
//            playerAction(param)
//            space(Modifier.height(8.dp))
            // 播放源卡片
            playerSourceCard(commonVM, sta)
            space(Modifier.height(8.dp))
            divider()
            // 播放线路和集数选择
            mediaPlayLineIndex(commonVM.playLineIndexVM, commonVM.playLineIndexVM.ui.value, 2)
        }

    }
}