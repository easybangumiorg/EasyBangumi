package com.heyanle.easybangumi4.plugin.js.runtime

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
class JSRuntimeProvider(
    private val maxRuntimeCount: Int = 1
) {

    private val runtimes: MutableList<JSRuntime> = mutableListOf()
    private var currentPoint = 0

    fun getRuntime(): JSRuntime {
        synchronized(runtimes){
            currentPoint ++
            currentPoint %= maxRuntimeCount
            var runtime = runtimes.getOrNull(currentPoint)
            if(runtime != null){
                return runtime
            }
            runtime = JSRuntime()
            runtime.init()
            runtimes.add(runtime)
            return runtime
        }
    }



    fun release() {
        synchronized(runtimes){
            runtimes.forEach {
                it.release()
            }
            runtimes.clear()
        }
    }
}