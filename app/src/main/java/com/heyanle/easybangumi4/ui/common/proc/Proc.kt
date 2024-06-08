package com.heyanle.easybangumi4.ui.common.proc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.navigationCartoonTag
import com.heyanle.easybangumi4.utils.logi
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
    onFilterClick: (FilterWith<T>, Int) -> Unit,
) {

    val statusMap = filterState.statusMap
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        filterState.list.forEach { filterWith ->
            val status = statusMap[filterWith.id] ?: FilterState.STATUS_OFF

            FilterItem(item = filterWith, status = status, onClick = { item ->
                onFilterClick(item, status)
            })
        }
    }
}

@Composable
fun <T> FilterItem(
    item: FilterWith<T>,
    status: Int,
    onClick: (FilterWith<T>) -> Unit
) {

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.clickable {
            onClick(item)
        },
        headlineContent = {
            Text(text = item.label)
        },
        leadingContent = {
            val state: ToggleableState = remember(status) {
                when (status) {
                    FilterState.STATUS_EXCLUDE -> {
                        ToggleableState.Indeterminate
                    }

                    FilterState.STATUS_ON -> {
                        ToggleableState.On
                    }

                    else -> {
                        ToggleableState.Off
                    }
                }
            }
            TriStateCheckbox(state = state, onClick = { onClick(item) })
        }
    )
}

@Composable
fun <T> SortDropDownMenu(
    modifier: Modifier = Modifier,
    isShow: Boolean,
    sortState: SortState<T>,
    onClick: (SortBy<T>, Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val current = sortState.current
    val isReverse = sortState.isReverse
    DropdownMenu(modifier = modifier, expanded = isShow, onDismissRequest = onDismissRequest) {
        sortState.sortList.forEach {
            val status = if (current != it.id) {
                SortState.STATUS_OFF
            } else if (isReverse) {
                SortState.STATUS_REVERSE
            } else {
                SortState.STATUS_ON
            }
            SortDropDownMenuItem(sortBy = it, status = status, onClick = { item ->
                onClick(it, status)
            })
        }
    }
}

// 筛选 Column
@Composable
fun <T> SortColumn(
    modifier: Modifier = Modifier,
    sortState: SortState<T>,
    onClick: (SortBy<T>, Int) -> Unit
) {
    val current = sortState.current
    val isReverse = sortState.isReverse
    LaunchedEffect(key1 = current) {
        current.logi("Proc")
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        sortState.sortList.forEach {
            val status = if (current != it.id) {
                SortState.STATUS_OFF
            } else if (isReverse) {
                SortState.STATUS_REVERSE
            } else {
                SortState.STATUS_ON
            }
            SortItem(sortBy = it, status = status, onClick = { item ->
                onClick(it, status)
            })
        }
    }
}


@Composable
fun <T> SortDropDownMenuItem(
    sortBy: SortBy<T>,
    status: Int, // 0->off 1->on 2->reverse
    onClick: (SortBy<T>) -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(text = sortBy.label)
        },
        leadingIcon = {
            when (status) {
                SortState.STATUS_ON -> {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = sortBy.label
                    )
                }

                SortState.STATUS_REVERSE -> {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        contentDescription = sortBy.label
                    )
                }

                else -> {
                    Icon(
                        Icons.Filled.ArrowDownward,
                        modifier = Modifier.alpha(0f),
                        contentDescription = sortBy.label
                    )
                }
            }
        },
        onClick = {
            onClick(sortBy)
        })
}

@Composable
fun <T> SortItem(
    sortBy: SortBy<T>,
    status: Int, // 0->off 1->on 2->reverse
    onClick: (SortBy<T>) -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier.clickable {
            onClick(sortBy)
        },
        headlineContent = {
            Text(text = sortBy.label)
        },
        leadingContent = {
            when (status) {
                SortState.STATUS_ON -> {
                    Icon(
                        Icons.Filled.ArrowUpward,
                        contentDescription = sortBy.label
                    )
                }

                SortState.STATUS_REVERSE -> {
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
