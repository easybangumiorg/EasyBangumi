package com.heyanle.easy_bangumi_cm.model.meida.local.model

import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.properties.Delegates


/// 文件系统节点，记录来自文件系统的关于库的原始信息，不参与构建媒体库节点树，但是作为构建媒体库节点树的依据
data class FileSystemNode(
    val exist: Boolean,
    val name: String,
    val fullName: String,
    val path: Path,
    val isDir: Boolean,
    val size: Long,
    val extension: String,
    val lastModified: FileTime,
    val creationTime: FileTime,
    val children: List<FileSystemNode>
)

class BuildFileSystemNode {
    var exist: Boolean by Delegates.notNull()
    var name: String by Delegates.notNull()
    var fullName: String by Delegates.notNull()
    var path: Path by Delegates.notNull()
    var isDir: Boolean by Delegates.notNull()
    var size: Long by Delegates.notNull()
    var extension: String by Delegates.notNull()
    var lastModified: FileTime by Delegates.notNull()
    var creationTime: FileTime by Delegates.notNull()
    var children: List<FileSystemNode> by Delegates.notNull()

    fun build(): FileSystemNode {
        return FileSystemNode(
            exist = exist,
            name = name,
            fullName = fullName,
            path = path,
            isDir = isDir,
            size = size,
            extension = extension,
            lastModified = lastModified,
            creationTime = creationTime,
            children = children
        )
    }

    companion object {
        inline operator fun invoke(block: BuildFileSystemNode.() -> Unit) =
            BuildFileSystemNode().apply(block)
    }
}