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

    data class FolderIndexItem(
        val relative: String,
        val name: String,
        val lastModified: Long,
        val size: Long,
        val tag: String? = null,
    )

    const val INDEX_FILE_NAME = ".folder_index.jsonl"

    suspend fun check(
        ufd: UFD,
        // 为空则不检查该字段
        tag: String? = null,
    ): Boolean = withContext(coroutineProvider.io())  {

        if (ufd.type != UFD.TYPE_OKIO && ufd.type != UFD.TYPE_JVM) {
            // android saf 可能有效率问题，先不支持
            return@withContext false
        }

        val folder = UniFileFactory.fromUFD(ufd) ?: return@withContext false


        if (!folder.exists() || !folder.isDirectory()) {
            return@withContext false
        }



        val indexItemFile = folder.child(INDEX_FILE_NAME)
        if(indexItemFile == null || !indexItemFile.exists()){
            return@withContext false
        }

        var hasIndex = false

        var sizeFromIndex = 0L

        var size = 0L

        val relativeTemp = hashMapOf<String, UniFile>()
        indexItemFile.openSource().buffer().use {
            while (true) {
                val line = it.readUtf8Line() ?: break


                val item = jsonSerializer.deserialize(line, FolderIndexItem::class, null) ?: return@withContext false

                if (item.name == INDEX_FILE_NAME && item.relative.isEmpty()) {
                    if (hasIndex) {
                        return@withContext false
                    }
                    // index 文件夹本身存储的是整个文件夹的信息
                    sizeFromIndex = item.size
                    if (tag != null && item.tag != tag) {
                        return@withContext false
                    }
                    hasIndex = true
                } else {

                    var file: UniFile? = folder.resolve(item.relative)
                    file ?: return@withContext false
                    if (!file.exists()) {
                        return@withContext false
                    }
                    if (file.lastModified() != item.lastModified) {
                        return@withContext false
                    }
                    if (file.length() != item.size) {
                        return@withContext false
                    }
                    if (file.getName() != item.name) {
                        return@withContext false
                    }
                    size += item.size
                }
            }
        }
        if (size != sizeFromIndex){
            return@withContext false
        }
        return@withContext true
    }

    suspend fun make(
        ufd: UFD,
        tag: String?,
    ): Boolean = withContext(coroutineProvider.io())  {

        if (ufd.type != UFD.TYPE_OKIO && ufd.type != UFD.TYPE_JVM) {
            // android saf 可能有效率问题，先不支持
            return@withContext false
        }

        val folder = UniFileFactory.fromUFD(ufd) ?: return@withContext false


        if (!folder.exists() || !folder.isDirectory()) {
            return@withContext false
        }


        val indexItemFile = folder.child(INDEX_FILE_NAME)
        if(indexItemFile == null){
            return@withContext false
        }

        indexItemFile.delete()

        var size = 0L

        val indexItems = mutableListOf<FolderIndexItem>()
        val pathDeque = ArrayDeque<Pair<String, UniFile>>()

        pathDeque.add("" to folder)
        // bfs
        while (pathDeque.isNotEmpty()) {
            val cur = pathDeque.removeFirst()
            val curPath = cur.first
            val curFile = cur.second
            val array = curFile.listFiles() ?: continue
            for (file in array){
                if (file?.isDirectory() == true) {
                    pathDeque.add(curPath + okio.Path.DIRECTORY_SEPARATOR + file.getName() to file)
                } else if (file?.isFile() == true) {
                    size += file.length()
                    indexItems.add(
                        FolderIndexItem(
                            relative = curPath + okio.Path.DIRECTORY_SEPARATOR + file.getName(),
                            name = file.getName(),
                            lastModified = file.lastModified(),
                            size = file.length()
                        )
                    )
                }
            }
        }

        indexItemFile.openSink(false).buffer().use {
            for (item in indexItems){
                it.writeUtf8(jsonSerializer.serialize(item))
                it.writeUtf8("\n")
            }
            // index 文件本身存储的是整个文件夹的信息
            it.writeUtf8(
                jsonSerializer.serialize(
                    FolderIndexItem(
                        relative = "",
                        name = INDEX_FILE_NAME,
                        lastModified = Clock.System.now().toEpochMilliseconds(),
                        tag = tag,
                        size = size
                    )
                )

            )
        }

        return@withContext true
    }

    fun t(
        root: UniFile,
        stringBuilder: StringBuilder
    ) {

        if (root.isFile()) {
            stringBuilder.append("file:").append(root.getName())
            stringBuilder.append("\n")
            return
        }

        if (!root.isDirectory()) {
            return
        }

        stringBuilder.append("folder:").append(root.getName())
        stringBuilder.append("\n")

        val array = root.listFiles()
        array.filterNotNull().forEach {
            t(it, stringBuilder)

        }

        stringBuilder.append("...")
        stringBuilder.append("\n")

        // 将以上算法改成迭代实现



    }

    fun tt(
        root: UniFile,
        stringBuilder: StringBuilder
    ) {

        val stack = arrayListOf<Pair<UniFile, Boolean>>()

        stack.add(root to false)
        stack.add(root to true)

        while (stack.isNotEmpty()) {
            val pop = stack.removeLastOrNull() ?: break
            if (!pop.second) {
                stringBuilder.append("...").append("\n")
            } else {
                if (pop.first.isFile()) {
                    stringBuilder.append("file:").append(pop.first.getName()).append("\n")
                    continue
                }
                if (pop.first.isDirectory()) {
                    stringBuilder.append("folder:").append(pop.first.getName()).append("\n")
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

    }

}