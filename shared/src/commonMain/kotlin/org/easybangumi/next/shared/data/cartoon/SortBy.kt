package org.easybangumi.next.shared.data.cartoon

import org.easybangumi.next.lib.utils.ResourceOr


/**
 * 排序项目
 * Created by heyanlin on 2023/11/3.
 */
class SortBy<T>(
    val id: String,
    val label: ResourceOr,
    val comparator: Comparator<T>,
)

data class SortState<T>(
    val sortList: List<SortBy<T>>,
    val current: String,
    val isReverse: Boolean,
){
    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
        const val STATUS_REVERSE = 2
    }
}
