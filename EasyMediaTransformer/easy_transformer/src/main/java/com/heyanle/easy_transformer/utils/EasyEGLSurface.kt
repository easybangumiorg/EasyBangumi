package com.heyanle.easy_transformer.utils

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.Surface
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.GlUtil.GlException
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

/**
 * Created by heyanlin on 2024/6/26.
 */
@UnstableApi
class EasyEGLSurface(
    private val thread: EasyEGLSurfaceThread,
    surfaceTexture: SurfaceTexture?
) : Surface(surfaceTexture) {

    companion object {
        private const val TAG = "EGLSurface"


        fun newInstance(
            listener: EasyEGLSurfaceTexture.TextureImageListener
        ): EasyEGLSurface {
            val thread = EasyEGLSurfaceThread()
            return thread.init(listener)
        }
    }


    private var threadReleased = false

    fun getTextureId(): Int {
        return thread.getTextureId()
    }

    override fun release() {
        super.release()
        synchronized(thread.lock){
            if (!threadReleased){
                thread.release()
                threadReleased = true
            }
        }
    }

    class EasyEGLSurfaceThread : HandlerThread("EGLSurfaceThread"), Handler.Callback {

        companion object {
            private const val TAG = "EGLSurfaceThread"
            const val MSG_INIT = 1
            const val MSG_RELEASE = 2
        }

        val lock = Object()
        private lateinit var handler: Handler

        @Volatile
        private lateinit var easyEglSurfaceTexture: EasyEGLSurfaceTexture

        private var surface: EasyEGLSurface? = null
        private var initException: RuntimeException? = null
        private var initError: Error? = null

        fun getTextureId(): Int {
            if(::easyEglSurfaceTexture.isInitialized)
                return easyEglSurfaceTexture.textureId
            return -1
        }

        fun init(
            textureImageListener: EasyEGLSurfaceTexture.TextureImageListener): EasyEGLSurface {
            start()
            handler = Handler(looper, this)
            easyEglSurfaceTexture =
                EasyEGLSurfaceTexture(
                    handler,
                    textureImageListener
                )
            var wasInterrupted = false
            synchronized(lock) {
                handler.obtainMessage(
                    MSG_INIT,
                ).sendToTarget()
                while (surface == null && initException == null && initError == null) {
                    try {
                        lock.wait()
                    } catch (e: InterruptedException) {
                        wasInterrupted = true
                    }
                }
            }
            if (wasInterrupted) {
                // Restore the interrupted status.
                currentThread().interrupt()
            }
            val exception = initException
            val error = initError
            if (exception != null) {
                throw exception
            } else if (error != null) {
                throw error
            } else {
                return Assertions.checkNotNull(surface)
            }
        }


        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                MSG_INIT -> {
                    try {
                        initInternal()
                    } catch (e: java.lang.RuntimeException) {
                        Log.e(TAG, "Failed to initialize placeholder surface", e)
                        initException = e
                    } catch (e: GlException) {
                        Log.e(TAG, "Failed to initialize placeholder surface", e)
                        initException = IllegalStateException(e)
                    } catch (e: java.lang.Error) {
                        Log.e(TAG, "Failed to initialize placeholder surface", e)
                        initError = e
                    } finally {
                        synchronized(lock) {
                            lock.notify()
                        }
                    }
                    return true
                }

                MSG_RELEASE -> {
                    try {
                        releaseInternal()
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed to release placeholder surface", e)
                    } finally {
                        quit()
                    }
                    return true
                }

                else -> return true
            }
        }

        fun release() {
            Assertions.checkNotNull(handler)
            handler.sendEmptyMessage(MSG_RELEASE)
        }

        @Throws(GlException::class)
        private fun initInternal() {
            Assertions.checkNotNull<EasyEGLSurfaceTexture>(easyEglSurfaceTexture)
            easyEglSurfaceTexture.init()
            this.surface = EasyEGLSurface(this, easyEglSurfaceTexture.surfaceTexture)
        }

        private fun releaseInternal() {
            Assertions.checkNotNull<EasyEGLSurfaceTexture>(easyEglSurfaceTexture)
            easyEglSurfaceTexture.release()
        }
    }

}