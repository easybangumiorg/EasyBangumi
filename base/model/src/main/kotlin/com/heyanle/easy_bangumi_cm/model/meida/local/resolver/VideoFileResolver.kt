package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.VideoFileNode

/// 视频文件名解析器
class VideoFileResolver(naming: NamingOptions) {

    fun resolve(node: FileSystemNode): VideoFileNode {
        return VideoFileNode(node.path.toString(), node.name).apply {
            container = node.extension
        }
        // TODO 在二次解析时，需要追溯父目录和祖父目录的文件名
    }
}