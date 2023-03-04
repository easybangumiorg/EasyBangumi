package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if(!cartoonCover.coverUrl.isNullOrEmpty()){
            OkImage(
                modifier = Modifier
                    .then(modifier).aspectRatio(19/27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoonCover.coverUrl,
                contentDescription = cartoonCover.title)

            Spacer(modifier = Modifier.size(4.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = cartoonCover.title,
                maxLines = 1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.size(4.dp))
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
            .fillMaxWidth()
            .then(modifier)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(0.6f), RoundedCornerShape(4.dp))
            .clickable {
                onClick(cartoonCover)
            }
            .padding(8.dp),
    ) {

        Text(
            text = cartoonCover.title,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        cartoonCover.intro?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }

}