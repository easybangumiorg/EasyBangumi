package org.easybangumi.next.lib.utils

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock

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

interface DataRepository <T: Any> {

    companion object {
        const val DEFAULT_EXPIRED_MS = 1000L * 60 * 60 * 2 // 2 小时
    }

    val flow: StateFlow<DataState<T>>

    // == 刷新等级由强到弱 =======

    // 强制刷新
    fun refresh(): Boolean = false

    // 如果没数据 || 是缓存 || 数据过期
    fun refreshIfNoneOrExpired(availableMs: Long = DEFAULT_EXPIRED_MS): Boolean {
        if (refreshIfNoneOrCache(null)) {
            return true
        }
        val v = flow.value
        if (Clock.System.now().toEpochMilliseconds() - v.timestamp > availableMs) {
            return refresh()
        }
        return false
    }

    // 如果没数据 || 是缓存（可选过期时间）
    fun refreshIfNoneOrCache(cacheAvailableMs: Long? = null): Boolean {
        if (refreshIfNone()) {
            return true
        }
        val v = flow.value
        if (v is DataState.Ok && v.isCache) {
            if (cacheAvailableMs == null) {
                return  refresh()
            }
            if (Clock.System.now().toEpochMilliseconds() - v.timestamp > cacheAvailableMs) {
                return refresh()
            }
        }
        return false
    }

    // 如果没数据
    fun refreshIfNone(): Boolean {
        val v = flow.value
        if (!v.isOk()) {
            return refresh()
        }
        return false
    }

}