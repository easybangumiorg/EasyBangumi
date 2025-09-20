package org.easybangumi.next.libplayer.vlcj

import kotlinx.coroutines.CoroutineScope
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
class VlcjBridgeManager(
    // vlc 参数
    libvlcArgs: Collection<String> = emptyList(),
) {

    // factory 初始化直接会加载 native 库（耗时），lazy 里加了锁，线程安全
    private val mediaPlayerFactory: MediaPlayerFactory by lazy {
        MediaPlayerFactory(libvlcArgs)
    }
    // vlcj 播放器创建不支持并发
    private val reentrantLock = ReentrantLock()

    // 强引用防止回调被 GC，jna 会直接抛异常
    // 记得调用 release !
    private val map = ConcurrentHashMap<String, VlcjPlayerBridge>()


    // 耗时
    fun preloadVlc(){
        mediaPlayerFactory
    }

    fun getOrCreateBridge(
        tag: String,
        customFrameScope: CoroutineScope? = null,
    ): VlcjPlayerBridge {
        return map.getOrPut(tag) {
            reentrantLock.withLock {
                val player = mediaPlayerFactory.mediaPlayers().newMediaPlayer()
                VlcjPlayerBridge(player, customFrameScope)
            }
        }

    }

    fun release(tag: String) {
        map.remove(tag)?.close()
    }
}