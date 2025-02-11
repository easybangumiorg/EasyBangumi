package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.ImageFileNode

class ImageFileResolver(private val naming: NamingOptions) {
    fun resolve(node: FileSystemNode): ImageFileNode {
        return ImageFileNode(node.path.toString(), node.name)
    }
}