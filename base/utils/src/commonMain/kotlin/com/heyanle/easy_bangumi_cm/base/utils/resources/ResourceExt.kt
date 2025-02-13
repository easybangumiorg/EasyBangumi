package com.heyanle.easy_bangumi_cm.base.utils.resources

/**
 * 保持 Any 方便后续迭代拓展资源类型
 * Created by heyanlin on 2025/2/6.
 */
// Resource or String
typealias ResourceOr = Any

fun ResourceOr.isString(): Boolean{
    return this is String
}