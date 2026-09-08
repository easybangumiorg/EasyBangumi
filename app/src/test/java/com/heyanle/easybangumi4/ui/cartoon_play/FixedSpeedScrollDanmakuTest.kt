package com.heyanle.easybangumi4.ui.cartoon_play

import java.lang.reflect.Proxy
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.GlobalFlagValues
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuFactory
import master.flame.danmaku.danmaku.renderer.android.DanmakusRetainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Exercise the actual DFM item contract without an Android canvas or asynchronous draw loop. */
class FixedSpeedScrollDanmakuTest {
    private val timer = DanmakuTimer()
    private val flags = GlobalFlagValues()
    private val factory = DanmakuFactory.create()
    private val admission = ScrollDanmakuAdmission()
    private val retainer = DanmakusRetainer(false)
    private var nextMeasuredWidth: Float? = null
    private val display = Proxy.newProxyInstance(
        IDisplayer::class.java.classLoader,
        arrayOf(IDisplayer::class.java),
    ) { _, method, args ->
        when (method.name) {
            "getWidth" -> 400
            "getHeight" -> 24
            "getMargin", "getAllMarginTop" -> 0
            "measure" -> nextMeasuredWidth?.let { width ->
                (args?.get(0) as BaseDanmaku).paintWidth = width
                nextMeasuredWidth = null
            }
            else -> null
        }
    } as IDisplayer

    private fun item(at: Long, width: Float, preventOcclusion: Boolean = true) =
        FixedSpeedScrollDanmaku(factory, admission.takeIf { preventOcclusion }, 160f).apply {
            this.flags = this@FixedSpeedScrollDanmakuTest.flags
            setTimer(this@FixedSpeedScrollDanmakuTest.timer)
            setTime(at)
            paintWidth = width
            paintHeight = 24f
            measure(display, false)
        }

    private fun frame(now: Long, vararg items: FixedSpeedScrollDanmaku) {
        timer.update(now)
        items.forEach { retainer.fix(it, display, null) }
    }

    @Test
    fun nextItemWaitsUntilTailEntersThenStartsAtRightEdge() {
        val first = item(0, 160f)
        val second = item(500, 80f)
        frame(1, first)
        frame(600, first, second)
        assertFalse(second.isShown)
        frame(1001, first, second)
        assertTrue(second.isShown)
        assertEquals(400f, second.left, 0f)
        frame(1501, first, second)
        assertEquals(320f, second.left, 0f)
        assertTrue(second.left >= first.right)
    }

    @Test
    fun timedOutItemStaysDroppedUntilExplicitTimelineReset() {
        val first = item(0, 400f)
        val second = item(500, 80f)
        frame(1, first)
        frame(1501, first, second)
        assertFalse(second.isShown)
        frame(2600, first, second)
        assertFalse(second.isShown)
        first.requestTimelineReset()
        second.requestTimelineReset()
        retainer.clear()
        frame(501, second)
        assertTrue(second.isShown)
    }

    @Test
    fun disabledModeKeepsBothCommentsAndLongTextTravelsAtSameSpeed() {
        val short = item(0, 80f, false)
        val long = item(0, 640f, false)
        frame(1000, short, long)
        assertTrue(short.isShown)
        assertTrue(long.isShown)
        assertEquals(short.left, long.left, 0f)
        assertTrue(long.duration.value > short.duration.value)
        assertTrue(factory.MAX_DANMAKU_DURATION >= long.duration.value)
    }

    @Test
    fun changingSpeedKeepsCoordinatesAndExtendsNativeLifetimeWhenSlowingDown() {
        val comment = item(0, 400f, false)
        frame(1000, comment)
        val leftBefore = comment.left
        comment.updateSpeed(1000, 80f)
        comment.measure(display, true)
        frame(1000, comment)
        assertEquals(leftBefore, comment.left, 0f)
        frame(2000, comment)
        assertEquals(leftBefore - 80f, comment.left, 0f)
        assertFalse(comment.isTimeOut(7000))
        assertTrue(comment.isTimeOut(9000))
    }

    @Test
    fun styleRemeasureKeepsAnEnteredCommentAtItsCurrentPosition() {
        val comment = item(0, 200f, false)
        frame(1000, comment)
        assertEquals(240f, comment.left, 0f)

        flags.updateVisibleFlag()
        flags.updateMeasureFlag()
        nextMeasuredWidth = 500f
        comment.measure(display, true)
        frame(1000, comment)

        assertEquals(240f, comment.left, 0f)
        frame(1500, comment)
        assertEquals(160f, comment.left, 0f)
    }

    @Test
    fun defenseModeBypassesDfmRetainerWhileNormalModeKeepsIt() {
        assertEquals(BaseDanmaku.TYPE_SPECIAL, item(0, 80f).type)
        assertEquals(BaseDanmaku.TYPE_SCROLL_RL, item(0, 80f, false).type)
    }
}
