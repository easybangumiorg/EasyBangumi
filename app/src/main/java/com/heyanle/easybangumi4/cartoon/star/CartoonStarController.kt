package com.heyanle.easybangumi4.cartoon.star

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.ui.common.proc.FilterWith
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.ui.common.proc.SortState
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.easybangumi4.utils.toJson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Created by heyanle on 2023/12/17.
 * https://github.com/heyanLE
 */
class CartoonStarController(
    private val cartoonInfoDao: CartoonInfoDao,
    private val sharePreferenceStore: AndroidPreferenceStore,
) {

    private val scope = MainScope()

    // 排序配置
    private val sortByStarTime = SortBy<CartoonInfo>(
        id = "CreateTime",
        label = stringRes(R.string.sort_by_create_time),
        comparator = { o1, o2 ->
            o1.starTime.compareTo(o2.starTime)
        }
    )
    private val sortByUpdateTime = SortBy<CartoonInfo>(
        id = "UpdateTime",
        label = stringRes(R.string.sort_by_update_time),
        comparator = { o1, o2 ->
            o1.lastUpdateTime.compareTo(o2.lastUpdateTime)
        }
    )
    private val sortByTitle = SortBy<CartoonInfo>(
        id = "Title",
        label = stringRes(R.string.sort_by_title),
        comparator = { o1, o2 ->
            o1.name.compareTo(o2.name)
        }
    )
    private val sortByLastWatchTime = SortBy<CartoonInfo>(
        id = "LastWatchTime",
        label = stringRes(R.string.sort_by_watch),
        comparator = { o1, o2 ->
            o1.lastHistoryTime.compareTo(o2.lastHistoryTime)
        }
    )
    private val sortBySource = SortBy<CartoonInfo>(
        id = "Source",
        label = stringRes(R.string.sort_by_source),
        comparator = { o1, o2 ->
            o1.source.compareTo(o2.source)
        }
    )
    private val sortByList = listOf<SortBy<CartoonInfo>>(
        sortByStarTime, sortByUpdateTime, sortByTitle, sortByLastWatchTime, sortBySource
    )

    // 当前选择排序
    private val sortIdShare =
        sharePreferenceStore.getString("cartoon_star_sort_id", sortByStarTime.id)

    // 当前是否反转排序
    private val sortIsReverseShare =
        sharePreferenceStore.getBoolean("cartoon_star_sort_is_reverse", false)
    val sortState = SortState(
        sortList = sortByList,
        isReverse = sortIsReverseShare.stateIn(scope),
        current = sortIdShare.stateIn(scope)
    )


    // 筛选配置
    private val filterWithUpdate = FilterWith<CartoonInfo>(
        id = "Update",
        label = stringRes(R.string.filter_with_is_update),
        filter = {
            it.isUpdate
        }
    )
    private val filterWithTag = FilterWith<CartoonInfo>(
        id = "Tag",
        label = stringRes(R.string.filter_with_has_tag),
        filter = {
            it.tags.isNotEmpty()
        }
    )
    private val filterWithUp = FilterWith<CartoonInfo>(
        id = "Up",
        label = stringRes(R.string.filter_with_is_up),
        filter = {
            it.upTime != 0L
        }
    )
    private val filterWithList = listOf(
        filterWithUpdate, filterWithTag, filterWithUp
    )
    private val filterMapStringShare =
        sharePreferenceStore.getString("cartoon_star_filter_map", "{}")
    private val filterMap = filterMapStringShare.flow().map {
        it.jsonTo<Map<String, Int>>() ?: emptyMap()
    }.stateIn(
        scope,
        SharingStarted.Lazily,
        filterMapStringShare.get().jsonTo<Map<String, Int>>() ?: emptyMap()
    )

    val filterState = FilterState<CartoonInfo>(
        list = filterWithList,
        statusMap = filterMap
    )


    fun onFilterChange(filterWith: FilterWith<CartoonInfo>, state: Int) {
        val currentMap = filterMap.value.toMutableMap()
        when (state) {
            FilterState.STATUS_OFF -> {
                currentMap[filterWith.id] = FilterState.STATUS_ON
            }

            FilterState.STATUS_ON -> {
                currentMap[filterWith.id] = FilterState.STATUS_EXCLUDE
            }

            else -> {
                currentMap[filterWith.id] = FilterState.STATUS_OFF
            }
        }
        filterMapStringShare.set(currentMap.toJson())
    }

    fun onSortChange(sortBy: SortBy<CartoonInfo>, state: Int) {
        when (state) {
            SortState.STATUS_OFF -> {
                sortIdShare.set(sortBy.id)
                sortIsReverseShare.set(false)
            }

            SortState.STATUS_ON -> {
                sortIdShare.set(sortBy.id)
                sortIsReverseShare.set(true)
            }

            else -> {
                sortIdShare.set(sortBy.id)
                sortIsReverseShare.set(false)
            }
        }
    }

    // 处理置顶 - 排序 - 筛选
    fun flowCartoon(): Flow<List<CartoonInfo>> {
        return combine(
            sortState.current,
            sortState.isReverse,
            filterState.statusMap.distinctUntilChanged(),
            cartoonInfoDao.flowAllStar()
        ) { currentSortId, isSortReverse, filterStateMap, starList ->
            val currentSort = sortByList.find { it.id == currentSortId } ?: sortByStarTime
            val onFilter = filterWithList.filter {
                filterStateMap[it.id] == FilterState.STATUS_ON
            }
            val excludeFilter = filterWithList.filter {
                filterStateMap[it.id] == FilterState.STATUS_EXCLUDE
            }
            val list = starList.filter {
                var check = true
                for (filterWith in onFilter) {
                    if (!filterWith.filter(it)) {
                        check = false
                        break
                    }
                }
                if (!check) {
                    return@filter false
                }
                for (filterWith in excludeFilter) {
                    if (filterWith.filter(it)) {
                        check = false
                        break
                    }
                }
                check
            }
//                .sortedWith { o1, o2 ->
//                    val res = if (o1.isUp() && o2.isUp()) {
//                        (o2.upTime - o1.upTime).toInt()
//                    } else if (o1.isUp() == o2.isUp()) {
//                        currentSort.comparator.compare(o1, o2)
//                    } else if (o1.isUp()) {
//                        1
//                    } else {
//                        -1
//                    }
//                    if (isSortReverse) -res else res
//                }
            val upList =
                list.filter { it.isUp() }.sortedWith { o1, o2 -> (o2.upTime - o1.upTime).toInt() }
            val normalList = list.filter { !it.isUp() }.sortedWith() { o1, o2 ->
                val res = currentSort.comparator.compare(o1, o2)
                if (isSortReverse) -res else res
            }
            upList + normalList
        }
    }

}