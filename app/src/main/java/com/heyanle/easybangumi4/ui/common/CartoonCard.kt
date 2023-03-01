package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heyanle.bangumi_source_api.api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/25 21:04.
 * https://github.com/heyanLE
 */

@Composable
fun CartoonCardWithCover(
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
        if(cartoonCover.coverUrl != null){
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonCardWithoutCover(
    modifier: Modifier = Modifier,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
) {
    Column (
        modifier = Modifier
            .width(260.dp)
            .padding(4.dp)
//                .clip(RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable {
                onClick(cartoonCover)
            }
            .padding(8.dp)
            .then(modifier),
    ) {

        Text(
            text = cartoonCover.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        cartoonCover.intro?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }

}