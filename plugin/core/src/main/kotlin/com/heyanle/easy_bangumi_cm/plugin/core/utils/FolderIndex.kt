package com.heyanle.easy_bangumi_cm.plugin.core.utils

import com.heyanle.easy_bangumi_cm.base.utils.CoroutineProvider
import com.heyanle.easy_bangumi_cm.base.utils.jsonTo
import com.heyanle.easy_bangumi_cm.base.utils.toJson
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
    )

    data class FolderIndex(
        val path: String,
        val size: Long,
        val modifiedTime: Long, // 业务自己维护
    )

    const val INDEX_FILE_NAME = ".index.json"
    const val INDEX_ITEM_FILE_NAME = ".index_items.jsonl"

    suspend fun check(
        path: String,
        // 为空则不检查该字段
        modifiedTime: Long? = null,
    ): Boolean = withContext(CoroutineProvider.io)  {
        val indexFile = File(path, INDEX_FILE_NAME)
        if(!indexFile.exists()){
            return@withContext false
        }
        val index = indexFile.readText()
        val folderIndex = index.jsonTo<FolderIndex>() ?: return@withContext false
        if (modifiedTime != null && folderIndex.modifiedTime != modifiedTime){
            return@withContext false
        }

        if (folderIndex.size == 0L) {
            return@withContext true
        }

        val indexItemFile = File(path, INDEX_ITEM_FILE_NAME)
        if(!indexItemFile.exists()){
            return@withContext false
        }

        var size = 0L
        indexItemFile.inputStream().bufferedReader().use {
            val ls = it.lineSequence()
            for (line in ls){
                val item = line.jsonTo<FolderIndexItem>() ?: return@withContext false
                val file = File(path, item.relative)
                if(!file.exists()){
                    return@withContext false
                }
                if(file.lastModified() != item.lastModified){
                    return@withContext false
                }
                if (file.length() != item.size){
                    return@withContext false
                }
                if (file.name != item.name){
                    return@withContext false
                }
                size += item.size
            }
        }
        if (size != folderIndex.size){
            return@withContext false
        }
        return@withContext true
    }

    suspend fun make(
        path: String,
        modifiedTime: Long,
    ) = withContext(CoroutineProvider.io)  {
        val folderFile = File(path)

        if(!folderFile.exists() || !folderFile.isDirectory){
            return@withContext
        }

        val indexFile = File(path, INDEX_FILE_NAME)
        val indexItemFile = File(path, INDEX_ITEM_FILE_NAME)

        indexFile.delete()
        indexItemFile.delete()

        indexFile.createNewFile()
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

        indexFile.writeText(
            FolderIndex(
                path = path,
                size = size,
                modifiedTime = modifiedTime
            ).toString()
        )

        indexItemFile.outputStream().bufferedWriter().use {
            for (item in indexItems){
                it.write(item.toJson())
                it.newLine()
            }
        }


    }

}