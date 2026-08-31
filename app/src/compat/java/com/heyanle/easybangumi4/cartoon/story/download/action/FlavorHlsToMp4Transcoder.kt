package com.heyanle.easybangumi4.cartoon.story.download.action

import android.app.Application
import com.jeffmony.m3u8library.VideoProcessManager
import com.jeffmony.m3u8library.listener.IVideoTransformListener

/** Compact FFmpeg remuxer retained from 6.0.1 for the normal APK. */
private class CompactFfmpegHlsToMp4Transcoder : HlsToMp4Transcoder {
    override fun start(
        inputPath: String,
        outputPath: String,
        onProgress: (Float) -> Unit,
        onError: (Exception?) -> Unit,
        onCompleted: () -> Unit,
    ) {
        VideoProcessManager.getInstance().transformM3U8ToMp4(
            inputPath,
            outputPath,
            object : IVideoTransformListener {
                override fun onTransformProgress(progress: Float) = onProgress(progress)

                override fun onTransformFailed(exception: Exception?) = onError(exception)

                override fun onTransformFinished() = onCompleted()
            },
        )
    }

    // JeffM3U8Lib 1.2.0 does not expose a cancellation primitive.
    override fun cancel() = Unit
}

internal fun createFlavorHlsToMp4Transcoder(
    application: Application,
): HlsToMp4Transcoder = CompactFfmpegHlsToMp4Transcoder()
