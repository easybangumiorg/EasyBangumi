package org.easybangumi.next.libplayer.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.easybangumi.next.libplayer.api.action.Action
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

/**
 * Created by heyanle on 2025/5/27.
 */
abstract class AbsPlayerBridge<T: Any> : PlayerBridge<T> {

    protected val innerPlayStateFlow = MutableStateFlow(C.State.IDLE)
    override val playStateFlow: StateFlow<C.State> = innerPlayStateFlow

    protected val innerPlayWhenReadyFlow = MutableStateFlow(false)
    override val playWhenReadyFlow: StateFlow<Boolean> = innerPlayWhenReadyFlow

    protected val innerVideoSizeFlow = MutableStateFlow(C.VIDEO_SIZE_UNSET)
    override val videoSizeFlow: StateFlow<VideoSize> = innerVideoSizeFlow


    protected val innerScaleTypeFlow = MutableStateFlow(C.RendererScaleType.SCALE_ADAPT)
    override val scaleTypeFlow: StateFlow<C.RendererScaleType> = innerScaleTypeFlow

    protected val innerErrorFlow = MutableStateFlow<Exception?>(null)
    override val errorStateFlow: StateFlow<Exception?> = innerErrorFlow



    @OptIn(ExperimentalAtomicApi::class)
    private val actionInit = AtomicBoolean(false)
    protected val actionMap = mutableMapOf<KClass<out Action<*>>, Action<*>>()

    @OptIn(ExperimentalAtomicApi::class)
    override fun <A : Action<*>> action(clazz: KClass<A>): A? {
        if (actionInit.compareAndSet(expectedValue = false, newValue = true)) {
            val map = prepareAction()
            actionMap.putAll(map)
        }
        @Suppress("UNCHECKED_CAST")
        return actionMap[clazz] as? A
    }

    abstract fun prepareAction(): Map<KClass<out Action<*>>, Action<*>>


}