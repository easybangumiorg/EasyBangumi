package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.star.CartoonInfoSortFilterConst
import com.heyanle.easybangumi4.cartoon.star.CartoonStarController

/**
 * Created by heyanle on 2024/6/9.
 * https://github.com/heyanLE
 */
class CartoonTagWrapper(
    val cartoonTag: CartoonTag,
    val tagSortFilterState: TagSortFilterStateItem
) {

    data class TagSortFilterStateItem(
        val tagId: Int,

        // 如果为 false 就走缺省配置
        val isCustomSetting: Boolean,

        val sortId: String,
        val isReverse: Boolean,

        val filterState: Map<String, Int>,
    ) {
        companion object {
            val default = TagSortFilterStateItem(
                tagId = CartoonStarController.DEFAULT_STATE_ID,
                isCustomSetting = false,
                sortId = CartoonInfoSortFilterConst.sortByStarTime.id,
                isReverse = false,
                filterState = emptyMap()
            )
        }
    }

    val id: Int
        get() = cartoonTag.id

    val order: Int
        get() = cartoonTag.order

    val label: String
        get() = cartoonTag.label
}