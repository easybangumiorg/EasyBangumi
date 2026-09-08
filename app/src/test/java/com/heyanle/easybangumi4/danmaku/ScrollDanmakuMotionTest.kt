package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollDanmakuMotionTest {
    @Test
    fun textLengthChangesLifetimeButNotMovement() {
        val short = ScrollDanmakuMotion(0, 400f, 160f)
        val long = ScrollDanmakuMotion(0, 400f, 160f)
        assertEquals(short.leftAt(1000), long.leftAt(1000), 0f)
        assertEquals(240f, short.leftAt(1000), 0f)
        assertTrue(long.endTimeMillis(600f) > short.endTimeMillis(100f))
    }

    @Test
    fun speedChangesPreservePositionAndDistanceBetweenComments() {
        val first = ScrollDanmakuMotion(0, 400f, 160f)
        val second = ScrollDanmakuMotion(700, 400f, 160f)
        val before = first.leftAt(1000)
        val gap = second.leftAt(1000) - before
        first.changeSpeed(1000, 320f)
        second.changeSpeed(1000, 320f)
        assertEquals(before, first.leftAt(1000), 0f)
        assertEquals(gap, second.leftAt(1800) - first.leftAt(1800), 0.001f)
        first.changeSpeed(1800, 160f)
        second.changeSpeed(1800, 160f)
        assertEquals(gap, second.leftAt(2200) - first.leftAt(2200), 0.001f)
    }

    @Test
    fun futureCommentDoesNotMoveBeforeItsTimestampWhenSpeedChanges() {
        val motion = ScrollDanmakuMotion(5000, 400f, 160f)
        motion.changeSpeed(1000, 320f)
        assertEquals(400f, motion.leftAt(5000), 0f)
        assertEquals(80f, motion.leftAt(6000), 0f)
    }

    @Test
    fun admissionWaitsForRowAndDropsAfterOriginalDeadlineEvenIfRowBecomesFree() {
        assertEquals(ScrollAdmission.WAIT, scrollAdmission(1500, 1000, false))
        assertEquals(ScrollAdmission.ENTER, scrollAdmission(2000, 1000, true))
        assertEquals(ScrollAdmission.DROP, scrollAdmission(2001, 1000, true))
        assertEquals(ScrollAdmission.DROP, scrollAdmission(2001, 1000, false))
    }
}
