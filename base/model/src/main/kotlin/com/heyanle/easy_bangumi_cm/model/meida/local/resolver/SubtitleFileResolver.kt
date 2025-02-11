package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.SubtitleFileNode

class SubtitleFileResolver(private val naming: NamingOptions) {
    fun resolve(node: FileSystemNode): SubtitleFileNode {
        return SubtitleFileNode(node.path.toString(), node.name)
    }
}