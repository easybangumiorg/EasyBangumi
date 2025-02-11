package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaFileNodeType
import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.StubType

// 视频文件节点
class VideoFileNode(
    path: String,
    name: String,
) : MediaFileNode(path, name, MediaFileNodeType.VIDEO) {
    var year: String? = null
    var container: String? = null // 容器, 如 mkv, mp4  取自Naming.videoFileExtensions
    var stub: StubType = StubType.UNKNOWN

    override fun toString(): String {
        return "VideoFileNode(path='$path', name='$name', year=$year, container=$container, stub=$stub)"
    }
}