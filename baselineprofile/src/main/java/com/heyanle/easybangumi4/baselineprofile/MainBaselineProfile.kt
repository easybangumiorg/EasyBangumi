package com.heyanle.easybangumi4.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Collects the app baseline profile on a real device.
 *
 * Journey: cold start -> wait for home source list -> fling grid -> open a detail
 * page -> back. The detail-page leg is best-effort: tapping must not fail the whole
 * collection, so optional steps are wrapped and the mandatory legs (startup + scroll)
 * always run.
 *
 * Run with: ./gradlew :app:generatePerformanceBaselineProfile
 */
class MainBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(PACKAGE_NAME) {
        // --- Mandatory: cold start through splash into home ---
        pressHome()
        startActivityAndWait()

        // Home waits for JS sources (Rhino + network) before the grid renders.
        device.wait(Until.hasObject(By.text("更新时刻表")), 30_000)
        device.waitForIdle(2_000)

        val w = device.displayWidth
        val h = device.displayHeight

        // --- Mandatory: fling the cover grid several screens ---
        repeat(4) {
            device.swipe(w / 2, h * 3 / 4, w / 2, h / 4, 300)
            device.waitForIdle(800)
        }
        repeat(2) {
            device.swipe(w / 2, h / 4, w / 2, h * 3 / 4, 300)
            device.waitForIdle(800)
        }

        // --- Best effort: open the first anime detail page (Jsoup-heavy path) ---
        runCatching {
            val firstCardText = device.findObjects(By.clazz("android.widget.TextView"))
                .firstOrNull { node ->
                    val b = node.visibleBounds
                    b.width() > 60 && b.height() > 30 && b.centerY() in 700..1800
                }
            firstCardText?.let {
                device.click(it.visibleBounds.centerX(), it.visibleBounds.centerY() - 200)
            }
            device.wait(Until.hasObject(By.text("播放线路")), 15_000)
            device.waitForIdle(1_500)
            device.pressBack()
            device.waitForIdle(1_000)
        }

        // One more scroll pass after returning, covering recomposition of cached items.
        repeat(2) {
            device.swipe(w / 2, h * 3 / 4, w / 2, h / 4, 300)
            device.waitForIdle(600)
        }
    }

    private companion object {
        const val PACKAGE_NAME = "com.heyanle.easybangumi4.performance"
    }
}
