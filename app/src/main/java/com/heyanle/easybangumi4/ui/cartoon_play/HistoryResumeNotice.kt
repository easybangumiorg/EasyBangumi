package com.heyanle.easybangumi4.ui.cartoon_play

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HistoryResumeNotice(val id: Long, val positionMs: Long)

/** A one-shot notice for an explicit history resume, never for buffering or configuration restore. */
class HistoryResumeNoticeController {
    private var nextId = 0L
    private var pendingPositionMs: Long? = null
    private val mutableNotice = MutableStateFlow<HistoryResumeNotice?>(null)
    val notice = mutableNotice.asStateFlow()

    fun prepare(positionMs: Long) {
        clear()
        pendingPositionMs = positionMs.takeIf { it > 0L }
    }

    fun onReady(): HistoryResumeNotice? {
        val position = pendingPositionMs ?: return null
        pendingPositionMs = null
        return HistoryResumeNotice(++nextId, position).also { mutableNotice.value = it }
    }

    fun dismiss(id: Long) {
        if (mutableNotice.value?.id == id) mutableNotice.value = null
    }

    fun cancelJump(): Boolean {
        if (mutableNotice.value == null) return false
        clear()
        return true
    }

    fun clear() {
        pendingPositionMs = null
        mutableNotice.value = null
    }
}

internal fun playerNoticeTime(positionMs: Long): String {
    val seconds = positionMs.coerceAtLeast(0L) / 1_000
    return if (seconds >= 3_600) "%d:%02d:%02d".format(seconds / 3_600, seconds / 60 % 60, seconds % 60)
    else "%02d:%02d".format(seconds / 60, seconds % 60)
}
