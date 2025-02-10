package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaNode

class ProjectResolver(naming: NamingOptions) {
    private val mediaFileResolver = MediaFileResolver(naming)

    fun resolve(node: FileSystemNode, front: MediaNode): MediaNode = MediaNode {
        path = node.path
        name = node.name
        type = if (front.isRoot) {
            MediaNodeType.PROJECT
        } else {
            MediaNodeType.SUBPROJECT
        }

        resources = node.children.filter { !it.isDir }.map {
            mediaFileResolver.resolve(it)
        }.toMutableList()

        children = node.children.filter { it.isDir }.map {
            resolve(it, this)
        }.toMutableList()

        if (isEmpty) {
            type = MediaNodeType.EMPTY
        }
    }
}