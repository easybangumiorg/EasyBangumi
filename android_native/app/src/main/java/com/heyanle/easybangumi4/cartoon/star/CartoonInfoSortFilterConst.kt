package com.heyanle.easybangumi4.cartoon.star

import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.ui.common.proc.FilterWith
import com.heyanle.easybangumi4.ui.common.proc.SortBy
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanle on 2024/6/8.
 * https://github.com/heyanLE
 */
object CartoonInfoSortFilterConst {




    // 排序配置 ================================================
    val sortByStarTime = SortBy<CartoonInfo>(
        id = "CreateTime",
        label = stringRes(R.string.sort_by_create_time),
        comparator = { o1, o2 ->
            o1.starTime.compareTo(o2.starTime)
        }
    )
    val sortByUpdateTime = SortBy<CartoonInfo>(
        id = "UpdateTime",
        label = stringRes(R.string.sort_by_update_time),
        comparator = { o1, o2 ->
            o1.lastUpdateTime.compareTo(o2.lastUpdateTime)
        }
    )
    val sortByTitle = SortBy<CartoonInfo>(
        id = "Title",
        label = stringRes(R.string.sort_by_title),
        comparator = { o1, o2 ->
            o1.name.compareTo(o2.name)
        }
    )
    val sortByLastWatchTime = SortBy<CartoonInfo>(
        id = "LastWatchTime",
        label = stringRes(R.string.sort_by_watch),
        comparator = { o1, o2 ->
            o1.lastHistoryTime.compareTo(o2.lastHistoryTime)
        }
    )
    val sortBySource = SortBy<CartoonInfo>(
        id = "Source",
        label = stringRes(R.string.sort_by_source),
        comparator = { o1, o2 ->
            o1.source.compareTo(o2.source)
        }
    )



    // 筛选配置 ================================================
    val filterWithUpdate = FilterWith<CartoonInfo>(
        id = "Update",
        label = stringRes(R.string.filter_with_is_update),
        filter = {
            it.isUpdate
        }
    )
    val filterWithTag = FilterWith<CartoonInfo>(
        id = "Tag",
        label = stringRes(R.string.filter_with_has_tag),
        filter = {
            it.tags.isNotEmpty()
        }
    )
    val filterWithUp = FilterWith<CartoonInfo>(
        id = "Up",
        label = stringRes(R.string.filter_with_is_up),
        filter = {
            it.upTime != 0L
        }
    )
    val filterLocalSource = FilterWith<CartoonInfo>(
        id = "Up",
        label = stringRes(R.string.local_source),
        filter = {
            it.isLocal
        }
    )


    val sortByList = listOf<SortBy<CartoonInfo>>(
        sortByStarTime, sortByUpdateTime, sortByTitle, sortByLastWatchTime, sortBySource
    )
    val filterWithList = listOf(
        filterWithUpdate, filterWithTag, filterWithUp, filterLocalSource
    )


}