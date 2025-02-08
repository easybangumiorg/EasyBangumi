package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

/// 媒体文件节点，用于存储文件信息
open class MediaFileNode(
    val path: String,
    val name: String,
    val type: MediaFileNodeType,
) {
    override fun toString(): String {
        return "MediaFileNode(path='$path', type=$type, name='$name')"
    }
}