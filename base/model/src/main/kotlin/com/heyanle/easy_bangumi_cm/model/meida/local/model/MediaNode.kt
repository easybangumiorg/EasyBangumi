package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType
import java.nio.file.Path
import kotlin.properties.Delegates

/// 媒体节点
class MediaNode {
    var path: Path by Delegates.notNull()
    var name: String by Delegates.notNull()
    var isRoot: Boolean = false
    var type: MediaNodeType = MediaNodeType.UNKNOWN

    val children: MutableList<MediaNode> = mutableListOf()
    val hasChildren: Boolean
        get() = children.isNotEmpty()

    val resources: MutableList<MediaFileNode> = mutableListOf()
    val hasResources: Boolean
        get() = resources.isNotEmpty()

    val isDirectory: Boolean
        get() = hasResources and !hasChildren // 有资源但没有子节点

    fun printTree(depth: Int = 0) {
        print("    ".repeat(depth))
        println("MediaNode(name='$name', path='$path', type=$type, isDirectory=$isDirectory)")
        children.forEach {
            it.printTree(depth + 1)
        }
        resources.forEach {
            print("    ".repeat(depth + 1))
            println(it)
        }
    }

    companion object {
        inline operator fun invoke(block: MediaNode.() -> Unit): MediaNode {
            return MediaNode().apply(block)
        }
    }
}