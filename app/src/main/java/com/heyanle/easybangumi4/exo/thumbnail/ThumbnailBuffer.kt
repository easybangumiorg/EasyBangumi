package com.heyanle.easybangumi4.exo.thumbnail

import java.io.File
import java.net.URI
import java.util.TreeMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.math.absoluteValue

/**
 * Created by heyanle on 2024/6/16.
 * https://github.com/heyanLE
 */
class ThumbnailBuffer(
    private val folder: File,
) {

    private val reentrantReadWriteLock = ReentrantReadWriteLock()
    private val treeMap: TreeMap<Long, File> = TreeMap()
    var onTreeMapChange: ((TreeMap<Long, File>) -> Unit)? = null

    fun addThumbnail(position: Long, file: File) {
        reentrantReadWriteLock.write {
            treeMap[position] = file
        }
        onTreeMapChange?.invoke(treeMap.clone() as TreeMap<Long, File>)
    }

    fun removeThumbnail(position: Long) {
        reentrantReadWriteLock.write {
            treeMap.remove(position)
        }
        onTreeMapChange?.invoke(treeMap.clone() as TreeMap<Long, File>)
    }

    fun dispatchCurrent(){
        onTreeMapChange?.invoke(treeMap.clone() as TreeMap<Long, File>)
    }

    fun getThumbnail(position: Long, interval: Long): File? {
        reentrantReadWriteLock.read {
            val higherEntry = treeMap.higherEntry(position)
            val lowerEntry = treeMap.lowerEntry(position)
            val higherDiff = higherEntry?.key?.minus(position)?.absoluteValue ?: Long.MAX_VALUE
            val lowerDiffer = lowerEntry?.key?.minus(position)?.absoluteValue ?: Long.MAX_VALUE
            if (higherDiff < lowerDiffer && higherDiff < interval) {
                return higherEntry?.value
            }
            if (lowerDiffer < higherDiff && lowerDiffer < interval) {
                return lowerEntry?.value
            }
            return null
        }
    }

    fun clear() {
        treeMap.clear()
        onTreeMapChange?.invoke(treeMap.clone() as TreeMap<Long, File>)
    }

    fun release() {
        clear()
        folder.deleteRecursively()
    }

}

class TagFile: File {
    constructor(pathname: String) : super(pathname)
    constructor(parent: String?, child: String) : super(parent, child)
    constructor(parent: File?, child: String) : super(parent, child)
    constructor(uri: URI) : super(uri)

    var tag: Any? = null
}