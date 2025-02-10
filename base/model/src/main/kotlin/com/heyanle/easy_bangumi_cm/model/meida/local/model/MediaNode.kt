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

    var children: MutableList<MediaNode> by Delegates.notNull()
    val hasChildren: Boolean
        get() = children.isNotEmpty()

    var resources: MutableList<MediaFileNode> by Delegates.notNull()
    val hasResources: Boolean
        get() = resources.isNotEmpty()

    val isDirectory: Boolean
        get() = hasResources or hasChildren

    val isEmpty: Boolean
        get() = !hasResources and !hasChildren

    fun printTree(depth: Int = 0) {
        print("    ".repeat(depth))
        println("MediaNode(name='$name', path='$path', type=$type, isDirectory=$isDirectory)")
        resources.forEach {
            print("    ".repeat(depth + 1))
            println(it)
        }
        children.forEach {
            it.printTree(depth + 1)
        }
    }

    companion object {
        inline operator fun invoke(block: MediaNode.() -> Unit): MediaNode {
            return MediaNode().apply(block)
        }
    }
}