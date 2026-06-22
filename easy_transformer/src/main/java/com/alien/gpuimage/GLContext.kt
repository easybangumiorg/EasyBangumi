package com.alien.gpuimage

import android.opengl.GLES20
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.alien.gpuimage.egl.EglCore
import com.alien.gpuimage.egl.EglSurfaceBase
import com.alien.gpuimage.egl.OffscreenSurface
import com.alien.gpuimage.utils.Logger


class GLContext(createContext: Boolean = false) {

    companion object {
        private const val TAG = "GLContext"

        // 当前 GLContext
        private var currentContext: GLContext? = null

        fun contextIsExist(): Boolean {
            return currentContext != null
        }

        fun currentContextIsExist(): Boolean {
            return currentContext?.eglCore?.isCurrentContext ?: false
        }

        fun useProcessingContext() {
            sharedProcessingContext()?.useAsCurrentContext()
        }

        fun sharedProcessingContext(): GLContext? {
            return currentContext
        }

        @JvmStatic
        fun sharedFramebufferCache(): FramebufferCache? {
            return sharedProcessingContext()?.framebufferCache
        }

        fun program(vertexShader: String, fragmentShader: String): GLProgram? {
            return sharedProcessingContext()?.program(vertexShader, fragmentShader)
        }

        fun deleteProgram(program: GLProgram?) {
            sharedProcessingContext()?.deleteProgram(program)
        }

        fun setActiveShaderProgram(program: GLProgram?) {
            sharedProcessingContext()?.setContextShaderProgram(program)
        }

        fun withinTextureForSize(inputSize: Size): Size {
            val maxTextureSize =
                DataBuffer.intArray { GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, it, 0) }

            if ((inputSize.width < maxTextureSize) && (inputSize.height < maxTextureSize)) {
                return inputSize
            }

            val adjustedSize = Size()
            if (inputSize.width > inputSize.height) {
                adjustedSize.width = maxTextureSize
                adjustedSize.height =
                    ((maxTextureSize.toFloat() / inputSize.width.toFloat()) * inputSize.height).toInt()
            } else {
                adjustedSize.height = maxTextureSize
                adjustedSize.width =
                    ((maxTextureSize.toFloat() / inputSize.height.toFloat()) * inputSize.width).toInt()
            }

            return adjustedSize
        }

        fun print() {
            sharedProcessingContext()?.printUseMemory()
        }

        fun gc() {
            sharedProcessingContext()?.gc()
        }
    }

    var eglCore: EglCore? = null
        private set
    private var eglSurface: EglSurfaceBase? = null
    private var thread: HandlerThread? = null
    private var handler: Handler? = null

    private var framebufferCache: FramebufferCache? = null
    private var currentShaderProgram: GLProgram? = null
    private var shaderProgramCache: HashMap<String, GLProgram> = HashMap()

    init {
        if (createContext) {
            thread = HandlerThread("GL_Thread")
            thread?.start()
            handler = Handler(thread!!.looper)

            handler?.post {
                eglCore = EglCore(null, EglCore.FLAG_TRY_GLES3)
                eglSurface = OffscreenSurface(eglCore, 1, 1)
            }
            waitDone()

            currentContext = this
        }
        framebufferCache = FramebufferCache()
    }

    fun release() {
        runSynchronous(Runnable {
            framebufferCache?.release()
            shaderProgramCache.values.forEach { it.release(true) }
        })

        eglSurface?.releaseEglSurface()
        eglCore?.release()

        thread?.quitSafely()
        thread?.join()
        thread = null
    }

    fun isCurrentThread(): Boolean {
        return Thread.currentThread().name.equals(thread?.name)
    }

    fun getCurrentLoop(): Looper? {
        return thread?.looper
    }

    fun getCurrentHandler(): Handler? {
        return handler
    }

    private fun isCurrentContext(): Boolean {
        return eglCore?.isCurrentContext == true
    }

    private fun use() {
        eglSurface?.makeCurrent()
    }

    private fun useAsCurrentContext() {
        if (!isCurrentContext()) {
            Logger.d(TAG, "useAsCurrentContext")
            use()
        }
    }

    private fun program(vertexShader: String, fragmentShader: String): GLProgram? {
        val key = "V: $vertexShader - F: $fragmentShader"
        var value = shaderProgramCache[key]
        if (value == null) {
            value = GLProgram(vertexShader, fragmentShader)
            value.programReferenceCount++
            shaderProgramCache[key] = value
        } else {
            value.programReferenceCount++
        }
        return value
    }

    private fun deleteProgram(value: GLProgram?) {
        if (value?.release(false) == true) {
            shaderProgramCache.values.remove(value)
        }
    }

    private fun setContextShaderProgram(program: GLProgram?) {
        useAsCurrentContext()
        val currentProgram = DataBuffer.intArray {
            GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, it, 0)
        }
        if (currentShaderProgram != program || (currentShaderProgram?.program != currentProgram)) {
            currentShaderProgram = program
            currentShaderProgram?.use()
        }
    }

    /**
     * 同步操作
     */
    fun runSynchronous(runnable: Runnable) {
        runAsynchronously(runnable)
        waitDone()
    }

    private fun waitDone(): Boolean {
        val waitDoneLock = Object()
        val unlockRunnable = Runnable {
            synchronized(waitDoneLock) {
                waitDoneLock.notifyAll()
            }
        }
        synchronized(waitDoneLock) {
            this.handler?.post(unlockRunnable)
            try {
                waitDoneLock.wait()
            } catch (ex: InterruptedException) {
                return false
            }
        }
        return true
    }

    /**
     * 异步操作
     */
    fun runAsynchronously(runnable: Runnable) {
        this.handler?.post(runnable)
    }

    private fun printUseMemory() {
        runAsynchronously(Runnable {
            Logger.d(TAG, "Fbo-> \n ${framebufferCache.toString()}")

            val stringBuilder = StringBuilder()
            val iterator = shaderProgramCache.values.iterator()
            iterator.forEach {
                stringBuilder.append(it.toString()).append("\n")
            }
            Logger.d(TAG, "Program-> \n $stringBuilder")
        })
    }

    private fun gc() {
        runAsynchronously(Runnable {
            sharedProcessingContext()?.framebufferCache?.gc()
        })
    }
}