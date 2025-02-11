package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.model.AudioFileNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode

class AudioFileResolver(private val nameing: NamingOptions) {

    fun resolve(node: FileSystemNode): AudioFileNode {
        return AudioFileNode(node.path.toString(), node.name)
    }
}