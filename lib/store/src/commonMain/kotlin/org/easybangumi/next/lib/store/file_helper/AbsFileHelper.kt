package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.buffer
import okio.use
import org.easybangumi.next.lib.logger.logger
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import kotlin.reflect.KClass

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
abstract class AbsFileHelper<T : Any>(
    private val folder: UFD,
    private val name: String,
    private val clazz: KClass<T>,
    private val def: T,
    private val scope: CoroutineScope
): FileHelper<T> {

    companion object {
        const val FINAL_MARK = "$\$final$$"
    }

    private var temp: T? = null


    private val folderFile: UniFile? by lazy {
        UniFileFactory.fromUFD(folder)
    }

    private val dataFileName = "$name.${suffix()}"
    private val bkFileName = "$name.bk.${suffix()}"

    private val dataFile: UniFile?
        get() = folderFile?.child(dataFileName)
    private val bkFile: UniFile?
        get() = folderFile?.child(bkFileName)

    // TODO 并发
    private val setListener = mutableListOf<(T) -> Unit>()

    override fun get(): T {
        val dataFile = dataFile
        return temp ?: dataFile?.openSource()?.use {
            val string = it.buffer().readByteString().utf8()
            if (string.endsWith(FINAL_MARK)) {
                bkFile?.delete()
                val data = deserializer(clazz, string) ?: return@get def
                temp = data
                data
            } else {
                dataFile.delete()
                bkFile?.delete()
                def
            }
        } ?: def
    }

    override fun set(t: T) {
        scope.launch {
            temp = t
            setListener.forEach {
                it(t)
            }
        }

        scope.launch {
            bkFile?.delete()
            val bk = bkFile
            bk ?: return@launch
            bk.openSink(false).buffer().use {
                it.writeUtf8(serializer(clazz, t))
                it.writeUtf8(FINAL_MARK)
                it.flush()
            }
            dataFile?.delete()
            bk.renameTo(dataFileName)
        }
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


    abstract fun suffix(): String
    abstract fun serializer(clazz: KClass<T>, data: T): String
    abstract fun deserializer(clazz: KClass<T>, source: String): T?



}