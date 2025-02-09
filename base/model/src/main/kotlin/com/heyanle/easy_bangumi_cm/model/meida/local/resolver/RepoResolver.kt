package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.RepoCanNotBeFileError
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaNode

class RepoResolver {

    @Throws(RepoCanNotBeFileError::class)
    fun resolve(node: FileSystemNode): MediaNode = MediaNode {
        path = node.path
        name = node.name
        if (!node.isDir)
            throw RepoCanNotBeFileError(node.path.toString())
        type = MediaNodeType.ROOT
        isRoot = true


    }
}