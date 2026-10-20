package com.heyanle.easybangumi4.ui.cartoon_play

import org.junit.Assert.*
import org.junit.Test

class HistoryResumeNoticeTest {
    @Test fun noticeAppearsOnlyOnceAfterReady() {
        val controller = HistoryResumeNoticeController()
        controller.prepare(75_000)
        assertNull(controller.notice.value)
        assertEquals(75_000L, controller.onReady()!!.positionMs)
        assertNull(controller.onReady())
        assertEquals("01:15", playerNoticeTime(controller.notice.value!!.positionMs))
    }
    @Test fun cancelIsOneShotAndUnavailableBeforeReady() {
        val controller = HistoryResumeNoticeController()
        controller.prepare(90_000)
        assertFalse(controller.cancelJump())
        controller.onReady()
        assertTrue(controller.cancelJump())
        assertFalse(controller.cancelJump())
        assertNull(controller.notice.value)
    }
    @Test fun staleTimeoutCannotDismissANewerNotice() {
        val controller = HistoryResumeNoticeController()
        controller.prepare(10_000)
        val old = controller.onReady()!!
        controller.prepare(20_000)
        val current = controller.onReady()!!
        controller.dismiss(old.id)
        assertEquals(current, controller.notice.value)
    }
    @Test fun switchingTargetOrLeavingClearsPendingResume() {
        val controller = HistoryResumeNoticeController()
        controller.prepare(20_000)
        controller.clear()
        assertNull(controller.onReady())
        controller.prepare(0)
        assertNull(controller.onReady())
    }
}
