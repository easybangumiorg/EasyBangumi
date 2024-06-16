package com.alien.gpuimage.filter

import android.opengl.GLES20
import java.nio.FloatBuffer

/**
 * Mask 滤镜可以将 一张黑白图片 和一张原图进行融合。
 * 原图会将黑的部分进行透明度0处理
 */
class MaskBlendFilter : TwoInputFilter(fragmentShader = SHADER_STRING) {
    companion object {
        private const val SHADER_STRING =
            """
            varying highp vec2 textureCoordinate;
            varying highp vec2 textureCoordinate2;
            
            uniform sampler2D inputImageTexture;
            uniform sampler2D inputImageTexture2;
            
            void main()
            {
                lowp vec4 base = texture2D(inputImageTexture, textureCoordinate);
                lowp vec4 overlay = texture2D(inputImageTexture2, textureCoordinate2);
                
                gl_FragColor = vec4(base.r, base.g, base.b, overlay.r);
            }
            """
    }

    override fun renderToTexture(vertices: FloatBuffer, textureCoordinates: FloatBuffer) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        super.renderToTexture(vertices, textureCoordinates)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}