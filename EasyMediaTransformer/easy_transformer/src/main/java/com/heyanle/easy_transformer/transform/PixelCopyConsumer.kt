package com.heyanle.easy_transformer.transform

import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import android.view.Surface
import androidx.media3.common.OnInputFrameProcessedListener
import androidx.media3.common.util.TimestampIterator
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.DecoderInputBuffer
import androidx.media3.transformer.SampleConsumer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.withLock
import kotlin.concurrent.write

/**
 * Created by heyanle on 2024/6/29.
 * https://github.com/heyanLE
 */
@UnstableApi
class PixelCopyConsumer(
    private val outputSurface: Surface,
    private val handler: Handler,
) : SampleConsumer {

    companion object {
        private const val TAG = "PixelCopyConsumer"
        const val MAX_PENDING_FRAME_COUNT = 5
    }

    @Volatile
    private var pendingFrameCount = 0
    @Volatile
    private var isEnd = false
    var listener : ConsumerListener? = null

    interface  ConsumerListener{
        fun onOutputRelease(presentationTimeUs: Long)
        fun onSignalEnd()
        fun onLastFrameRelease()
    }

    override fun getInputSurface(): Surface {
        return outputSurface
    }

    override fun getPendingVideoFrameCount(): Int {
        return pendingFrameCount

    }

    override fun registerVideoFrame(presentationTimeUs: Long): Boolean {
        if (isEnd) {
            return false
        }
        Log.i(TAG, "registerVideoFrame ${presentationTimeUs} $isEnd" )
        if (pendingFrameCount >= MAX_PENDING_FRAME_COUNT){
            return false
        }
        pendingFrameCount ++
        return true

    }

    fun releaseOutput(presentationTimeUs: Long) {
        pendingFrameCount --
        if (pendingFrameCount <= 0 && isEnd){
            handler.post {
                listener?.onLastFrameRelease()
            }
        }
        Log.i(TAG, "releaseOutput ${presentationTimeUs} $isEnd $pendingFrameCount" )
    }

    override fun signalEndOfVideoInput() {
        Log.i(TAG, "signalEndOfVideoInput" )
        isEnd = true
        listener?.onSignalEnd()
        if (pendingFrameCount <= 0){
            listener?.onLastFrameRelease()
        }
    }
}