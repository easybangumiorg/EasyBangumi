package com.heyanle.easybangumi4.v2.ui.story

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.plugin.api.entity.toIdentify
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.ui.story.local.LocalViewModel
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.theme.V2Theme

@Composable
internal fun LocalLibraryV1CopyV2(
    state: LocalViewModel.State,
    starredIdentities: Set<String>,
    onItemClick: (CartoonStoryItem) -> Unit,
    onItemLongPress: (CartoonStoryItem) -> Unit,
) {
    when {
        state.loading -> StoryLoadingV2("正在读取本地番剧")
        state.storyList.isEmpty() -> StoryEmptyV2(
            title = if (state.searchKey.isNullOrBlank()) "暂无本地番剧" else "没有匹配的番剧",
            subtitle = if (state.searchKey.isNullOrBlank()) {
                "下载完成的番剧会出现在这里"
            } else {
                "试试其他关键词"
            },
        )
        else -> {
            val haptic = LocalHapticFeedback.current
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 4.dp,
                    top = 4.dp,
                    end = 4.dp,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items = state.storyList,
                    key = { it.cartoonLocalItem.itemId },
                ) { item ->
                    LocalLibraryCardV2(
                        item = item,
                        selected = item in state.selection,
                        starred = item.cartoonLocalItem.cartoonCover.toIdentify() in starredIdentities,
                        onClick = { onItemClick(item) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemLongPress(item)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LocalLibraryCardV2(
    item: CartoonStoryItem,
    selected: Boolean,
    starred: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(13.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = if (selected) V2Theme.colors.accentContainer else Color.Transparent,
        contentColor = V2Tokens.TextPrimary,
        shape = shape,
        tonalElevation = 0.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(19f / 27f)
                    .clip(RoundedCornerShape(7.dp)),
            ) {
                OkImage(
                    modifier = Modifier.fillMaxSize(),
                    image = item.cartoonLocalItem.cartoonCover.coverUrl.orEmpty(),
                    contentDescription = item.cartoonLocalItem.title,
                    errorRes = R.drawable.placeholder,
                )
                if (starred) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(7.dp)
                            .size(27.dp),
                        color = V2Tokens.Surface.copy(alpha = 0.94f),
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "已追番",
                            tint = V2Theme.colors.accent,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.34f)),
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(7.dp)
                            .size(28.dp),
                        color = V2Theme.colors.accent,
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "已选择",
                            tint = V2Tokens.Surface,
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(7.dp),
                    color = V2Tokens.TextPrimary.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(7.dp),
                ) {
                    Text(
                        text = "${item.cartoonLocalItem.episodes.size} 集",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        color = V2Tokens.Surface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Text(
                text = item.cartoonLocalItem.title,
                modifier = Modifier.padding(top = 6.dp),
                color = V2Tokens.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp,
            )
        }
    }
}

@Composable
internal fun StoryLoadingV2(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = V2Theme.colors.accent, strokeWidth = 2.dp)
        Text(
            text = message,
            modifier = Modifier.padding(top = 14.dp),
            color = V2Tokens.TextSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
internal fun StoryEmptyV2(
    title: String,
    subtitle: String,
    titleColor: Color = V2Tokens.TextPrimary,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            color = titleColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = subtitle,
            modifier = Modifier.padding(top = 7.dp),
            color = V2Tokens.TextSecondary,
            fontSize = 13.sp,
        )
    }
}
