package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

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
class AbsFileHelper<T : Any>(
    val folder: Path,
    val fileName: String,
    private val def: T,
    val scope: CoroutineScope,
): FileHelper<T> {

    private val setListener = CopyOnWriteArrayList<(T) -> Unit>()

    private var temp: T? = null
    private val tempFileName = "${fileName}.temp"


    override fun set(t: T) {
        innerSet(t)
    }

    override fun get(): T {
        return temp ?: folder.findFile(fileName)?.openInputStream()?.use {
            temp = load(it)
            temp
        } ?: def
    }

    override fun def(): T {
        return def
    }

    override fun flow(): Flow<T> {
        return callbackFlow<T> {
            val listener: (T) -> Unit = { t: T ->
                trySend(t)
            }
            setListener.add(listener)
            awaitClose {
                setListener.remove(listener)
            }
        }.onStart {
            emit(get())
        }.distinctUntilChanged()
    }

    private fun innerSet(t: T){
        scope.launch {
            temp = t
            setListener.forEach {
                it(t)
            }

            scope.launch {
                try {
                    var completely = false
                    folder.findFile(tempFileName)?.delete()
                    val tempFile = folder.createFile(tempFileName)
                    tempFile?.openOutputStream()?.use {
                        save(t, it)
                        completely = true
                    }
                    if (completely) {
                        folder.findFile(fileName)?.delete()
                        tempFile?.renameTo(fileName)
                    } else {
                        tempFile?.delete()
                        folder.findFile(fileName)?.delete()
                        folder.createFile(fileName)?.openOutputStream()?.use {
                            save(t, it)
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

    }

    abstract fun load(inputStream: InputStream): T?

    abstract fun save(t: T, outputStream: OutputStream): Boolean
}