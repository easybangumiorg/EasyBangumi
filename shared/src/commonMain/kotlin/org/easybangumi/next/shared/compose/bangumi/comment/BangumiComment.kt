package org.easybangumi.next.shared.compose.bangumi.comment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import org.easybangumi.next.shared.data.bangumi.BgmReviews
import org.easybangumi.next.shared.foundation.image.AsyncImage
import org.easybangumi.next.shared.foundation.lazy.pagingCommon

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
fun BangumiComment(
    modifier: Modifier,
    vm: BangumiCommentVM,
) {
    val sta = vm.ui.value
    val pagingItems = sta.commentPaging?.collectAsLazyPagingItems()
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (pagingItems != null) {
            bgmReviewsItem(
                pagingItems = pagingItems,
                onClick = { review ->
                    // TODO 点击评论
                },
            )
            pagingCommon(200.dp, pagingItems, isShowLoading = true, canRetry = true)
        }

    }

}


private fun LazyListScope.bgmReviewsItem(
    pagingItems: LazyPagingItems<BgmReviews>,
    onClick: (BgmReviews) -> Unit,
) {
    items(pagingItems.itemCount) {
        val review = pagingItems[it]
        if (review != null) {
            BgmReviewItem(
                modifier = Modifier,
                review = review,
                onClick = onClick,
            )
        }
    }
}

@Composable
fun BgmReviewItem(
    modifier: Modifier,
    review: BgmReviews,
    onClick: (BgmReviews) -> Unit,
){
    Column {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            modifier = modifier.clickable { onClick(review) },
            overlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = review.cover ?: "",
                        modifier = Modifier.size(24.dp),
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${review.author}")
                    Spacer(Modifier.weight(1f))
                    Text("${review.date} - ${review.starCount} 回复")
                }
            },
            headlineContent = {
                Text("${review.title}", modifier = Modifier.padding(top = 4.dp))
            },
            supportingContent = {
                Text("${review.contentShort}", modifier = Modifier.padding(top = 4.dp))
            },

            )
        HorizontalDivider()
    }

}