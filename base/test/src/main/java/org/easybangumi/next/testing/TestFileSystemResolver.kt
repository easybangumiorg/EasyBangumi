package org.easybangumi.next.testing

import com.heyanle.easy_bangumi_cm.model.meida.local.model.BuildFileSystemNode
import com.heyanle.easy_bangumi_cm.model.meida.local.model.FileSystemNode
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.getAttribute


object TestFileSystemResolver {

    fun resolve(nodePath: String): FileSystemNode = BuildFileSystemNode {
        path = Path(nodePath)
        exist = Files.exists(path)
        name = path.fileName.toString()
        fullName = nodePath
        isDir = path.getAttribute("isDirectory") as Boolean
        size = path.getAttribute("size") as Long
        extension = path.fileName.extension
        lastModified = path.getAttribute("lastModifiedTime") as FileTime
        creationTime = path.getAttribute("creationTime") as FileTime
        children = if (isDir)
            Files.list(path).map { resolve(it.toString()) }.toList()
        else
            emptyList()
    }.build()

}