package com.heyanle.easybangumi4.ui.common.proc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.navigationCartoonTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

/**
 * 排序 - 筛选 面板
 * Created by heyanlin on 2023/11/3.
 */
@Composable
fun <T> FilterColumn(
    modifier: Modifier = Modifier,
    filterState: FilterState<T>,
){}

// 搜索 Column
@Composable
fun <T> SortColumn(
    modifier: Modifier = Modifier,
    sortState: SortState<T>,
) {
    val current = sortState.current.collectAsState()
    val isReverse = sortState.isReverse.collectAsState()
    Column(
        modifier = modifier
    ) {
        sortState.sortList.forEach {
            val status = remember(current, isReverse) {
                if (current.value != it) {
                    0
                } else if (isReverse.value) {
                    2
                } else {
                    1
                }
            }
            SortItem(sortBy = it, status = status, onClick = { item ->
                when (status) {
                    0 -> {
                        sortState.current.update {
                            item
                        }
                    }
                    1 -> {
                        sortState.isReverse.update {
                            true
                        }
                    }
                    else -> {
                        sortState.isReverse.update {
                            false
                        }
                    }
                }
            })
        }
    }
}

@Composable
fun <T> SortItem(
    sortBy: SortBy<T>,
    status: Int, // 0->off 1->on 2->reverse
    onClick: (SortBy<T>) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {
            onClick(sortBy)
        },
        headlineContent = {
            Text(text = sortBy.label)
        },
        leadingContent = {
            when (status) {
                1 -> {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = sortBy.label
                    )
                }

                2 -> {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = sortBy.label
                    )
                }

                else -> {
                    Box(modifier = Modifier.size(24.dp))
                }
            }
        }
    )
}
