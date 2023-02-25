package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heyanle.bangumi_source_api.api2.entity.CartoonCover
import com.heyanle.easybangumi4.utils.dip2px

/**
 * Created by HeYanLe on 2023/2/25 21:04.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonCard(
    modifier: Modifier = Modifier,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
) {

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                onClick(cartoonCover)
            }
            .padding(4.dp)
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if(cartoonCover.coverUrl == null){

        }else{
            OkImage(
                modifier = Modifier
                    .height(135.dp)
                    .width(95.dp)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoonCover.coverUrl,
                contentDescription = cartoonCover.title)

            var needEnter by remember() {
                mutableStateOf(false)
            }

            Spacer(modifier = Modifier.size(4.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = "${cartoonCover.title}${if (needEnter) "\n " else ""}",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = {
                    if (it.lineCount < 2) {
                        needEnter = true
                    }
                }
            )
        }


    }

}