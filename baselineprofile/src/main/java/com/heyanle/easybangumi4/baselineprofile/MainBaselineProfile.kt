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

        val w = device.displayWidth
        val h = device.displayHeight

        // Fresh benchmark installs start on the three-page setup guide. Advance through
        // it without opening optional permission/folder pickers, then continue on home.
        var onboardingSteps = 0
        while (onboardingSteps < 4) {
            val hasNextButton = device.wait(Until.hasObject(By.text("下一步")), 2_000)
            if (!hasNextButton) break
            device.click(w / 2, h * 9 / 10)
            device.waitForIdle(1_000)
            onboardingSteps++
        }

        // The performance version suffix can make the local update checker show release notes.
        if (device.wait(Until.hasObject(By.text("取消")), 3_000)) {
            device.click(w * 4 / 5, h * 9 / 10)
            device.waitForIdle(1_000)
        }

        // Wait for the V2 home destination before exercising its source-backed grid.
        device.wait(Until.hasObject(By.text("日番")), 30_000)
        device.waitForIdle(2_000)

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
            val detailOpened = firstCardText?.let {
                device.click(it.visibleBounds.centerX(), it.visibleBounds.centerY() - 200)
                device.wait(Until.hasObject(By.text("外部播放")), 15_000)
            } == true
            if (detailOpened) {
                device.waitForIdle(1_500)
                device.pressBack()
                device.waitForIdle(1_000)
            }
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
