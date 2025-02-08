package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

class ImageFileNode(
    path: String,
    name: String,
    block: (ImageFileNode.() -> Unit)? = null
) : MediaFileNode(path, name, MediaFileNodeType.IMAGE) {
    var container: String? = null // 容器, 如 png,jpg 取自Naming.imageFileExtensions

    override fun toString(): String {
        return "ImageFileNode(path='$path', name='$name')"
    }

    init {
        block?.invoke(this)
    }
}