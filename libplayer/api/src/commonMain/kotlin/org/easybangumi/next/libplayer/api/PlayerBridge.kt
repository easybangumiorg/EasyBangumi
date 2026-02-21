package org.easybangumi.next.libplayer.api

import kotlinx.coroutines.flow.StateFlow
import org.easybangumi.next.libplayer.api.action.Action
import kotlin.reflect.KClass

/**
 * 播放器桥接接口，用于在 Common 层中与播放器进行交互
 * 各平台需要自己实现
 * Created by heyanle on 2025/5/27.
 */
interface PlayerBridge<T: Any>: AutoCloseable {

    // 尽量不要使用该变量，走 action
    val impl: T

    val playStateFlow: StateFlow<C.State>

    fun prepare(mediaItem: MediaItem)

    val playWhenReadyFlow: StateFlow<Boolean>
    fun setPlayWhenReady(playWhenReady: Boolean)

    val videoSizeFlow: StateFlow<VideoSize>

    // Playback control
    fun seekTo(positionMs: Long)

    // 不支持回调，业务自己缓存到 vm
    val positionMs: Long
    val bufferedPositionMs: Long
    val durationMs: Long

    // Renderer containers
    fun setScaleType(scaleType: C.RendererScaleType)
    val scaleTypeFlow: StateFlow<C.RendererScaleType>

    val errorStateFlow: StateFlow<Exception?>

    fun <A: Action<*>> action(clazz: KClass<A>): A?



}

inline fun <reified A: Action<P>, P: Any, R> PlayerBridge<P>.action(check: Boolean = true, block: A.()->R,): R? {
    val action = action(A::class)
    if (action == null) {
        if (check) {
            throw IllegalStateException("Action is null")
        }
        return null
    }
    return action.block()
}