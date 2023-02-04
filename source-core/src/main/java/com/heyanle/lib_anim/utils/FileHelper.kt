package com.heyanle.lib_anim.utils

import android.content.Context
import androidx.collection.LruCache
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

/**
 * Created by HeYanLe on 2023/2/3 22:55.
 * https://github.com/heyanLE
 */
lateinit var fileHelper: FileHelper
class FileHelper(
    context: Context
) {
    private val cacheDir = File(context.externalCacheDir?:context.cacheDir, "network_cache")

    private val lock = ReentrantReadWriteLock()
    private val cacheFileMap = hashMapOf<String, FileLru>()

    private class FileLru: LruCache<String, File>(10){
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: File, newValue: File?) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            kotlin.runCatching {
                oldValue.delete()
            }.onFailure {
                it.printStackTrace()
            }

        }
    }


    fun getFile(parserKey: String, fileName: String): File {
        lock.write {
            if(!cacheFileMap.containsKey(parserKey)){
                cacheFileMap[parserKey] = FileLru()
            }

            val fileLru = cacheFileMap[parserKey]?:throw IllegalAccessException()
            val file = fileLru.get(fileName)
            if(file == null){
                fileLru.put(fileName, File(File(cacheDir, parserKey), fileName))
            }
            return fileLru.get(fileName)?:throw IllegalAccessException()

        }
    }

}

fun File.getUri(): String{
    return "file://$absolutePath"
}