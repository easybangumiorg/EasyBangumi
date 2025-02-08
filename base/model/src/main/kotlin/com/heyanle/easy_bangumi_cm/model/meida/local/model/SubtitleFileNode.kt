package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType

/// 字幕文件节点
class SubtitleFileNode(
    path: String,
    name: String,
    block: (SubtitleFileNode.() -> Unit)? = null
) : MediaFileNode(path, name, MediaFileNodeType.SUBTITLE) {
    var container: String? = null // 容器, 如 ass,srt 取自Naming.subtitleFileExtensions

    override fun toString(): String {
        return "SubtitleFileNode(path='$path', name='$name')"
    }

    init {
        block?.invoke(this)
    }
}