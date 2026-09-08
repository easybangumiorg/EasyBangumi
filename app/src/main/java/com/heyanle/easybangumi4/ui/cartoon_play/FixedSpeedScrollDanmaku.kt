package com.heyanle.easybangumi4.ui.cartoon_play

import com.heyanle.easybangumi4.danmaku.ScrollAdmission
import com.heyanle.easybangumi4.danmaku.ScrollDanmakuMotion
import com.heyanle.easybangumi4.danmaku.SCROLL_DANMAKU_MAX_WAIT_MILLIS
import com.heyanle.easybangumi4.danmaku.scrollAdmission
import master.flame.danmaku.danmaku.model.Duration
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.R2LDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuFactory

/**
 * DFM still owns filters, measurement, drawing and the timeline. Only scrolling motion and the
 * final admission step are replaced. Each item needs its own duration: long text exits later.
 * The monitor serializes DFM's drawing/cache threads with live speed changes from the UI thread.
 */
internal class FixedSpeedScrollDanmaku(
    private val factory: DanmakuFactory,
    private val admission: ScrollDanmakuAdmission?,
    private var pixelsPerMediaSecond: Float,
) : R2LDanmaku(Duration(DanmakuFactory.COMMON_DANMAKU_DURATION)) {
    private var motion: ScrollDanmakuMotion? = null
    private var viewportWidth = 0
    private var scheduledTimeMillis = Long.MIN_VALUE
    private var entered = false
    private var dropped = false
    private var timelineResetRequested = false

    /**
     * In defense mode the item bypasses DFM's overwrite-on-overflow scrolling retainer and uses
     * [ScrollDanmakuAdmission] as the final row allocator. Normal mode keeps DFM's standard rows.
     */
    override fun getType(): Int =
        if (admission == null) TYPE_SCROLL_RL else TYPE_SPECIAL

    @Synchronized
    override fun measure(displayer: IDisplayer, fromWorkerThread: Boolean) {
        val oldWidth = paintWidth
        val oldHeight = paintHeight
        val oldViewportWidth = viewportWidth
        val now = timer?.currMillisecond ?: actualTime
        val oldLeft = motion?.leftAt(now)
        val wasEntered = entered
        val shouldResetTimeline = timelineResetRequested ||
            (motion != null && scheduledTimeMillis != actualTime)
        super.measure(displayer, fromWorkerThread)
        viewportWidth = displayer.width
        if (shouldResetTimeline || motion == null) {
            resetMotion()
        } else if (oldWidth != paintWidth || oldHeight != paintHeight ||
            oldViewportWidth != viewportWidth) {
            if (wasEntered && oldLeft != null) {
                // Font, density and viewport updates keep an active item at its current X.
                motion = ScrollDanmakuMotion(now, oldLeft, pixelsPerMediaSecond)
                entered = true
                updateDuration()
            } else {
                resetMotion()
            }
        }
    }

    private fun resetMotion() {
        scheduledTimeMillis = actualTime
        motion = ScrollDanmakuMotion(actualTime, viewportWidth.toFloat(), pixelsPerMediaSecond)
        entered = false
        dropped = false
        timelineResetRequested = false
        setVisibility(false)
        updateDuration()
    }

    private fun ensureTimeline() {
        if (motion != null && (timelineResetRequested || scheduledTimeMillis != actualTime)) {
            resetMotion()
        }
    }

    private fun updateDuration() {
        val currentMotion = motion ?: return
        val waitAllowance = if (admission != null && !entered) SCROLL_DANMAKU_MAX_WAIT_MILLIS else 0L
        duration.setValue((currentMotion.endTimeMillis(paintWidth) - actualTime + waitAllowance).coerceAtLeast(1L))
        // DFM's time-window query must retain long/slow comments until their actual exit.
        synchronized(factory) {
            factory.MAX_DANMAKU_DURATION = maxOf(factory.MAX_DANMAKU_DURATION, duration.value)
        }
    }

    @Synchronized
    fun updateSpeed(timeMillis: Long, speed: Float) {
        ensureTimeline()
        pixelsPerMediaSecond = speed
        motion?.changeSpeed(timeMillis, speed)
        updateDuration()
    }

    @Synchronized
    fun requestTimelineReset() {
        timelineResetRequested = true
        setVisibility(false)
    }

    @Synchronized
    override fun isTimeOut(time: Long): Boolean {
        ensureTimeline()
        return super.isTimeOut(time)
    }

    @Synchronized
    override fun isOutside(time: Long): Boolean {
        ensureTimeline()
        return super.isOutside(time)
    }

    @Synchronized
    override fun getAccurateLeft(displayer: IDisplayer, currTime: Long): Float {
        ensureTimeline()
        return motion?.leftAt(currTime) ?: displayer.width.toFloat()
    }

    @Synchronized
    override fun layout(displayer: IDisplayer, x: Float, y: Float) {
        ensureTimeline()
        val now = timer?.currMillisecond ?: return
        if (dropped || isOutside(now)) {
            setVisibility(false)
            return
        }
        var rowTop = y
        if (admission != null && !entered) {
            // layout() runs after application filtering and DFM's active primary filters.
            // Cache preparation never reserves rows.
            val availableTop = admission.findRow(this, displayer, now)
            when (scrollAdmission(now, actualTime, availableTop != null)) {
                ScrollAdmission.DROP -> {
                    dropped = true
                    setVisibility(false)
                    return
                }
                ScrollAdmission.WAIT -> {
                    setVisibility(false)
                    return
                }
                ScrollAdmission.ENTER -> {
                    rowTop = checkNotNull(availableTop)
                    // A delayed comment starts at the edge, never halfway across the picture.
                    motion = ScrollDanmakuMotion(now, displayer.width.toFloat(), pixelsPerMediaSecond)
                    entered = true
                    updateDuration()
                    admission.onEntered(this)
                }
            }
        }
        entered = true
        this.x = getAccurateLeft(displayer, now)
        if (!isShown) this.y = rowTop
        setVisibility(true)
    }
}

/** Draw-thread-only row ownership, independent of DFM's overwrite-on-overflow retainer. */
internal class ScrollDanmakuAdmission {
    private val entering = mutableListOf<FixedSpeedScrollDanmaku>()

    fun findRow(item: FixedSpeedScrollDanmaku, displayer: IDisplayer, now: Long): Float? {
        entering.removeAll { previous ->
            !previous.isShown || previous.isTimeOut(now) ||
                (previous.getRectAtTime(displayer, now)?.get(2) ?: 0f) <= displayer.width
        }
        val step = (item.paintHeight + displayer.margin).coerceAtLeast(1f)
        var top = displayer.allMarginTop.toFloat()
        while (top + item.paintHeight <= displayer.height) {
            if (entering.none { it.top < top + item.paintHeight && it.bottom > top }) return top
            top += step
        }
        return null
    }

    fun onEntered(item: FixedSpeedScrollDanmaku) {
        entering.add(item)
    }
}
