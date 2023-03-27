package com.heyanle.easybangumi4.ui.search.searchpage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.heyanle.bangumi_source_api.api.component.search.SearchComponent

/**
 * Created by HeYanLe on 2023/3/27 22:57.
 * https://github.com/heyanLE
 */
@Composable
fun SearchPage(
    searchComponent: SearchComponent,
    historyKey: SnapshotStateList<String>,
    onHistoryKeyClick: (String)->Unit,
){



}