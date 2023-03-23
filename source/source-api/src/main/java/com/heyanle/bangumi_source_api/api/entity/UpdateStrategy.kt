package com.heyanle.bangumi_source_api.api.entity

import androidx.annotation.Keep

/**
 *
 * 单个番剧的更新策略，在库更新时起作用
 *
 * Created by HeYanLe on 2023/2/18 21:11.
 * https://github.com/heyanLE
 */
@Keep
enum class UpdateStrategy {

    /**
     * 无论自动更新还是手动更新都会更新
     */
    ALWAYS,

    /**
     * 只有手动更新时才会更新，一般用于已完结
     */
    ONLY_MANUAL,

    /**
     * 不更新，一般用于剧场版或年代久远不可能更新的番剧
     */
    NEVER,

}