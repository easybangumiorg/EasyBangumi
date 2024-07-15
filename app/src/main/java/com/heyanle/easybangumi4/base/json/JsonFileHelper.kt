package com.heyanle.easybangumi4.base.json

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.toJson
import com.hippo.unifile.UniFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2024/7/14.
 * https://github.com/heyanLE
 */
class JsonFileHelper<T : Any>(
    val folder: UniFile,
    val name: String,
    val def: T,
    val scope: CoroutineScope,
    val type: Type,
) {


    private val _flow = MutableStateFlow<DataResult<T>>(DataResult.Loading())
    val flow = _flow.asStateFlow()
    val requestFlow = flow.filterIsInstance<DataResult.Ok<T>>().map { it.data }

    private val tempFileName = "${name}.temp"

    init {
        scope.launch {
            val jsonFile = folder.createFile(name)
            if (jsonFile == null || !jsonFile.canRead()){
                _flow.update {
                    DataResult.error("json file create failed or can't read")
                }
                return@launch
            }
            val jsonString = jsonFile.openInputStream().use {
                it.bufferedReader().readText()
            }
            var data = jsonString.jsonTo<T>(type)
            if (data == null){
                val tempFile = folder.findFile(tempFileName)
                data = if (tempFile != null && tempFile.canRead()){
                    val tempString = tempFile.openInputStream().use {
                        it.bufferedReader().readText()
                    }
                    tempString.jsonTo<T>(type) ?: def
                } else {
                    def
                }
            }
            _flow.update {
                DataResult.ok(data)
            }
        }
    }

    fun trySave (){
        scope.launch {
            val data = flow.value.okOrNull() ?: return@launch
            val jsonString = data.toJson(type)
            var tempFile = folder.findFile(tempFileName)
            var needWriteTemp = true
            if (tempFile != null) {
                needWriteTemp = tempFile.delete()
            }
            if (needWriteTemp) {
                tempFile = folder.createFile(tempFileName)
                if (tempFile == null){
                    needWriteTemp = false
                } else {
                    tempFile.openOutputStream().bufferedWriter().use {
                        it.write(jsonString)
                    }
                }
            }
            folder.findFile(name)?.delete()
            if (!needWriteTemp || tempFile == null){
                val jsonFile = folder.createFile(name)
                if (jsonFile != null && jsonFile.canWrite()) {
                    jsonFile.openOutputStream().bufferedWriter().use {
                        it.write(jsonString)
                    }
                }
            } else {
                tempFile.renameTo(name)
            }
        }
    }

    fun set(data: T){
        trySave()
        _flow.update {
            DataResult.ok(data)
        }
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



}