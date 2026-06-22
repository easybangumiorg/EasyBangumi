package com.alien.gpuimage.filter

/**
 * 啥也不干，只提供回调，这里回调执行在 gl 线程，会在所有 Filter 处理完毕后回调
 * Created by heyanle on 2024/6/29.
 * https://github.com/heyanLE
 */
class HookFilter : Filter() {

    interface OnFrameCompletelyListener {

        fun onFrameStart(time: Long, textureIndex: Int)
        fun onFrameCompletely(time: Long, textureIndex: Int)
    }

    var onFrameCompletely: OnFrameCompletelyListener? = null
    override fun newFrameReadyAtTime(time: Long, textureIndex: Int) {
        onFrameCompletely?.onFrameStart(time, textureIndex)
        super.newFrameReadyAtTime(time, textureIndex)
        onFrameCompletely?.onFrameCompletely(time, textureIndex)
    }
}