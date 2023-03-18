package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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

/**
 * Created by HeYanLe on 2023/3/16 23:17.
 * https://github.com/heyanLE
 */
@Composable
fun CartoonCard(
    cover: String,
    name: String,
    source: String?,
) {
    Box(
        modifier = Modifier
            .width(95.dp)
            .aspectRatio(19/27F)
            .clip(RoundedCornerShape(4.dp))
    ) {

        OkImage(
            image = cover,
            contentDescription = name,
            modifier = Modifier
                .fillMaxSize()
        )
        source?.let {
            Text(
                fontSize = 13.sp,
                text = it,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 0.dp, 4.dp, 0.dp)
                    )
                    .padding(4.dp, 0.dp)
            )
        }


    }
}