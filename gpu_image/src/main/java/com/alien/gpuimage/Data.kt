package com.alien.gpuimage

import android.opengl.GLES20

/// region Data 数据
data class Size(var width: Int = 0, var height: Int = 0) {

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


}


data class TextureAttributes(
    var minFilter: Int = GLES20.GL_LINEAR,
    var magFilter: Int = GLES20.GL_LINEAR,
    var wrapS: Int = GLES20.GL_CLAMP_TO_EDGE,
    var wrapT: Int = GLES20.GL_CLAMP_TO_EDGE,
    var internalFormat: Int = GLES20.GL_RGBA,
    var format: Int = GLES20.GL_RGBA,
    var type: Int = GLES20.GL_UNSIGNED_BYTE
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
