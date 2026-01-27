package org.easybangumi.next.shared.compose.media.bangumi.detail.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.easybangumi.next.shared.compose.media.bangumi.AndroidBangumiMediaVM
import org.easybangumi.next.shared.compose.media.bangumi.BangumiMediaPageParam

@Composable
actual fun BangumiAction(param: BangumiMediaPageParam) {
    val vm = param.bangumiMediaVM
    Row (
        Modifier.padding(4.dp, 0.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        CollectionAction(param)
    }
}
