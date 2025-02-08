package com.heyanle.easy_bangumi_cm.model.meida.local.model

import java.time.DateTimeException


/// 文件系统节点，记录来自文件系统的关于库的原始信息，不参与构建媒体库节点树，但是作为构建媒体库节点树的依据
data class FileSystemNode(
    val exist: Boolean,
    val name: String,
    val fullName: String,
    val path: String,
    val isDir: Boolean,
    val size: Long,
    val extension: String,
    val lastModified: Long,
    val creationTime: Long,
    val children: List<FileSystemNode>
)