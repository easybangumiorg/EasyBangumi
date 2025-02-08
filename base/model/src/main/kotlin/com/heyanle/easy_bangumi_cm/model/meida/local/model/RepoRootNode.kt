package com.heyanle.easy_bangumi_cm.model.meida.local.model

class RepoRootNode(
    val basePath: String,
    val name: String,
    block: (RepoRootNode.() -> Unit)? = null
) {

    val children: MutableList<MediaNode> = mutableListOf()
    val hasChildren: Boolean
        get() = children.isNotEmpty()

    init {
        block?.invoke(this)
    }

    fun printTree(depth: Int = 0) {
        print("    ".repeat(depth))
        println("RepoRootNode(name='$name', basePath='$basePath')")
        children.forEach {
            it.printTree(depth + 1)
        }
    }
}