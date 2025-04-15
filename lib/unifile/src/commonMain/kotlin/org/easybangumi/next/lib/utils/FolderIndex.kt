package org.easybangumi.next.lib.utils

import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.io.files.Path
import okio.buffer
import okio.use
import org.easybangumi.next.lib.serialization.JsonSerializer
import org.easybangumi.next.lib.serialization.jsonSerializer
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 *  快速检查一个文件夹里的文件有没有修改
 *  检查文件 size 和 lastModifier
 */

object FolderIndex {

    sealed class Item {

        companion object {
            fun fromLine(line: String?): Item? {
                if (line == null) {
                    return null
                }
                if (line.startsWith("f")){
                    val name = line.substring(1)
                    return jsonSerializer.deserialize(name, FolderItem::class, null)
                }
                if (line.startsWith("i")){
                    val name = line.substring(1)
                    return jsonSerializer.deserialize(name, IndexItem::class, null)
                }
                if (line == UP_LINE) {
                    return UpItem
                }
                val item = jsonSerializer.deserialize(line, FileItem::class, null)
                return item
            }
        }

        // 返回 parent
        object UpItem: Item()

        // 进入文件夹
        data class FolderItem (
            val name: String,
        ): Item()

        // 检查文件
        data class FileItem (
            val name: String,
            val lastModified: Long,
            val size: Long,
        ): Item()

        // 检查索引
        data class IndexItem (
            val name: String,
            val totalSize: Long,
            val tag: String?,
        ): Item()
    }


    const val UP_LINE = "..."
    const val INDEX_FILE_NAME = ".folder.index"

    suspend fun check(
        ufd: UFD,
        // 为空则不检查该字段
        tag: String? = null,
    ): Boolean = withContext(coroutineProvider.io())  {

        val folder = UniFileFactory.fromUFD(ufd) ?: return@withContext false
        if (!folder.exists() || !folder.isDirectory()) {
            return@withContext false
        }

        val indexItemFile = folder.child(INDEX_FILE_NAME)
        if(indexItemFile == null || !indexItemFile.exists()){
            return@withContext false
        }

        indexItemFile.openSource().buffer().use {
            val nameLine = it.readUtf8Line() ?: return@withContext false
            if (nameLine != folder.getName()) {
                return@withContext false
            }

            val tagLine = it.readUtf8Line()
            if (tag != null && tag != tagLine) {
                return@withContext false
            }

            var indexSize = 0L
            var totalSize = 0L

            var current: UniFile? = folder

            while (true) {
                val line = it.readUtf8Line() ?: break

                val item = Item.fromLine(line)
                when(item) {
                    is Item.FileItem -> {
                        if (current == null) {
                            return@withContext false
                        }
                        val child = current.child(item.name)
                        if (child == null) {
                            return@withContext false
                        }

                        if (!child.exists() && item.size > 0) {
                            return@withContext false
                        }

                        if (!child.isFile() && item.size > 0) {
                            return@withContext false
                        }

                        if (child.length() != item.size) {
                            return@withContext false
                        }

                        if (child.lastModified() != item.lastModified) {
                            return@withContext false
                        }

                        if (child.getName() != item.name) {
                            return@withContext false
                        }

                        totalSize += item.size
                    }
                    is Item.FolderItem -> {
                        current = current?.child(item.name)
                        continue
                    }
                    is Item.UpItem -> {
                        current = current?.getParentFile()
                        continue
                    }
                    is Item.IndexItem -> {
                        indexSize = item.totalSize
                        if (folder.getName() != item.name) {
                            return@withContext false
                        }
                        if (tag != null && tag != item.tag) {
                            return@withContext false
                        }

                    }
                    else -> {
                        continue
                    }
                }
            }
            if (totalSize != indexSize) {
                return@withContext false
            }
        }
        return@withContext true
    }

    suspend fun make(
        ufd: UFD,
        tag: String?,
    ): Boolean = withContext(coroutineProvider.io())  {


        val folder = UniFileFactory.fromUFD(ufd) ?: return@withContext false


        if (!folder.exists() || !folder.isDirectory()) {
            return@withContext false
        }


        val indexItemFile = folder.child(INDEX_FILE_NAME)
        if(indexItemFile == null){
            return@withContext false
        }

        indexItemFile.delete()

        indexItemFile.openSink(false).buffer().use {

            var totalSize = 0L

            val stack = arrayListOf<Pair<UniFile, Boolean>>()
            stack.add(folder to false)
            stack.add(folder to true)
            while (stack.isNotEmpty()) {
                val pop = stack.removeLastOrNull() ?: break
                if (!pop.second) {
                    it.writeUtf8(UP_LINE).writeUtf8("\n")
                } else {
                    if (pop.first.isFile()) {
                        val fileItem = Item.FileItem(
                            name = pop.first.getName(),
                            lastModified = pop.first.lastModified(),
                            size = pop.first.length()
                        )
                        totalSize += fileItem.size
                        it.writeUtf8(jsonSerializer.serialize(fileItem)).writeUtf8("\n")
                        continue
                    }
                    if (pop.first.isDirectory()) {
                        val folderItem = Item.FolderItem(
                            name = pop.first.getName(),
                        )
                        it.writeUtf8("f").writeUtf8(jsonSerializer.serialize(folderItem)).writeUtf8("\n")
                        for (file in pop.first.listFiles().filterNotNull().reversed()) {
                            if (!file.exists()) {
                                continue
                            }
                            if (file.isDirectory()) {
                                stack += (file to false)
                                stack += (file to true)
                            } else if (file.isFile()) {
                                stack += (file to true)
                            }
                        }
                    }


                }
            }

            val indexItem = Item.IndexItem(
                name = folder.getName(),
                totalSize = totalSize,
                tag = tag
            )
            it.writeUtf8("i").writeUtf8(jsonSerializer.serialize(indexItem)).writeUtf8("\n")
            it.flush()


        }
        return@withContext indexItemFile.exists() && indexItemFile.length() > 0
    }


}