package org.easybangumi.next.shared.cartoon.collection

import org.easybangumi.next.shared.data.cartoon.CartoonInfo
import org.easybangumi.next.shared.data.cartoon.FilterWith
import org.easybangumi.next.shared.data.cartoon.SortBy
import org.easybangumi.next.shared.resources.Res

/**
 * Created by heyanle on 2024/6/8.
 * https://github.com/heyanLE
 */
object CartoonInfoSortFilterConst {

    // 排序配置 ================================================
    val sortByStarTime = SortBy<CartoonInfo>(
        id = "CreateTime",
        label = Res.strings.sort_by_create_time,
        comparator = { o1, o2 ->
            o1.starTime.compareTo(o2.starTime)
        }
    )
    val sortByUpdateTime = SortBy<CartoonInfo>(
        id = "UpdateTime",
        label = Res.strings.sort_by_update_time,
        comparator = { o1, o2 ->
            o1.lastHistoryTime.compareTo(o2.lastUpdateTime)
        }
    )
    val sortByTitle = SortBy<CartoonInfo>(
        id = "Title",
        label = Res.strings.sort_by_title,
        comparator = { o1, o2 ->
            o1.name.compareTo(o2.name)
        }
    )
    val sortByLastWatchTime = SortBy<CartoonInfo>(
        id = "LastWatchTime",
        label = Res.strings.sort_by_watch,
        comparator = { o1, o2 ->
            o1.lastHistoryTime.compareTo(o2.lastHistoryTime)
        }
    )
    val sortBySource = SortBy<CartoonInfo>(
        id = "Source",
        label = Res.strings.sort_by_source,
        comparator = { o1, o2 ->
            o1.fromSourceKey.compareTo(o2.fromSourceKey)
        }
    )



    // 筛选配置 ================================================
    val filterWithUpdate = FilterWith<CartoonInfo>(
        id = "Update",
        label = Res.strings.filter_with_is_update,
        filter = {
            it.lastUpdateTime != 0L
        }
    )
    val filterWithTag = FilterWith<CartoonInfo>(
        id = "Tag",
        label = Res.strings.filter_with_has_tag,
        filter = {
            it.tagList.isNotEmpty()
        }
    )
    val filterWithPin = FilterWith<CartoonInfo>(
        id = "Pin",
        label = Res.strings.filter_with_is_up,
        filter = {
            it.pinTime != 0L
        }
    )
//    val filterLocalSource = FilterWith<CartoonInfo>(
//        id = "Local",
//        label = Res.strings.local_source,
//        filter = {
//            it.fromSourceKey == LocalSource.key
//        }
//    )


    val sortByList = listOf<SortBy<CartoonInfo>>(
        sortByStarTime, sortByUpdateTime, sortByTitle, sortByLastWatchTime, sortBySource
    )
    val filterWithList = listOf(
        filterWithUpdate, filterWithTag, filterWithPin,
    )


}