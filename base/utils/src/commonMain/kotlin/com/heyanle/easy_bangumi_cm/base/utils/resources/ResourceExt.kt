package com.heyanle.easy_bangumi_cm.base.utils.resources

/**
 * 保持 Any 方便后续迭代拓展资源类型
 * Created by heyanlin on 2025/2/6.
 */
// Label - String StringResource
// Drawable - String DrawableResource ImageVector Bitmap
typealias ResourceOr = Any

fun ResourceOr.isString(): Boolean{
    return this is String
}