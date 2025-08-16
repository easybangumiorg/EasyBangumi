package com.heyanle.easybangumi4.base.json

import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.utils.jsonTo
import com.heyanle.easybangumi4.utils.logi
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
 */
class JsonlFileHelper <T : Any>(
    val folder: UniFile,
    val name: String,
    val scope: CoroutineScope,
    val type: Type,
) {

    companion object {
        private const val TAG = "JsonlFileHelper"
    }


    private val _flow = MutableStateFlow<DataResult<List<T>>>(DataResult.Loading())
    val flow = _flow.asStateFlow()
    val requestFlow = flow.filterIsInstance<DataResult.Ok<List<T>>>().map { it.data }

    private val tempFileName = "${name}.temp"

    val initJob = scope.launch {
        val jsonFile = folder.createFile(name)
        if (jsonFile == null || !jsonFile.canRead()){
            _flow.update {
                DataResult.error("json file create failed or can't read")
            }
            return@launch
        }
        var data = jsonFile.openInputStream().use {
            it.bufferedReader().lineSequence().map {
                it.jsonTo<T>(type)
            }.filterNotNull().toList()
        }
        _flow.update {
            DataResult.ok(data)
        }
    }


    fun trySave (){
        scope.launch {
            val data = flow.value.okOrNull() ?: return@launch
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
                        data.forEach { t ->
                            val line = t.toJson(type)
                            if (line != null) {
                                it.write(line)
                                it.newLine()
                            }
                        }
                    }
                }
            }
            folder.findFile(name)?.delete()
            if (!needWriteTemp || tempFile == null){
                val jsonFile = folder.createFile(name)
                if (jsonFile != null && jsonFile.canWrite()) {
                    jsonFile.openOutputStream().bufferedWriter().use {
                        data.forEach { t ->
                            val line = t.toJson(type)
                            if (line != null) {
                                it.write(line)
                                it.newLine()
                            }
                        }
                    }
                }
            } else {
                tempFile.renameTo(name)
            }
        }
    }

    fun set(data: List<T>){
        _flow.update {
            DataResult.ok(data)
        }
        trySave()
    }

    fun getOrDef(): List<T> {
        return flow.value.okOrNull() ?: emptyList()
    }
    fun getOrNull(): List<T>? {
        return flow.value.okOrNull()
    }

    fun update(
        block:(List<T>) -> List<T>
    ){
        val data = getOrDef()
        set( block(data).apply {
            "$data -> $this".logi(TAG)
        })
    }



}