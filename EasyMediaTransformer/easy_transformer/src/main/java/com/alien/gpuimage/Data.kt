package com.alien.gpuimage

import android.opengl.GLES20

/// region Data 数据
data class Size(var width: Int = 0, var height: Int = 0) {

    fun makeSizeWithAspectRation(bounding: Size): Size {
        val srcAspectRatio = (width.toFloat() / height.toFloat())
        val destAspectRatio = (bounding.width.toFloat() / bounding.height.toFloat())

        val resultWidth: Float
        val resultHeight: Float
        if (srcAspectRatio > destAspectRatio) {
            resultWidth = bounding.width.toFloat()
            val scale = width.toFloat() / resultWidth
            resultHeight = height.toFloat() / scale
        } else {
            resultHeight = bounding.height.toFloat()
            val scale = height.toFloat() / resultHeight
            resultWidth = width.toFloat() / scale
        }
        return Size(resultWidth.toInt(), resultHeight.toInt())
    }

    override fun equals(other: Any?): Boolean {
        if (other is Size) {
            return (width == other.width && height == other.height)
        }
        return false
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        return result
    }

    fun setSize(size: Size) {
        this.width = size.width
        this.height = size.height
    }
}

data class SizeF(var width: Float = 0f, var height: Float = 0f)

data class TextureAttributes(
    var minFilter: Int = GLES20.GL_LINEAR,
    var magFilter: Int = GLES20.GL_LINEAR,
    var wrapS: Int = GLES20.GL_CLAMP_TO_EDGE,
    var wrapT: Int = GLES20.GL_CLAMP_TO_EDGE,
    var internalFormat: Int = GLES20.GL_RGBA,
    var format: Int = GLES20.GL_RGBA,
    var type: Int = GLES20.GL_UNSIGNED_BYTE
)

data class BackgroundColor(
    var r: Float = 0f,
    var g: Float = 0f,
    var b: Float = 0f,
    var a: Float = 1.0f
)

enum class RotationMode {
    NoRotation,
    RotateLeft,
    RotateRight,
    FlipVertical,
    FlipHorizontal,
    RotateRightFlipVertical,
    RotateRightFlipHorizontal,
    Rotate180
}
/// endregion

/// region Callback
interface Callback {
    fun function()
}

interface CallbackParam<T> {
    fun function(t: T)
}
/// endregion

