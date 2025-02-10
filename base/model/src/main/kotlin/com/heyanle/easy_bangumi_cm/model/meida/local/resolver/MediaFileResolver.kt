package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaFileNode

/// 媒体文件解析器
class MediaFileResolver {
    fun resolve(node: FileSystemNode): MediaFileNode {
        return MediaFileNode(node.path.toString(), node.name, type = MediaFileNodeType.UNKNOWN)
    }
}