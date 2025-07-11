package org.easybangumi.next.shared.ui.detail.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.ext.shared.plugin.bangumi.model.Character
import org.easybangumi.ext.shared.plugin.bangumi.model.Person
import org.easybangumi.ext.shared.plugin.bangumi.model.Subject
import org.easybangumi.ext.shared.plugin.bangumi.model.Tags
import org.easybangumi.next.lib.utils.DataState
import org.easybangumi.next.shared.foundation.EasyTab
import org.easybangumi.next.shared.foundation.InputMode
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.cartoon.CartoonCoverCard
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderBehavior
import org.easybangumi.next.shared.foundation.scroll_header.ScrollableHeaderScaffold
import org.easybangumi.next.shared.foundation.scroll_header.rememberScrollableHeaderState
import org.easybangumi.next.shared.foundation.shimmer.ShimmerState
import org.easybangumi.next.shared.foundation.shimmer.drawRectWhenShimmerVisible
import org.easybangumi.next.shared.foundation.shimmer.onShimmerVisible
import org.easybangumi.next.shared.foundation.shimmer.rememberShimmerState
import org.easybangumi.next.shared.foundation.shimmer.shimmer
import org.easybangumi.next.shared.foundation.stringRes

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
fun BangumiDetail(
    modifier: Modifier = Modifier,
    vm: BangumiDetailViewModel,
    nestedScrollConnection: NestedScrollConnection? = null,
    contentPaddingTop: Dp = 0.dp,
) {

    val scope = rememberCoroutineScope()
    val scrollableHeaderState = rememberScrollableHeaderState()
    val behavior = ScrollableHeaderBehavior.discoverScrollHeaderBehavior(
        state = scrollableHeaderState,
    )

    val currentTab = vm.ui.value.currentTab

    val subjectState = vm.ui.value.subjectState
    val subjectShimmerState = rememberShimmerState(
        subjectState.isLoading()
    )

    val characterState = vm.ui.value.characterState
    val personState = vm.ui.value.personState

    val pagerState = rememberPagerState { vm.detailTabList.size }



    ScrollableHeaderScaffold(
        modifier = modifier.fillMaxSize().run {
            if (nestedScrollConnection != null) {
                nestedScroll(nestedScrollConnection)
            } else {
                this
            }
        },
        behavior = behavior,
    ){

        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentPadding = contentPadding,
            userScrollEnabled = false,
        ) {
            val tab = vm.detailTabList.getOrNull(it) ?: return@HorizontalPager
            when (tab) {
                BangumiDetailViewModel.DetailTab.DETAIL -> {
                    BangumiSubPageDetail(
                        vm = vm,
                        modifier = Modifier.fillMaxSize().contentPointerScrollOpt(LocalUIMode.current.inputMode == InputMode.POINTER),
                        subjectState = subjectState,
                        subjectShimmerState = subjectShimmerState,
                        characterState = characterState,
                        personState = personState,
                    )
                }
                BangumiDetailViewModel.DetailTab.COMMENT -> {

                }
                BangumiDetailViewModel.DetailTab.EPISODE -> {

                }
            }
        }


        key(
            subjectState, subjectShimmerState, contentPaddingTop
        ) {
            BangumiDetailHeader(
                vm,
                modifier = Modifier.header(contentPaddingTop).background(MaterialTheme.colorScheme.surfaceContainerLowest),
                contentPaddingTop = contentPaddingTop,
                subject = subjectState,
                subjectShimmerState = subjectShimmerState,
            )
        }


        key(currentTab) {
            Column (modifier = Modifier
                .fillMaxWidth()
                .pinHeader()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                EasyTab(
                    modifier = Modifier.fillMaxWidth(),
                    size = vm.detailTabList.size,
                    selection = currentTab?.index ?: 0,
                    onSelected = {
                        scope.launch {
                            pagerState.animateScrollToPage(it)
                        }
                    }
                ) { index, selected ->
                    val tab = vm.detailTabList[index]
                    Text(
                        text = stringRes(tab.title),
                    )
                }
                HorizontalDivider()
            }
        }
    }

}

@Composable
fun BangumiDetailHeader(
    vm: BangumiDetailViewModel,
    modifier: Modifier,
    contentPaddingTop: Dp,
    subject: DataState<Subject>,
    subjectShimmerState: ShimmerState,
) {
    Column (modifier.padding(32.dp, 0.dp)) {
        Spacer(modifier = Modifier.size(contentPaddingTop))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                CartoonCoverCard(
                    model = vm.coverUrl,
                    itemSize = 206.dp,
                    onClick = { }
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .shimmer(subjectShimmerState)
                ) {
                    Text(
                        text = subject.okOrNull()?.nameCn ?: "",
                        modifier = Modifier.drawRectWhenShimmerVisible(subjectShimmerState),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val displayAirDate = subject.okOrNull()?.displayAirDate
                    if (displayAirDate != null) {
                        Text(
                            text = displayAirDate,
                            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)).padding(16.dp, 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BangumiSubPageDetail(
    vm: BangumiDetailViewModel,
    modifier: Modifier = Modifier,
    subjectState: DataState<Subject>,
    subjectShimmerState: ShimmerState,
    characterState: DataState<List<Character>>,
    personState: DataState<List<Person>>,
){

    val subject = subjectState.okOrNull()

    LazyColumn(
        modifier = modifier.shimmer(subjectShimmerState),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth().shimmer(subjectShimmerState)
            ) {
                if (subject == null) {
                    Text(modifier = Modifier.onShimmerVisible(subjectShimmerState) {
                        weight(1f)
                    },text = "", maxLines = 1)
                    Text(modifier = Modifier.onShimmerVisible(subjectShimmerState) {
                        weight(1f)
                    },text = "", maxLines = 1)
                    Text(modifier = Modifier.onShimmerVisible(subjectShimmerState) {
                        weight(1f)
                    },text = "", maxLines = 1)

                    Row {
                        DetailTag(Modifier.drawRectWhenShimmerVisible(subjectShimmerState), nomeTags)
                        DetailTag(Modifier.drawRectWhenShimmerVisible(subjectShimmerState), nomeTags)
                        DetailTag(Modifier.drawRectWhenShimmerVisible(subjectShimmerState), nomeTags)
                    }
                } else {
                    Text(
                        text = subject.summary ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(modifier = Modifier.fillMaxWidth()) {
                        subject.tags.forEach { tag ->
                            DetailTag(
                                modifier = Modifier,
                                tags = tag,
                                onClick = {
//                                vm.onTagClick(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}

val nomeTags = Tags("")

@Composable
fun DetailTag(
    modifier: Modifier = Modifier,
    tags: Tags,
    onClick: (Tags) -> Unit = {},
){
    val name = tags.name
    if (name != null) {
        FilterChip(false, modifier = modifier, onClick = {
            onClick(tags)
        }, label = {
            Text("$name ${tags.count}")
        })
    }

}