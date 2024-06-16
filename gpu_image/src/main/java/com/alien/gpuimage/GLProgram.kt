package com.alien.gpuimage

import android.opengl.GLES20
import android.text.TextUtils
import com.alien.gpuimage.utils.Logger

class GLProgram() {

    companion object {
        private const val TAG = "GLProgram"
    }

    var program: Int = 0
        private set
    private var vertShader: Int = 0
    private var fragShader: Int = 0

    private var initialized: Boolean = false

    var programLog: String? = null
        private set
    var vertexShaderLog: String? = null
        private set
    var fragmentShaderLog: String? = null
        private set

    private val attributes: MutableList<String> = mutableListOf()

    var programReferenceCount: Int = 0

    constructor(vertexShader: String, fragmentShader: String) : this() {
        program = GLES20.glCreateProgram()

        vertShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader)
        if (vertShader <= 0) {
            Logger.e(TAG, "Failed to compile vertex shader.")
        }

        fragShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        if (fragShader <= 0) {
            Logger.e(TAG, "Failed to compile fragment shader.")
        }

        GLES20.glAttachShader(program, vertShader)
        GLES20.glAttachShader(program, fragShader)
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        if (TextUtils.isEmpty(shaderCode)) {
            Logger.e(TAG, "Failed to load shader.")
            return 0
        }

        val shaderId = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shaderId, shaderCode)
        GLES20.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            if (type == GLES20.GL_VERTEX_SHADER) {
                vertexShaderLog = log
            } else if (type == GLES20.GL_FRAGMENT_SHADER) {
                fragmentShaderLog = log
            }

            GLES20.glDeleteShader(shaderId)
            Logger.e(TAG, "Compilation of shader failed.")
            return 0
        }

        return shaderId
    }

    fun link(): Boolean {
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)

        if (linkStatus[0] == 0) {
            Logger.d(TAG, "Linking of program failed:${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            return false
        }

        if (vertShader > 0) {
            GLES20.glDeleteShader(vertShader)
            vertShader = 0
        }

        if (fragShader > 0) {
            GLES20.glDeleteShader(fragShader)
            fragShader = 0
        }

        initialized = true
        return true
    }

    fun use() {
        GLES20.glUseProgram(program)
    }

    fun release(force: Boolean): Boolean {
        if (!force && programReferenceCount-- > 0) {
            return false
        }

        if (vertShader > 0)
            GLES20.glDeleteShader(vertShader)

        if (fragShader > 0)
            GLES20.glDeleteShader(fragShader)

        if (program > 0)
            GLES20.glDeleteProgram(program)

        attributes.clear()

        return true
    }

    fun addAttribute(attributeName: String) {
        if (!attributes.contains(attributeName)) {
            attributes.add(attributeName)
            GLES20.glBindAttribLocation(program, attributes.indexOf(attributeName), attributeName)
        }
    }

    fun attributeIndex(attributeName: String): Int {
        check(GLContext.currentContextIsExist()) { "当前上下文不存在" }
        return attributes.indexOf(attributeName)
    }

    fun uniformIndex(uniformName: String): Int {
        check(GLContext.currentContextIsExist()) { "当前上下文不存在" }
        return GLES20.glGetUniformLocation(program, uniformName)
    }

    override fun toString(): String {
        return "program:$program attributes:${attributes} programReferenceCount:${programReferenceCount}"
    }
}