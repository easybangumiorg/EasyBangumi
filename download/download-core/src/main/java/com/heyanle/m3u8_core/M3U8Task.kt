package com.heyanle.m3u8_core

import kotlinx.coroutines.CoroutineScope

/**
 * Created by heyanlin on 2023/8/30.
 */

data class M3U8Task(
    val id: String,
    val scope: CoroutineScope,
    val url: String,
    val folder: String,
    val name: String,
    val isPausing: Boolean,
){

    sealed class ProcessState {
        data object Parsing: ProcessState()

        data class Downloading(
            val size: Long,
            val current: Long,
            val speed: Long,
        ): ProcessState()

        data class Merging(
            val size: Long,
            val current: Long,
        ): ProcessState()

        data class Converting(
            val size: Long,
            val current: Long,
        ): ProcessState()

    }
    sealed class M3U8State {
        data object Parsing: M3U8State()
        class Downloading(

        ): M3U8State()

        class Merging: M3U8State()
    }

    data class TsState(
        val url: String,
        val key: String,
        val method: String,
        val iv: String,
    )
}