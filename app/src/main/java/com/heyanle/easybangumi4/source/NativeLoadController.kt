package com.heyanle.easybangumi4.source

import com.heyanle.easybangumi4.utils.logi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

/**
 * Created by heyanle on 2024/6/1.
 * https://github.com/heyanLE
 */
class NativeLoadController {


    companion object {
        val TAG = "NativeLoadController"
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val loadedLibName = linkedMapOf<String, String>()

    suspend fun load(path: String): Int {
        return withContext(dispatcher){
            val file = File(path)
            if (!file.exists()){
                return@withContext -1
            }
            val name = file.name
            val oldPath = loadedLibName[name]
            if (oldPath == null){
                return@withContext try {
                    System.load(file.absolutePath)
                    loadedLibName[name] = file.absolutePath
                    "so load sus ${file.absolutePath}".logi(TAG)
                    0
                } catch (e: Throwable){
                    -1
                }
            }else if (oldPath != file.absolutePath) {
                // 动态库连接冲突，需重启了
                return@withContext -2
            }else{
                // 已经连接了
                return@withContext 0
            }
        }
    }

}