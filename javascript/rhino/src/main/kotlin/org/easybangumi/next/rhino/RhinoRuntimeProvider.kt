package org.easybangumi.next.rhino

/**
 * Created by heyanle on 2024/7/30.
 * https://github.com/heyanLE
 */
class RhinoRuntimeProvider(
    private val maxRuntimeCount: Int = 1
) {

    private val runtimes: MutableList<RhinoRuntime> = mutableListOf()
    private var currentPoint = 0

    fun getRuntime(): RhinoRuntime {
        synchronized(runtimes){
            currentPoint ++
            currentPoint %= maxRuntimeCount
            var runtime = runtimes.getOrNull(currentPoint)
            if(runtime != null){
                return runtime
            }
            runtime = RhinoRuntime()
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