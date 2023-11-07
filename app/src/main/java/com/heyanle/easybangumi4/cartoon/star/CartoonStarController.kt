package com.heyanle.easybangumi4.cartoon.star

import com.heyanle.easybangumi4.base.preferences.android.AndroidPreferenceStore
import com.heyanle.easybangumi4.cartoon.entity.CartoonStar
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonStarDao
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
 * 番剧收藏，主要处理置顶，排序还筛选
 * Created by heyanlin on 2023/11/6.
 */
class CartoonStarController(
    private val cartoonStarDao: CartoonStarDao,
    private val sharePreferenceStore: AndroidPreferenceStore,
) {

    private val scope = MainScope()

    // 排序配置
    private val sortByCreateTime = SortBy<CartoonStar>(
        id = "CreateTime",
        label = stringRes(com.heyanle.easy_i18n.R.string.sort_by_create_time),
        comparator = { o1, o2 ->
            (o1.createTime - o2.createTime).toInt()
        }
    )
    private val sortByUpdateTime = SortBy<CartoonStar>(
        id = "UpdateTime",
        label = stringRes(com.heyanle.easy_i18n.R.string.sort_by_update_time),
        comparator = { o1, o2 ->
            (o1.lastUpdateTime - o2.lastUpdateTime).toInt()
        }
    )
    private val sortByTitle = SortBy<CartoonStar>(
        id = "Title",
        label = stringRes(com.heyanle.easy_i18n.R.string.sort_by_title),
        comparator = { o1, o2 ->
            o1.title.compareTo(o2.title)
        }
    )
    private val sortByLastWatchTime = SortBy<CartoonStar>(
        id = "LastWatchTime",
        label = stringRes(com.heyanle.easy_i18n.R.string.sort_by_watch),
        comparator = { o1, o2 ->
            (o1.lastWatchTime - o2.lastWatchTime).toInt()
        }
    )
    private val sortBySource = SortBy<CartoonStar>(
        id = "Source",
        label = stringRes(com.heyanle.easy_i18n.R.string.sort_by_source),
        comparator = { o1, o2 ->
            o1.source.compareTo(o2.source)
        }
    )
    private val sortByList = listOf<SortBy<CartoonStar>>(
        sortByCreateTime, sortByUpdateTime, sortByTitle, sortByLastWatchTime, sortBySource
    )

    // 当前选择排序
    private val sortIdShare =
        sharePreferenceStore.getString("cartoon_star_sort_id", sortByCreateTime.id)

    // 当前是否反转排序
    private val sortIsReverseShare =
        sharePreferenceStore.getBoolean("cartoon_star_sort_is_reverse", false)
    val sortState = SortState(
        sortList = sortByList,
        isReverse = sortIsReverseShare.stateIn(scope),
        current = sortIdShare.stateIn(scope)
    )


    // 筛选配置
    private val filterWithUpdate = FilterWith<CartoonStar>(
        id = "Update",
        label = stringRes(com.heyanle.easy_i18n.R.string.filter_with_is_update),
        filter = {
            it.isUpdate
        }
    )
    private val filterWithTag = FilterWith<CartoonStar>(
        id = "Tag",
        label = stringRes(com.heyanle.easy_i18n.R.string.filter_with_has_tag),
        filter = {
            it.tags.isNotEmpty()
        }
    )
    private val filterWithUp = FilterWith<CartoonStar>(
        id = "Up",
        label = stringRes(com.heyanle.easy_i18n.R.string.filter_with_is_up),
        filter = {
            it.isUp()
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

    val filterState = FilterState<CartoonStar>(
        list = filterWithList,
        statusMap = filterMap
    )


    fun onFilterChange(filterWith: FilterWith<CartoonStar>, state: Int) {
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

    fun onSortChange(sortBy: SortBy<CartoonStar>, state: Int) {
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
    fun flowCartoon(): Flow<List<CartoonStar>> {
        return combine(
            sortState.current,
            sortState.isReverse,
            filterState.statusMap.distinctUntilChanged(),
            cartoonStarDao.flowAll()
        ) { currentSortId, isSortReverse, filterStateMap, starList ->
            val currentSort = sortByList.find { it.id == currentSortId } ?: sortByCreateTime
            val onFilter = filterWithList.filter {
                filterStateMap[it.id] == FilterState.STATUS_ON
            }
            val excludeFilter = filterWithList.filter {
                filterStateMap[it.id] == FilterState.STATUS_EXCLUDE
            }
            starList.filter {
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
            }.sortedWith { o1, o2 ->
                val res = if (o1.isUp() && o2.isUp()) {
                    (o2.upTime - o1.upTime).toInt()
                } else if (o1.isUp() == o2.isUp()) {
                    currentSort.comparator.compare(o1, o2)
                } else if (o1.isUp()) {
                    1
                } else {
                    -1
                }
                if (isSortReverse) -res else res
            }
        }
    }


}