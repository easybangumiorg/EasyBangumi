package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import com.heyanle.easy_bangumi_cm.base.data.DataState
import com.heyanle.easy_bangumi_cm.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

/**
 * 维护一个持久化数据，不支持 Error 态，需要指定 Default
 * Created by heyanlin on 2024/12/4.
 */
abstract class BaseFileHelper<T>(
    val folder: UniFile,
    val fileName: String,
    val def: T,
    val scope: CoroutineScope,
) {

    abstract fun load(inputStream: InputStream): T?

    abstract fun save(t: T, outputStream: OutputStream): Boolean

    private val _flow = MutableStateFlow<DataState<T>>(DataState.Loading())
    val flow = _flow.asStateFlow()
    val requestFlow = flow.filterIsInstance<DataState.Ok<T>>().map { it.data }

    private val tempFileName = "${fileName}.temp"

    init {
        scope.launch {
            val file = getFile() ?: run {
                fireDef()
                return@launch
            }
            try {
                val data = file.openInputStream().use {
                    load(it)
                }
                if (data == null) {
                    fireDef()
                } else {
                    fireData(data)
                }
            }catch (e: Exception){
                e.printStackTrace()
                fireDef()
            }

        }
    }

    fun trySave (){
        scope.launch {
            val data = flow.value.okOrNull() ?: return@launch
            var completely = false

            // 先尝试写入 temp 在 rename
            var tempFile = folder.findFile(tempFileName)
            tempFile?.delete()
            tempFile = folder.createFile(tempFileName)
            if (tempFile != null && tempFile.canWrite()) {
                if (save(data, tempFile.openOutputStream())) {
                    folder.findFile(fileName)?.delete()
                    completely = tempFile.renameTo(fileName)
                }
            }

            // 如果失败则直接写入
            if (!completely){
                val f = folder.findFile(fileName)
                f?.delete()
                val file = folder.createFile(fileName)
                if (file != null && file.canWrite()) {
                    if (!save(data, file.openOutputStream())) {
                        file.delete()
                    }
                }
            }
        }
    }

    fun set(data: T){
        // 这里异步写入，不保证写入成功
        fireData(data)
        trySave()
    }

    fun getOrDef(): T {
        return flow.value.okOrNull() ?: def
    }
    fun getOrNull(): T? {
        return flow.value.okOrNull()
    }

    fun update(
        block:(T) -> T
    ){
        val data = getOrDef()
        set( block(data))
    }

    private fun getFile(): UniFile? {
        val file = folder.findFile(fileName)
        if (file == null || !file.exists()) {
            val temp = folder.findFile(tempFileName)
            if (temp != null && temp.exists()) {
                temp.renameTo(fileName)
            }
            temp?.delete()
        }
        return folder.createFile(fileName)
    }

    private fun fireDef(){
        _flow.update {
            DataState.ok(def)
        }
    }

    private fun fireData(data: T){
        _flow.update {
            DataState.ok(data)
        }
    }
}