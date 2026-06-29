package org.easybangumi.next.shared.data.cartoon

import kotlinx.serialization.Serializable

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
@Serializable
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

    // 标签类型
    val type: String = TYPE_LOCAL,
){

    companion object {
        const val TYPE_LOCAL = "local"
        const val TYPE_BANGUMI = "bangumi"

        const val BANGUMI_TAG_LABEL = "{{bangumi}}"
        const val DEFAULT_TAG_LABEL = "{{default}}"


        fun create(label: String, type: String = TYPE_LOCAL): CartoonTag {
            return CartoonTag(
                label = label,
                // 默认顺序为 全部 更新 本地 自定义
                // 自定义默认排序从 0 开始，内部标签设置为负数能保证在用户手动排序之前内部便签在最前
                order = when(label){
                    BANGUMI_TAG_LABEL -> -2
                    DEFAULT_TAG_LABEL -> -3
                    else -> 0
                 },
                show = when(label){
                    DEFAULT_TAG_LABEL -> true
                    else -> true
                },
                isCustomSetting = when(label){
                    DEFAULT_TAG_LABEL -> false
                    else -> false
                },
                type = type,
                sortId = "",
                isReverse = false,
                filterState = emptyMap(),
            )
        }
    }




    val isLocal: Boolean
        get() = type == TYPE_LOCAL



    val isDefault: Boolean
        get() = label == DEFAULT_TAG_LABEL
    val isBangumi: Boolean
        get() = label == BANGUMI_TAG_LABEL



    val isInFilter: Boolean by lazy {
        filterState.filter { it.value != FilterState.STATUS_OFF }.isNotEmpty()
    }


}