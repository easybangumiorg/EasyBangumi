package com.alien.gpuimage.filter

import androidx.annotation.FloatRange

/**
 * 明亮度滤镜
 */
class BrightnessFilter : Filter(fragmentShader = FRAGMENT_SHADER) {

    companion object {
        private const val FRAGMENT_SHADER =
            """
             varying highp vec2 textureCoordinate;
             uniform sampler2D inputImageTexture;
             
             uniform lowp float brightness;
             
             void main()
             {
                 lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
                 gl_FragColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);
             }
            """
    }

    private var brightness = 0.0f
    private var brightnessUniform: Int = 0

    init {
        runSynchronouslyGpu(Runnable {
            brightnessUniform = filterProgram?.uniformIndex("brightness") ?: 0
            brightness = 0.0f
        })
    }

    /**
     * 亮度滑竿
     */
    fun setBrightness(@FloatRange(from = 0.0, to = 1.0) value: Float) {
        brightness = value
        setFloat(value, brightnessUniform, filterProgram)
    }
}