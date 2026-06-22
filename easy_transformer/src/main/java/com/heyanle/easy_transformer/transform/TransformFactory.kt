package com.heyanle.easy_transformer.transform

import androidx.media3.common.Format
import androidx.media3.transformer.Codec
import androidx.media3.transformer.SampleConsumer

/**
 * Created by heyanle on 2024/6/29.
 * https://github.com/heyanLE
 */
interface TransformFactory {

    fun createSampleConsumer(format: Format): SampleConsumer

}