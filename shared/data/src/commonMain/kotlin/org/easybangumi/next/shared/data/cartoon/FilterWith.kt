package org.easybangumi.next.shared.data.cartoon

import org.easybangumi.next.lib.utils.ResourceOr

/**
 * 筛选
 * Created by heyanlin on 2023/11/3.
 */
class FilterWith<T>(
    val id: String,
    val label: ResourceOr,
    val filter: (T) -> Boolean,
)

class FilterState<T> (
    val list: List<FilterWith<T>>,
    val statusMap: Map<String, Int>,
){

    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
        const val STATUS_EXCLUDE = 2
    }
}