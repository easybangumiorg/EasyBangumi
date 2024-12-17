package com.heyanle.easy_bangumi_cm.base.utils.file_helper

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Created by heyanlin on 2024/12/11.
 */
interface FileHelper<T: Any> {

    fun set(t: T)

    fun get(): T

    fun def(): T

    fun flow(): Flow<T>

    fun update(block: (T) -> T){
        set(block(get()))
    }
}

