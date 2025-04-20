package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.flow.Flow

interface FileHelper<T: Any> {

    suspend fun get(): T

    fun set(t: T)

    fun getSync(): T

    fun def(): T

    fun flow(): Flow<T>

    fun updateSync(block: (T) -> T){
        set(block(getSync()))
    }

    suspend fun update(block: (T) -> T){
        set(block(get()))
    }
}