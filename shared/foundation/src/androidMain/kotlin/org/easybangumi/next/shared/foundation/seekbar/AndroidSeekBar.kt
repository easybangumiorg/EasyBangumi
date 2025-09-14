package org.easybangumi.next.shared.foundation.seekbar

import android.content.res.ColorStateList
import android.widget.SeekBar
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView


/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
fun AndroidSeekBar(
    modifier: Modifier,
    during: Int,
    position: Int,
    secondary: Int,
    onValueChange: (Int) -> Unit,
    onValueChangeFinish: () -> Unit,
) {
    val colors = SliderDefaults.colors()
    AndroidView(
        modifier = modifier,
        factory = {
            SeekBar(it).apply {
                progress = position
                secondaryProgress = secondary
                max = during
                thumbTintList = ColorStateList.valueOf(colors.thumbColor.toArgb())
                progressTintList = ColorStateList.valueOf(colors.activeTrackColor.toArgb())
                progressBackgroundTintList = ColorStateList.valueOf(Color.White.copy(alpha = 0.6f).toArgb())
                secondaryProgressTintList = ColorStateList.valueOf(Color.White.copy(alpha = 0.8f).toArgb())
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            onValueChange(progress)
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        onValueChangeFinish()
                    }
                })
            }
        },
        update = {
            it.progress = position
            it.secondaryProgress = secondary
            it.max = during
        }
    )
}