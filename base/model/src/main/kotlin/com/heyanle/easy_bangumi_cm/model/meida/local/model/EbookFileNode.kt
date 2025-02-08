package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

class EbookFileNode (
    path: String,
    name: String,
    block: (EbookFileNode.() -> Unit)? = null
) : MediaFileNode(path, name, MediaFileNodeType.BOOK) {
    var container: String? = null // 容器, 如 png,jpg 取自Naming.ebookFileExtensions
    var author: String? = null

    override fun toString(): String {
        return "EbookFileNode(path='$path', name='$name')"
    }

    init {
        block?.invoke(this)
    }
}