package com.heyanle.easy_bangumi_cm.model.meida.local.model

import com.heyanle.easy_bangumi_cm.model.meida.local.entitie.MediaNodeType

/// 媒体节点，用于表达媒体库中间节点
class MediaNode(
    val path: String,
    val name: String,
    val block: (MediaNode.() -> Unit)? = null
) {
    var type: MediaNodeType = MediaNodeType.UNKNOWN

    val children: MutableList<MediaNode> = mutableListOf()
    val hasChildren: Boolean
        get() = children.isNotEmpty()

    val resources: MutableList<MediaFileNode> = mutableListOf()
    val hasResources: Boolean
        get() = resources.isNotEmpty()

    val isDirectory: Boolean
        get() = hasResources and !hasChildren // 有资源但没有子节点

    init {
        block?.invoke(this)
    }

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
}