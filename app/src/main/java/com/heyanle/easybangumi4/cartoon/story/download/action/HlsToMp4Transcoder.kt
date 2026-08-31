package com.heyanle.easybangumi4.cartoon.story.download.action

import android.app.Application

/** Flavor boundary for the download pipeline's local HLS/TS-to-MP4 remux step. */
interface HlsToMp4Transcoder {
    fun start(
        inputPath: String,
        outputPath: String,
        onProgress: (Float) -> Unit,
        onError: (Exception?) -> Unit,
        onCompleted: () -> Unit,
    )

    fun cancel()
}

fun createHlsToMp4Transcoder(application: Application): HlsToMp4Transcoder =
    createFlavorHlsToMp4Transcoder(application)
