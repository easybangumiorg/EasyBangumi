package com.heyanle.easy_bangumi_cm.common.plugin.core.utils

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.moshi.jsonTo
import com.heyanle.easy_bangumi_cm.base.utils.moshi.toJson
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Created by heyanlin on 2024/12/13.
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
        path: String,
        // 为空则不检查该字段
        tag: String? = null,
    ): Boolean = withContext(CoroutineProvider.io)  {
        val indexItemFile = File(path, INDEX_FILE_NAME)
        if(!indexItemFile.exists()){
            return@withContext false
        }

        var hasIndex = false

        var sizeFromIndex = 0L

        var size = 0L
        indexItemFile.inputStream().bufferedReader().use {
            val ls = it.lineSequence()

            for (line in ls){

                val item = line.jsonTo<FolderIndexItem>() ?: return@withContext false

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

                    val file = File(path, item.relative)
                    if (!file.exists()) {
                        return@withContext false
                    }
                    if (file.lastModified() != item.lastModified) {
                        return@withContext false
                    }
                    if (file.length() != item.size) {
                        return@withContext false
                    }
                    if (file.name != item.name) {
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
        path: String,
        tag: String?,
    ) = withContext(CoroutineProvider.io)  {
        val folderFile = File(path)

        if(!folderFile.exists() || !folderFile.isDirectory){
            return@withContext
        }


        val indexItemFile = File(path, INDEX_FILE_NAME)


        indexItemFile.delete()


        indexItemFile.createNewFile()

        var size = 0L

        val indexItems = mutableListOf<FolderIndexItem>()
        val pathDeque = ArrayDeque<Pair<String, File>>()

        pathDeque.add("" to folderFile)
        // bfs
        while (pathDeque.isNotEmpty()) {
            val cur = pathDeque.removeFirst()
            val curPath = cur.first
            val curFile = cur.second
            val array = curFile.listFiles() ?: continue
            for (file in array){
                if (file.isDirectory) {
                    pathDeque.add(curPath + "/" + file.name to file)
                } else if (file.isFile) {
                    size += file.length()
                    indexItems.add(
                        FolderIndexItem(
                            relative = curPath + "/" + file.name,
                            name = file.name,
                            lastModified = file.lastModified(),
                            size = file.length()
                        )
                    )
                }
            }
        }

        indexItemFile.outputStream().bufferedWriter().use {
            for (item in indexItems){
                it.write(item.toJson())
                it.newLine()
            }
            // index 文件本身存储的是整个文件夹的信息
            it.write(
                FolderIndexItem(
                    relative = "",
                    name = INDEX_FILE_NAME,
                    lastModified = System.currentTimeMillis(),
                    tag = tag,
                    size = size
                ).toJson()
            )
        }


    }

}