package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

class AudioFileNode(
    path: String,
    name: String,
    block: (AudioFileNode.() -> Unit)? = null
) : MediaFileNode(path, name, MediaFileNodeType.AUDIO) {
    var container: String? = null // 容器, 如 mp3,wav 取自Naming.audioFileExtensions
    var author: String? = null

    override fun toString(): String {
        return "AudioFileNode(path='$path', name='$name')"
    }

    init {
        block?.invoke(this)
    }
}