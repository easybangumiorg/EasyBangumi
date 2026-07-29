package com.heyanle.easybangumi4.ui.cartoon_play

import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.ContextThemeWrapper
import android.widget.FrameLayout
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heyanle.easybangumi4.MainActivity
import com.heyanle.easybangumi4.danmaku.DanmakuComment
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayMode
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.ui.widget.DanmakuView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DfmDanmakuRendererInstrumentedTest {

    @Test
    fun rendererOwnedView_holderReplacementReusesRendererSession() {
        val renderer = DfmDanmakuRenderer()
        lateinit var originalView: DanmakuView
        lateinit var originalContext: DanmakuContext
        lateinit var originalHolder: FrameLayout
        lateinit var replacementHolder: FrameLayout
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                originalHolder = FrameLayout(activity)
                replacementHolder = FrameLayout(activity)
                val root = FrameLayout(activity).apply {
                    addView(originalHolder)
                    addView(replacementHolder)
                }
                activity.setContentView(root)

                originalView = renderer.getOrCreateView(
                    androidContext = activity,
                    positionMillis = 12_000L,
                    isPlaying = false,
                )
                originalHolder.addView(originalView)
                // prepare() installs the DFM handler and its context synchronously. Completion is
                // asynchronous and device-dependent, so context identity is the stable signal:
                // release() clears the handler while another prepare creates a new context.
                originalContext = checkNotNull(originalView.config)
            }

            scenario.onActivity { activity ->
                val wasPreparedBeforeReplacement = originalView.isPrepared
                val reusedView = renderer.getOrCreateView(
                    androidContext = activity,
                    positionMillis = 14_000L,
                    isPlaying = false,
                )

                // This is the same hand-off performed by AndroidView.factory when Compose has
                // created a replacement holder before disposing the previous one.
                (reusedView.parent as? ViewGroup)?.removeView(reusedView)
                replacementHolder.addView(reusedView)

                assertSame(originalView, reusedView)
                assertSame(originalContext, reusedView.config)
                assertEquals(wasPreparedBeforeReplacement, reusedView.isPrepared)
                assertEquals(0, originalHolder.childCount)
                assertSame(replacementHolder, reusedView.parent)
            }
        } finally {
            scenario.onActivity { renderer.release() }
            scenario.close()
        }
    }

    @Test
    fun rendererOwnedView_differentContextReplacesSessionAndFinalReleaseIsIdempotent() {
        val renderer = DfmDanmakuRenderer()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                val originalView = renderer.getOrCreateView(
                    androidContext = ContextThemeWrapper(
                        activity,
                        android.R.style.Theme_DeviceDefault,
                    ),
                    positionMillis = 12_000L,
                    isPlaying = false,
                )
                val originalContext = checkNotNull(originalView.config)

                val replacementView = renderer.getOrCreateView(
                    androidContext = ContextThemeWrapper(
                        activity,
                        android.R.style.Theme_DeviceDefault,
                    ),
                    positionMillis = 14_000L,
                    isPlaying = false,
                )

                assertNotSame(originalView, replacementView)
                assertNotSame(originalContext, replacementView.config)
                assertNull(originalView.config)

                renderer.release()
                renderer.release()
                assertNull(replacementView.config)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun dfmSpeedFactorChangesOnlyScrollingDurationAndMarginIsNonNegativePixels() {
        val context = DanmakuContext.create()
        context.displayer.setSize(1_920, 1_080)
        val scrolling = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, context)
        val fixed = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_FIX_TOP, context)
        val initialScrollingDuration = scrolling.duration.value
        val initialFixedDuration = fixed.duration.value

        context.setScrollSpeedFactor(0.5f)
        context.setDanmakuMargin(24)

        assertEquals(initialScrollingDuration / 2L, scrolling.duration.value)
        assertEquals(initialFixedDuration, fixed.duration.value)
        assertEquals(24, context.displayer.margin)
    }

    @Test
    fun largeCommentSet_prepareSeekPauseResumeClearAndRelease_doesNotCrash() {
        val renderer = DfmDanmakuRenderer()
        var danmakuView: DanmakuView? = null
        val comments = List(5_000) { index ->
            DanmakuComment(
                id = index.toLong(),
                timeMillis = 10_000L + index * 50L,
                mode = DanmakuDisplayMode.entries[index % DanmakuDisplayMode.entries.size],
                colorArgb = Color.WHITE,
                userId = "device-test",
                text = "大数据弹幕 $index",
            )
        }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                danmakuView = activity.installDanmakuView()
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        enabled = true,
                        fontSizeSp = 12f,
                        lineHeightFactor = 1f,
                        scrollSpeed = 0.5f,
                    ),
                    positionMillis = 120_000L,
                )
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                renderer.attach(danmakuView!!, positionMillis = 120_000L, isPlaying = false)
                renderer.setComments(comments, timeOffsetMillis = 0L, positionMillis = 120_000L)
            }
            awaitPrepared(scenario) { danmakuView }

            scenario.onActivity {
                assertTrue(danmakuView?.isPrepared == true)
                assertFalse(danmakuView?.isClickable == true)
                // Exercise both ends of every supported style range while paused. Configuration
                // must reuse the prepared view and preserve the renderer clock state.
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        fontSizeSp = 36f,
                        lineHeightFactor = 2f,
                        scrollSpeed = 2f,
                    ),
                    positionMillis = 120_000L,
                )
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        fontSizeSp = 24f,
                        lineHeightFactor = 1.5f,
                        scrollSpeed = 1.25f,
                    ),
                    positionMillis = 120_000L,
                )
                renderer.seekTo(180_000L)
                renderer.resume()
                renderer.pause()
                // Model a true native-view replacement separately from the holder-only
                // fullscreen path, which now reuses the renderer-owned native view.
                danmakuView = it.installDanmakuView()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity {
                renderer.attach(danmakuView!!, positionMillis = 180_000L, isPlaying = false)
            }
            awaitPrepared(scenario) { danmakuView }

            scenario.onActivity {
                assertTrue(danmakuView?.isPrepared == true)
                renderer.clear()
                renderer.release()
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun pausedRenderer_displayConfigurationDoesNotResumeClock() {
        val renderer = DfmDanmakuRenderer()
        var danmakuView: DanmakuView? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                danmakuView = renderer.getOrCreateView(
                    androidContext = activity,
                    positionMillis = 60_000L,
                    isPlaying = false,
                )
                activity.installDanmakuView(danmakuView!!)
            }
            awaitPrepared(scenario) { danmakuView }

            scenario.onActivity {
                renderer.pause()
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                assertTrue("fixture must be paused before changing config", danmakuView!!.isPaused)
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        fontSizeSp = 28f,
                        lineHeightFactor = 1.4f,
                        scrollSpeed = 1.5f,
                    ),
                    positionMillis = 60_000L,
                )
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            SystemClock.sleep(150L)

            scenario.onActivity {
                assertTrue(
                    "display-only configuration must not resume DFM playback",
                    danmakuView!!.isPaused,
                )
            }
        } finally {
            scenario.onActivity { renderer.release() }
            scenario.close()
        }
    }

    @Test
    fun preparedRenderer_displayConfigurationUpdatesInPlaceWithoutClearingTimeline() {
        val renderer = DfmDanmakuRenderer()
        var danmakuView: TrackingDanmakuView? = null
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            scenario.onActivity { activity ->
                danmakuView = TrackingDanmakuView(activity)
                activity.installDanmakuView(danmakuView!!)
                renderer.attach(danmakuView!!, positionMillis = 60_000L, isPlaying = true)
            }
            awaitPrepared(scenario) { danmakuView }
            scenario.onActivity {
                renderer.setComments(
                    comments = List(200) { index ->
                        DanmakuComment(
                            id = index.toLong(),
                            timeMillis = 50_000L + index * 100L,
                            mode = DanmakuDisplayMode.entries[index % 3],
                            colorArgb = Color.WHITE,
                            userId = "configuration-test",
                            text = "配置热更新 $index",
                        )
                    },
                    timeOffsetMillis = 0L,
                    positionMillis = 60_000L,
                )
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            scenario.onActivity {
                val removeCallsBefore = danmakuView!!.removeAllCalls
                val addCallsBefore = danmakuView!!.addCalls

                // STYLE is the dominant classification, but this snapshot also exercises all
                // content settings to ensure the entire configuration is applied in place.
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        showScroll = false,
                        showTop = false,
                        enabledProvenance = emptySet(),
                        timeOffsetMillis = 1_500L,
                        fontSizeSp = 27f,
                        lineHeightFactor = 1.5f,
                        scrollSpeed = 2f,
                    ),
                    positionMillis = 60_000L,
                )
                // Exercise a content-only transition after the combined transition.
                renderer.setDisplayConfig(
                    DanmakuDisplayConfig.DEFAULT.copy(
                        showBottom = false,
                        timeOffsetMillis = -500L,
                    ),
                    positionMillis = 60_000L,
                )

                assertEquals(removeCallsBefore, danmakuView!!.removeAllCalls)
                assertEquals(addCallsBefore, danmakuView!!.addCalls)
                assertTrue(danmakuView!!.config.getR2LDanmakuVisibility())
                assertTrue(danmakuView!!.config.getFTDanmakuVisibility())
                assertFalse(danmakuView!!.config.getFBDanmakuVisibility())
                assertEquals(1f, danmakuView!!.config.scaleTextSize, 0f)
            }
        } finally {
            scenario.onActivity { renderer.release() }
            scenario.close()
        }
    }

    private fun awaitPrepared(
        scenario: ActivityScenario<MainActivity>,
        timeoutMillis: Long = 3_000L,
        viewProvider: () -> DanmakuView?,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        var diagnostics = "view unavailable"
        while (SystemClock.uptimeMillis() < deadline) {
            var prepared = false
            scenario.onActivity {
                val view = viewProvider()
                prepared = view?.isPrepared == true
                diagnostics = if (view == null) {
                    "view unavailable"
                } else {
                    "ready=${view.isViewReady}, shown=${view.isShown}, " +
                        "attached=${view.isAttachedToWindow}, size=${view.width}x${view.height}, " +
                        "visibility=${view.visibility}, config=${view.config != null}"
                }
            }
            if (prepared) return
            SystemClock.sleep(25L)
        }
        scenario.onActivity {
            assertTrue(
                "DanmakuView was not prepared within ${timeoutMillis}ms ($diagnostics)",
                viewProvider()?.isPrepared == true,
            )
        }
    }

    private fun MainActivity.installDanmakuView(): DanmakuView {
        val view = DanmakuView(this)
        installDanmakuView(view)
        return view
    }

    private fun MainActivity.installDanmakuView(view: DanmakuView) {
        val root = FrameLayout(this).apply {
            addView(
                view,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        setContentView(root)
        // ActivityScenario can run while the physical display is off or covered by the test
        // runner. Give this renderer-focused stress fixture deterministic bounds so DFM receives
        // its required onLayout/isViewReady signal independently of device window visibility.
        val width = resources.displayMetrics.widthPixels.coerceAtLeast(1)
        val height = resources.displayMetrics.heightPixels.coerceAtLeast(1)
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, width, height)
    }

    private class TrackingDanmakuView(
        context: android.content.Context,
    ) : DanmakuView(context) {
        var removeAllCalls = 0
            private set
        var addCalls = 0
            private set

        override fun removeAllDanmakus(isClearOnScreen: Boolean) {
            removeAllCalls += 1
            super.removeAllDanmakus(isClearOnScreen)
        }

        override fun addDanmaku(item: BaseDanmaku) {
            addCalls += 1
            super.addDanmaku(item)
        }
    }

}
