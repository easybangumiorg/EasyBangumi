package org.easybangumi.next.shared.compose.media_finder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.easybangumi.next.shared.compose.media_finder_old.logger
import org.easybangumi.next.shared.compose.media_finder_old.radar.MediaRadar
import org.easybangumi.next.shared.compose.media_finder_old.search.MediaSearcher
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

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
fun MediaFinderHost(
    vm: MediaFinderVM
) {
    val state = vm.ui.value
    val bottomSheet = rememberModalBottomSheetState(false)
    LaunchedEffect(state.panelShow, bottomSheet.isVisible) {
        logger.info("MediaRadarBottomPanel LaunchedEffect show: ${state.panelShow}")
        if (state.panelShow && !bottomSheet.isVisible) {
            bottomSheet.show()
        } else if (!state.panelShow) {
            bottomSheet.hide()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    if (state.panelShow) {
        ModalBottomSheet(
            onDismissRequest = {
                vm.hidePanel()
            },
            sheetState = bottomSheet,
            contentWindowInsets = {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Top)
            }
        ) {
            ListItem(
                leadingContent = {
                    Text(
                        text = "搜索词：",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                headlineContent = {
                    Text(
                        text = state.keyword ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = {
                    IconButton(onClick = {
                        vm.showKeywordEditPopup()
                    }){
                        Icon(Icons.Filled.Edit, "edit")
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 0.dp).align(Alignment.CenterHorizontally),
            ) {
                SegmentedButton(
                    selected = vm.pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            vm.pagerState.scrollToPage(0)
                        }
                    },
                    label = {
                        Text(text = vm.labelList[0])
                    },
                    shape = RoundedCornerShape(32.dp, 0.dp, 0.dp, 32.dp)
                )
                SegmentedButton(
                    selected = vm.pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            vm.pagerState.scrollToPage(1)
                        }
                    },
                    label = {
                        Text(text = vm.labelList[1])
                    },
                    shape = RoundedCornerShape(0.dp, 32.dp, 32.dp, 0.dp)
                )
            }


            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = vm.pagerState,
                userScrollEnabled = false,
                beyondViewportPageCount = 2,
            ) {
                when(it) {
                    0 -> {
                        Radar(
                            modifier = Modifier.fillMaxSize(),
                            vm = vm
                        )
                    }
                    1 -> {
                        Search(
                            modifier = Modifier.fillMaxSize(),
                            vm = vm
                        )
                    }
                    else -> {}
                }
            }

        }
    }

    when (val pop = state.popup) {
        is MediaFinderVM.Popup.KeywordEdit -> {
            FinderEditDialog(vm, pop)
        }
        else -> {}
    }

}

@Composable
fun FinderEditDialog(
    vm: MediaFinderVM,
    state: MediaFinderVM.Popup.KeywordEdit,
) {
    val fieldText = remember {
        mutableStateOf(state.currentKeyword)
    }
    val showSuggestAll = remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = {
            vm.dismissPopup()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("编辑关键词", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Help, contentDescription = "")
                }
            }


        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fieldText.value,
                    onValueChange = {
                        fieldText.value = it
                    },
                    singleLine = true,
                    label = {
                        Text("搜索关键词")
                    }
                )

                Spacer(Modifier.size(16.dp))
                Text("别名建议", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.keywordList) {string ->
                        Row(modifier = Modifier
//                                .widthIn(max = 120.dp)

                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                fieldText.value = string
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(
                                text = string,
                                modifier = Modifier.fillMaxWidth()

                            )
                        }

//                        ListItem(
//                            modifier = Modifier.clickable {
//                                fieldText.value = string
//                            },
//                            headlineContent = {
//                                Text(string)
//                            },
//
//                            colors = ListItemDefaults.colors(
//                                containerColor = Color.Transparent,
//                            )
//                        )
                    }

                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    vm.changeKeyword(fieldText.value)
                }
            ) {
                Text(stringRes(Res.strings.search))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    vm.dismissPopup()
                }
            ) {
                Text(stringRes(Res.strings.cancel))
            }
        }
    )
}
