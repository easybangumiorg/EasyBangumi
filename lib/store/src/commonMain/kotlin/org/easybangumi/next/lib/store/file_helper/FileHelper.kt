package org.easybangumi.next.lib.store.file_helper

import kotlinx.coroutines.flow.Flow

interface FileHelper<T: Any> {

    fun set(t: T)

    fun get(): T

    fun def(): T

    fun flow(): Flow<T>

    fun update(block: (T) -> T){
        set(block(get()))
    }
}