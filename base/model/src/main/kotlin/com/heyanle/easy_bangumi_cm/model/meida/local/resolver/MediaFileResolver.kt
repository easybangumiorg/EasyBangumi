package com.heyanle.easy_bangumi_cm.model.meida.local.resolver

import com.heyanle.easy_bangumi_cm.model.meida.local.NamingOptions
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.MediaFileNode

/// 媒体文件解析器
class MediaFileResolver(private val naming: NamingOptions) {
    private val videoFileResolver = VideoFileResolver(naming)
    private val audioFileResolver = AudioFileResolver(naming)
    private val imageFileResolver = ImageFileResolver(naming)
    private val ebookFileResolver = EBookFileResolver(naming)
    private val subtitleFileResolver = SubtitleFileResolver(naming)

    fun resolve(node: FileSystemNode): MediaFileNode {
        return when {
            naming.videoFileExtensions.contains(node.extension) -> videoFileResolver.resolve(node)
            naming.subtitleFileExtensions.contains(node.extension) -> subtitleFileResolver.resolve(node)
            naming.audioFileExtensions.contains(node.extension) -> audioFileResolver.resolve(node)
            naming.ebookFileExtensions.contains(node.extension) -> ebookFileResolver.resolve(node)
            naming.imageFileExtensions.contains(node.extension) -> imageFileResolver.resolve(node)
            else -> MediaFileNode(node.path.toString(), node.name)
        }
    }
}