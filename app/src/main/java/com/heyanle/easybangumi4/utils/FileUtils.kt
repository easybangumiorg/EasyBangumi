package com.heyanle.easybangumi4.utils

import java.io.File

/**
 * Created by heyanle on 2024/6/1.
 * https://github.com/heyanLE
 */
object FileUtils {

    fun traverseFolder(folder: File?, res: ArrayList<Pair<String, Long>>){
        traverseFolder(folder, arrayListOf(), res)

    }

    private fun traverseFolder(folder: File?, path: ArrayList<String>, res: ArrayList<Pair<String, Long>>){
        if (folder == null || !folder.exists()){
            return
        }
        if (folder.isFile){
            res.add(path.joinToString(File.separator) + File.separator + folder.name to folder.length())
            return
        }
        path.add(folder.name)
        folder.listFiles()?.forEach {
            traverseFolder(it, path, res)
        }
        path.removeLast()

    }

}