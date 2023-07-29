package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.bangumi_source_api.api.entity.CartoonCover
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.base.entity.CartoonStar
import com.heyanle.easybangumi4.source.LocalSourceBundleController

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                onClick(cartoonCover)
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (!cartoonCover.coverUrl.isNullOrEmpty()) {
            OkImage(
                modifier = Modifier
                    .then(modifier)
                    .aspectRatio(19 / 27F)
                    .clip(RoundedCornerShape(4.dp)),
                image = cartoonCover.coverUrl,
                contentDescription = cartoonCover.title,
                errorRes = R.drawable.placeholder,
            )

            Spacer(modifier = Modifier.size(4.dp))
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = cartoonCover.title,
                maxLines = 2,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonStarCardWithCover(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    cartoon: CartoonStar,
    showSourceLabel: Boolean = true,
    onClick: (CartoonStar) -> Unit,
    onLongPress: (CartoonStar) -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .run {
                if (selected) {
                    background(MaterialTheme.colorScheme.primary)
                } else {
                    this
                }
            }
            .then(modifier)
            .combinedClickable(
                onClick = {
                    onClick(cartoon)
                },
                onLongClick = {
                    onLongPress(cartoon)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val sourceBundle = LocalSourceBundleController.current
        Box(
            modifier = Modifier
                .aspectRatio(19 / 27F)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = cartoon.coverUrl?:"",
                contentDescription = cartoon.title,
                errorRes = R.drawable.placeholder,
            )
            if (showSourceLabel) {
                Text(
                    fontSize = 13.sp,
                    text = sourceBundle.source(cartoon.source)?.label
                        ?: cartoon.source,
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


        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = cartoon.title,
            maxLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = if(selected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonCardWithoutCover(
    modifier: Modifier = Modifier,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
) {
    Column(
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