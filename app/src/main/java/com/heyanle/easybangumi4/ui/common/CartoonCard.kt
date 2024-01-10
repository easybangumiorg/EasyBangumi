package com.heyanle.easybangumi4.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonCover

/**
 * Created by HeYanLe on 2023/2/25 21:04.
 * https://github.com/heyanLE
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .combinedClickable(
                onClick = {
                    onClick(cartoonCover)
                },
                onLongClick = {
                    onLongPress?.invoke(cartoonCover)
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(19 / 27F)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = cartoonCover.coverUrl?:"",
                contentDescription = cartoonCover.title,
                errorRes = R.drawable.placeholder,
            )
            if (star) {
                Text(
                    fontSize = 13.sp,
                    text = stringResource(id = com.heyanle.easy_i18n.R.string.stared_min),
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
//            OkImage(
//                modifier = Modifier
//                    .then(modifier)
//                    .aspectRatio(19 / 27F)
//                    .clip(RoundedCornerShape(4.dp)),
//                image = cartoonCover.coverUrl,
//                contentDescription = cartoonCover.title,
//                errorRes = R.drawable.placeholder,
//            )

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CartoonStarCardWithCover(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    cartoon: CartoonInfo,
    showSourceLabel: Boolean,
    showWatchProcess: Boolean,
    showIsUp: Boolean,
    showIsUpdate: Boolean,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
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
                contentDescription = cartoon.name,
                errorRes = R.drawable.placeholder,
            )
            if (showSourceLabel) {
                Text(
                    fontSize = 13.sp,
                    text = sourceBundle.source(cartoon.source)?.label
                        ?: cartoon.sourceName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 4.dp, 0.dp, 0.dp)
                        )
                        .padding(4.dp, 0.dp)
                )
            }
            if (showWatchProcess && cartoon.lastHistoryTime != 0L) {
                cartoon.matchHistoryEpisode?.let { last ->
                    val index = last.first.sortedEpisodeList.indexOf(last.second)
                    Text(
                        fontSize = 13.sp,
                        text = "${index+1}/${last.first.sortedEpisodeList.size}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(0.dp, 0.dp, 0.dp, 4.dp)
                            )
                            .padding(4.dp, 0.dp)
                    )
                }
            }


            if(showIsUpdate || showIsUp){
                Row (
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 0.dp, 4.dp, 0.dp)
                        )
                        .padding(4.dp, 0.dp)
                ) {
                    if(showIsUp && cartoon.upTime > 0L){
                        Icon(Icons.Filled.PushPin, modifier = Modifier.size(13.dp).rotate(-45f),contentDescription = stringResource(id = com.heyanle.easy_i18n.R.string.push_pin) , tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    if(showIsUpdate && cartoon.isUpdate){
                        Text(
                            fontSize = 13.sp,
                            text = stringResource(id = com.heyanle.easy_i18n.R.string.need_update),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

        }


        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = cartoon.name,
            maxLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = if(selected) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CartoonCardWithoutCover(
    modifier: Modifier = Modifier,
    star: Boolean = false,
    cartoonCover: CartoonCover,
    onClick: (CartoonCover) -> Unit,
    onLongPress: ((CartoonCover) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .clip(RoundedCornerShape(4.dp))
            .border(
                1.dp,
                if (star) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                    0.6f
                ),
                RoundedCornerShape(4.dp)
            )
            .combinedClickable(
                onClick = {
                    onClick(cartoonCover)
                },
                onLongClick = {
                    onLongPress?.invoke(cartoonCover)
                }
            )
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
        if(star){
            Text(
                text = stringResource(id = com.heyanle.easy_i18n.R.string.stared_min),
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }

}