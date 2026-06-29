package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.flow.StateFlow

interface FileHelper<T: Any> {

    suspend fun get(): T

    fun push(t: T)

    suspend fun setAndWait(t: T)

    fun getSync(): T

    fun def(): T

    fun flow(): StateFlow<T>

    fun updateSync(block: (T) -> T){
        push(block(getSync()))
    }

    suspend fun update(block: (T) -> T){
        push(block(get()))
    }
}