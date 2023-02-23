package com.heyanle.easybangumi.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.bangumi_source_api.api.entity.Bangumi
import com.heyanle.bangumi_source_api.api.entity.BangumiDetail
import com.heyanle.easybangumi.source.AnimSourceFactory

/**
 * Created by HeYanLe on 2023/2/5 14:43.
 * https://github.com/heyanLE
 */

@Composable
fun BangumiCard(
    bangumiDetail: BangumiDetail
) {
    BangumiCard(bangumiDetail.cover, bangumiDetail.name, bangumiDetail.source)
}

@Composable
fun BangumiCard(
    bangumi: Bangumi
) {
    BangumiCard(bangumi.cover, bangumi.name, bangumi.source)
}

@Composable
fun BangumiCard(
    cover: String,
    name: String,
    source: String,
) {
    Box(
        modifier = Modifier
            .height(135.dp)
            .width(95.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {

        OkImage(
            image = cover,
            contentDescription = name,
            modifier = Modifier
                .fillMaxSize()
        )

        val sourceText =
            AnimSourceFactory.label(source)
                ?: source
        Text(
            fontSize = 13.sp,
            text = sourceText,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondary,
                    RoundedCornerShape(0.dp, 0.dp, 8.dp, 0.dp)
                )
                .padding(8.dp, 0.dp)
        )

    }
}