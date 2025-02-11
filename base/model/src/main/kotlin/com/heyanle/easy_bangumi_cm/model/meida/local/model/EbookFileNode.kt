package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

class EbookFileNode (
    path: String,
    name: String,
) : MediaFileNode(path, name, MediaFileNodeType.EBOOK) {
    var container: String? = null // 容器, 如 txt,epub 取自Naming.ebookFileExtensions
    var author: String? = null

    override fun toString(): String {
        return "EbookFileNode(path='$path', name='$name')"
    }
}