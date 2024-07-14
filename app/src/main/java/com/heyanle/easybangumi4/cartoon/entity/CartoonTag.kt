package com.heyanle.easybangumi4.cartoon.entity

import com.heyanle.easybangumi4.cartoon.star.CartoonInfoSortFilterConst
import com.heyanle.easybangumi4.ui.common.proc.FilterState
import com.heyanle.easybangumi4.utils.stringRes

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
data class CartoonTag (
    val label: String,

    // 排序
    val order: Int,

    // 是否展示
    val show: Boolean,

    // 如果为 false 就走 【全部】 Tag 的配置
    val isCustomSetting: Boolean,

    val sortId: String,
    val isReverse: Boolean,

    val filterState: Map<String, Int>,
){

    companion object {
        const val ALL_TAG_LABEL = "{{all}}"
        const val DEFAULT_TAG_LABEL = "{{default}}"

        val innerLabel: Set<String>
            get() = setOf(ALL_TAG_LABEL, DEFAULT_TAG_LABEL)

        fun create(label: String): CartoonTag {
            return CartoonTag(
                label = label,
                // 默认顺序为 全部 更新 本地 自定义
                // 自定义默认排序从 0 开始，内部标签设置为负数能保证在用户手动排序之前内部便签在最前
                order = when(label){
                    ALL_TAG_LABEL -> -3
                    else -> 0
                 },
                show = when(label){
                    ALL_TAG_LABEL -> false
                    DEFAULT_TAG_LABEL -> true
                    else -> true
                },
                isCustomSetting = when(label){
                    ALL_TAG_LABEL -> true
                    DEFAULT_TAG_LABEL -> false
                    else -> false
                },
                sortId = CartoonInfoSortFilterConst.sortByStarTime.id,
                isReverse = false,
                filterState = emptyMap(),
            )
        }
    }



    val isInner: Boolean
        get() = label == ALL_TAG_LABEL || label == DEFAULT_TAG_LABEL


    val isAll: Boolean
        get() = label == ALL_TAG_LABEL
    val isDefault: Boolean
        get() = label == DEFAULT_TAG_LABEL


    val isInFilter: Boolean by lazy {
        filterState.filter { it.value != FilterState.STATUS_OFF }.isNotEmpty()
    }

    val display by lazy {
        when (label) {
            ALL_TAG_LABEL -> {
                stringRes(com.heyanle.easy_i18n.R.string.all_word)
            }
            DEFAULT_TAG_LABEL -> {
                stringRes(com.heyanle.easy_i18n.R.string.default_word)
            }
            else -> {
                label
            }
        }
    }
}