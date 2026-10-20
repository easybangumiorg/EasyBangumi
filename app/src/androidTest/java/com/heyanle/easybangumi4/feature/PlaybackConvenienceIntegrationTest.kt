package com.heyanle.easybangumi4.feature

import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.heyanle.easybangumi4.base.preferences.Preference
import com.heyanle.easybangumi4.cartoon.repository.CartoonRepository
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cloudsync.CloudSyncEventBatcher
import com.heyanle.easybangumi4.danmaku.DanmakuPreferences
import com.heyanle.easybangumi4.navControllerRef
import com.heyanle.easybangumi4.navigation.buildPlaybackDetailRoute
import com.heyanle.easybangumi4.plugin.source.SourceController
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.sourcelab.foregroundSourceLabForDeviceTests
import com.heyanle.easybangumi4.ui.cartoon_play.PlayerSleepTimerOption
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModelFactory
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayingViewModel
import com.heyanle.easybangumi4.ui.main.home.HomeViewModel
import com.heyanle.easybangumi4.ui.main.homePageIndexOkkv
import com.heyanle.easybangumi4.utils.getFilePath
import com.heyanle.easybangumi4.utils.toJson
import com.heyanle.easybangumi4.v2.MainV2Activity
import com.heyanle.inject.api.get
import com.heyanle.inject.core.Inject
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import loli.ball.easyplayer2.ControlViewModel
import loli.ball.easyplayer2.EasyPlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device integration evidence for the real V2 page, with a deterministic local video source.
 * These results are not network-source acceptance. Only the isolated .sourcelab package runs.
 * Supply fixtureVideoPath via instrumentation, or place a >= 15 second MP4 at
 * targetContext.cacheDir/feature-acceptance/fixture.mp4. The input video is never deleted.
 */
@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class PlaybackConvenienceIntegrationTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext

    private data class Session(
        val selection: CartoonPlayViewModel,
        val playback: CartoonPlayingViewModel,
        var firstFrame: Boolean = false,
        var mediaErrors: Int = 0,
    )

    private inner class Fixture(val caseName: String) {
        val token = UUID.randomUUID().toString().replace("-", "")
        val key = "test.device.features.$token"
        val id = "$caseName-$token"
        val title = "播放功能验收 $caseName"
        val output = File(app.cacheDir, "feature-acceptance/$caseName-$token").apply { mkdirs() }
        val facts = linkedMapOf<String, Any?>(
            "case" to caseName,
            "package" to app.packageName,
            "sourceKey" to key,
            "cartoonId" to id,
            "mediaKind" to "local-generated-mp4",
            "networkSourceAcceptance" to false,
        )
        val controller = Inject.get<SourceController>()
        val dao = Inject.get<CartoonInfoDao>()
        var scenario: ActivityScenario<MainV2Activity>? = null
        var home: HomeViewModel? = null
        var previousHomeSource: String? = null
        val sessions = mutableListOf<Session>()

        fun open(line: String = "good", progressMs: Long = 0L): Session {
            val enter = CartoonPlayViewModel.EnterData(
                playLineId = line, playLineLabel = "", playLineIndex = -1,
                episodeId = "ep1", episodeLabel = "", episodeOrder = -1, episodeIndex = -1,
                adviceProgress = progressMs, preferLocal = false,
            )
            // Create page-owned VMs in the navigation entry before its first composition, so the
            // first-frame listener cannot miss a fast local-media READY/render event.
            val session = onMain {
                val nav = requireNotNull(navControllerRef?.get())
                nav.navigate(buildPlaybackDetailRoute(id, key, enter.toJson()))
                val entry = requireNotNull(nav.currentBackStackEntry)
                val selection = ViewModelProvider(entry, CartoonPlayViewModelFactory(enter))[
                    CartoonPlayViewModel::class.java
                ]
                val playback = ViewModelProvider(entry)[CartoonPlayingViewModel::class.java]
                Session(selection, playback).also { session ->
                    playback.exoPlayer.volume = 0f
                    playback.exoPlayer.setPlaybackSpeed(1f)
                    playback.exoPlayer.addListener(object : Player.Listener {
                        override fun onRenderedFirstFrame() { session.firstFrame = true }
                        override fun onPlayerError(error: PlaybackException) { session.mediaErrors++ }
                    })
                    sessions += session
                }
            }
            // Android's main queue and Compose's test frame clock are separate. Drain the
            // navigation transition so the real page can compose and launch its play effects.
            compose.waitForIdle()
            return session
        }

        fun capture(name: String) {
            val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            File(output, "$name.png").outputStream().use {
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }

        fun ready(session: Session, requireFirstFrame: Boolean = true) {
            waitFor("real page reaches READY with video dimensions") {
                onMain {
                    session.playback.playerController.playbackState == EasyPlaybackState.READY &&
                        session.playback.exoPlayer.videoSize.width > 0 &&
                        session.playback.exoPlayer.videoSize.height > 0 &&
                        (!requireFirstFrame || session.firstFrame)
                }
            }
            val duration = onMain { session.playback.playerController.duration }
            assertTrue("Use a generated MP4 of at least 15 seconds", duration >= 15_000L)
            facts["videoDurationMs"] = duration
            facts["firstFrame"] = onMain { session.firstFrame }
        }

        fun showTimerSettings() {
            onMain {
                val entry = requireNotNull(navControllerRef?.get()?.currentBackStackEntry)
                ViewModelProvider(entry)[ControlViewModel::class.java].isNormalLockedControlShow = true
            }
            compose.onNodeWithContentDescription("播放设置").performClick()
            compose.onNodeWithTag("player_settings_tab_sleep_timer").performClick()
        }
    }

    @Test
    fun parseFailureAutomaticallySwitchesToPlayableLine() = fixture("fallback") { f ->
        val session = f.open("bad", 8_000L)
        waitFor("automatic line-switch notice") {
            onMain { session.playback.autoLineSwitch.value != null }
        }
        val notice = onMain { requireNotNull(session.playback.autoLineSwitch.value) }
        assertEquals("可播放线路", notice.lineLabel)
        assertEquals(1, notice.attempt)
        compose.onNodeWithText("取消换线").assertIsDisplayed()
        f.capture("automatic-switch-notice")
        f.ready(session)
        assertEquals("good", onMain { session.selection.curringPlayState.value?.playLine?.playLine?.id })
        val start = onMain { session.playback.playerController.currentPosition }
        assertTrue("Fallback retains the initial 8 second progress", start >= 7_500L)
        waitFor("three seconds of real playback after first frame", 10_000L) {
            onMain { session.playback.playerController.currentPosition - start >= 3_000L }
        }
        f.facts["fallbackAttempt"] = notice.attempt
        f.facts["positionAfterSwitchMs"] = start
        f.facts["progressAfterFirstFrameMs"] = onMain { session.playback.playerController.currentPosition - start }
        f.capture("fallback-playing")
    }

    @Test
    fun exoMediaErrorAutomaticallySwitchesToPlayableLine() = fixture("media_error") { f ->
        val session = f.open("bad-media", 8_000L)
        val observedAttempts = linkedSetOf<Int>()
        waitFor("actual Exo media error starts automatic fallback") {
            onMain {
                session.playback.autoLineSwitch.value?.let { observedAttempts += it.attempt }
                session.mediaErrors > 0 && session.playback.autoLineSwitch.value != null
            }
        }
        compose.onNodeWithText("取消换线").assertIsDisplayed()
        f.capture("exo-media-error-switch-notice")
        // The missing-file line is followed by the deliberate parser-failure line, then good.
        // This exercises a bounded mixed media/parse failure chain in the actual page.
        waitFor("mixed failure chain selects the playable line") {
            onMain {
                session.playback.autoLineSwitch.value?.let { observedAttempts += it.attempt }
                session.selection.curringPlayState.value?.playLine?.playLine?.id == "good"
            }
        }
        f.ready(session)
        assertTrue("A real Exo error callback must be observed", onMain { session.mediaErrors } > 0)
        assertTrue("Fallback attempts stay within the three-candidate bound",
            observedAttempts.isNotEmpty() && observedAttempts.all { it in 1..3 })
        assertEquals("good", onMain { session.selection.curringPlayState.value?.playLine?.playLine?.id })
        val start = onMain { session.playback.playerController.currentPosition }
        waitFor("three seconds of playback after Exo media failure", 10_000L) {
            onMain { session.playback.playerController.currentPosition - start >= 3_000L }
        }
        f.facts["exoMediaErrors"] = onMain { session.mediaErrors }
        f.facts["observedFallbackAttempts"] = observedAttempts.toList()
        f.facts["progressAfterFirstFrameMs"] = onMain { session.playback.playerController.currentPosition - start }
        f.capture("exo-media-error-recovered")
    }

    @Test
    fun cancelAutomaticSwitchPreventsDelayedPlayback() = fixture("cancel") { f ->
        val session = f.open("bad")
        waitFor("cancellable line-switch notice") {
            onMain { session.playback.autoLineSwitch.value != null }
        }
        compose.onNodeWithText("取消换线").performClick()
        waitFor("cancelled switch is an actionable stopped state") {
            onMain {
                session.playback.autoLineSwitch.value == null &&
                    session.playback.playingState.value.isError &&
                    !session.playback.playingState.value.isLoading
            }
        }
        val cancelledLine = onMain { session.selection.curringPlayState.value?.playLine?.playLine?.id }
        // Observe beyond the two-second scheduled-switch window, rather than checking only the
        // immediate cancellation state. Polling also catches a brief unintended late restart.
        val until = SystemClock.elapsedRealtime() + 3_500L
        while (SystemClock.elapsedRealtime() < until) {
            compose.mainClock.advanceTimeByFrame()
            onMain {
                assertFalse("Cancelled switch must not resume playback", session.playback.playerController.playWhenReady)
                assertEquals(cancelledLine, session.selection.curringPlayState.value?.playLine?.playLine?.id)
                assertFalse(session.firstFrame)
            }
            SystemClock.sleep(75L)
        }
        f.facts["cancelledLine"] = cancelledLine
        f.facts["observedStoppedMs"] = 3_500L
        f.capture("cancelled-switch")
    }

    @Test
    fun historyNoticeCancelReturnsToStart() = fixture("resume") { f ->
        val session = f.open(progressMs = 8_000L)
        f.ready(session)
        waitFor("history notification after READY", 5_000L) {
            onMain { session.playback.historyResumeNotice.value != null }
        }
        assertEquals(8_000L, onMain { session.playback.historyResumeNotice.value?.positionMs })
        compose.onNodeWithText("已跳转至 00:08").assertIsDisplayed()
        f.capture("history-jump-notice")
        compose.onNodeWithText("取消跳转").performClick()
        waitFor("cancel seeks to start", 3_000L) {
            onMain {
                session.playback.historyResumeNotice.value == null &&
                    session.playback.playerController.currentPosition < 2_000L
            }
        }
        f.facts["cancelledPositionMs"] = onMain { session.playback.playerController.currentPosition }
        f.capture("history-jump-cancelled")
    }

    @Test
    fun homeContinueWatchingReentersSavedEpisode() = fixture("continue") { f ->
        val session = f.open()
        f.ready(session)
        onMain { session.playback.playerController.seekTo(8_000L) }
        waitFor("seek applied before exiting") {
            onMain { session.playback.playerController.currentPosition >= 8_000L }
        }
        onMain { requireNotNull(navControllerRef?.get()).popBackStack() }
        waitFor("real exit saved fixture history") {
            runBlocking {
                f.dao.getByCartoonSummary(f.id, f.key)?.let {
                    it.lastHistoryTime > 0 && it.lastProcessTime >= 8_000L && it.lastProcessTime < it.lastTotalTile
                } == true
            }
        }
        compose.waitUntil(10_000L) {
            compose.onAllNodes(androidx.compose.ui.test.hasTestTag("continue-watching-play"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("继续观看${f.title}").assertIsDisplayed()
        f.capture("home-continue-watching")
        compose.onNodeWithTag("continue-watching-play").performClick()
        compose.waitForIdle()
        val reopened = onMain {
            val entry = requireNotNull(navControllerRef?.get()?.currentBackStackEntry)
            Session(
                ViewModelProvider(entry)[CartoonPlayViewModel::class.java],
                ViewModelProvider(entry)[CartoonPlayingViewModel::class.java],
            ).also { it.playback.exoPlayer.volume = 0f; f.sessions += it }
        }
        f.ready(reopened, requireFirstFrame = false)
        assertEquals(f.id, onMain { reopened.selection.curringPlayState.value?.cartoonSummary?.id })
        assertEquals("ep1", onMain { reopened.selection.curringPlayState.value?.episode?.id })
        assertTrue(onMain { reopened.playback.playerController.currentPosition >= 7_500L })
        f.facts["resumedPositionMs"] = onMain { reopened.playback.playerController.currentPosition }
        f.capture("continue-watching-reopened")
    }

    @Test
    fun timerUiCountsDownAndEpisodeEndDoesNotAdvance() = fixture("sleep") { f ->
        val session = f.open()
        f.ready(session)
        onMain { session.playback.playerController.pause() }
        f.showTimerSettings()
        compose.onNodeWithTag("player_sleep_timer_fifteen_minutes").performScrollTo().performClick()
        waitFor("15 minute countdown selected") {
            onMain { session.playback.sleepTimer.state.value.option == PlayerSleepTimerOption.FIFTEEN_MINUTES }
        }
        val initial = onMain { session.playback.sleepTimer.state.value.remainingMs }
        waitFor("countdown advances while paused", 4_000L) {
            onMain { session.playback.sleepTimer.state.value.remainingMs <= initial - 1_000L }
        }
        assertTrue(initial in 890_000L..900_000L)
        f.facts["fifteenMinuteInitialMs"] = initial
        f.facts["fifteenMinuteObservedMs"] = onMain { session.playback.sleepTimer.state.value.remainingMs }
        f.capture("sleep-fifteen-minute-countdown")
        compose.onNodeWithTag("player_sleep_timer_end_of_episode").performScrollTo().performClick()
        compose.onNodeWithContentDescription("关闭播放设置").performClick()
        onMain {
            val player = session.playback.playerController
            player.seekTo(player.duration - 2_000L)
            player.playWhenReady = true
        }
        waitFor("sleep timer stops at actual media end", 10_000L) {
            onMain { session.playback.sleepTimer.state.value.stoppedByTimer }
        }
        val until = SystemClock.elapsedRealtime() + 1_500L
        while (SystemClock.elapsedRealtime() < until) {
            compose.mainClock.advanceTimeByFrame()
            onMain {
                assertFalse(session.playback.playerController.playWhenReady)
                assertEquals("ep1", session.selection.curringPlayState.value?.episode?.id)
            }
            SystemClock.sleep(75L)
        }
        f.facts["endedEpisodeId"] = "ep1"
        f.facts["nextEpisodeAvailable"] = "ep2"
        f.facts["stoppedByTimer"] = true
        f.capture("sleep-ended-without-next-episode")
    }

    private fun fixture(name: String, block: (Fixture) -> Unit) {
        require(app.packageName == "com.heyanle.easybangumi4.sourcelab") {
            "This test must never run against a user package"
        }
        val video = File(InstrumentationRegistry.getArguments().getString("fixtureVideoPath")
            ?: File(app.cacheDir, "feature-acceptance/fixture.mp4").path).canonicalFile
        require(video.isFile && video.canRead() && video.length() > 0) { "Readable local fixture MP4 required: $video" }
        require(video.path.startsWith(app.cacheDir.canonicalPath + "/") ||
            video.path.startsWith(requireNotNull(app.getExternalFilesDir(null)).canonicalPath + "/")) {
            "Fixture video must be in the isolated app's cache or files"
        }
        val f = Fixture(name)
        val settings = Inject.get<SettingPreferences>()
        val danmaku = Inject.get<DanmakuPreferences>()
        val restorePreferences = listOf(
            snapshot(settings.playbackEngine), snapshot(settings.isInPrivate),
            snapshot(settings.playerOrientationMode), snapshot(settings.exoAdAudioProbeEnabled),
            snapshot(danmaku.enabledSourceIds),
        )
        val previousHomePage = homePageIndexOkkv
        val installedFile = File(app.getFilePath("source_v3"), "${f.key}.js")
        check(!installedFile.exists()) { "Unique fixture key unexpectedly exists" }
        println("PLAYBACK_FEATURE_START $name local-generated-mp4; not network source acceptance")
        var failure: Throwable? = null
        try {
            settings.playbackEngine.set(SettingPreferences.PlaybackEngine.EXO_PLAYER)
            settings.isInPrivate.set(false)
            settings.playerOrientationMode.set(SettingPreferences.PlayerOrientationMode.Disable)
            settings.exoAdAudioProbeEnabled.set(false)
            danmaku.enabledSourceIds.set(emptySet())
            homePageIndexOkkv = 0
            val source = File(f.output, "fixture.js").apply {
                writeText(fixtureSource(f, Uri.fromFile(video).toString()))
            }
            runBlocking { f.controller.appendOrUpdateSource(source) }
            waitFor("fixture loaded into application source bundle") {
                f.controller.sourceBundle.value?.play(f.key) != null &&
                    f.controller.sourceBundle.value?.detailed(f.key) != null
            }
            foregroundSourceLabForDeviceTests()
            f.scenario = ActivityScenario.launch(MainV2Activity::class.java)
            f.scenario?.onActivity { activity ->
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            compose.waitForIdle()
            waitFor("real home navigation available") {
                onMain { navControllerRef?.get()?.currentBackStackEntry?.destination?.route == "home" }
            }
            onMain {
                val entry = requireNotNull(navControllerRef?.get()?.currentBackStackEntry)
                f.home = ViewModelProvider(entry)[HomeViewModel::class.java]
                f.previousHomeSource = f.home?.stateFlow?.value?.selectionKey
                f.home?.changeSelectionSource(f.key)
            }
            block(f)
            f.facts["status"] = "PASS"
        } catch (error: Throwable) {
            failure = error
            f.facts["status"] = "FAIL"
            f.facts["error"] = "${error.javaClass.simpleName}: ${error.message}"
            runCatching { f.capture("failure") }
            throw error
        } finally {
            // Dispose the actual page before deleting its own fixture record, so its onExit save
            // cannot repopulate the row after cleanup. Never clear an existing source or history.
            val cleanupErrors = mutableListOf<Throwable>()
            fun cleanupStep(action: () -> Unit) {
                runCatching(action).exceptionOrNull()?.let(cleanupErrors::add)
            }
            cleanupStep {
                onMain {
                    navControllerRef?.get()?.popBackStack("home", false)
                    f.previousHomeSource?.let { f.home?.changeSelectionSource(it) }
                }
                compose.waitForIdle()
            }
            cleanupStep { f.scenario?.close() }
            cleanupStep {
                instrumentation.waitForIdleSync()
                runBlocking {
                    // Drain earlier onExit writes through the repository mutation lock before
                    // deleting only this test's identity. No existing record is changed.
                    Inject.get<CartoonRepository>().mutateCartoonInfo(
                        listOf(CartoonRepository.MutationTarget(f.id, f.key)),
                        notifyCloudSync = false,
                    ) { _, _ -> null }
                    f.dao.deleteByCartoonSummary(f.id, f.key)
                    val batcher = Inject.get<CloudSyncEventBatcher>()
                    batcher.markUploaded(batcher.flush().filterKeys { it.source == f.key })
                }
            }
            cleanupStep {
                check(!installedFile.exists() || installedFile.delete())
                f.controller.refresh()
            }
            restorePreferences.forEach { cleanupStep(it) }
            cleanupStep { homePageIndexOkkv = previousHomePage }
            f.facts["cleanupSucceeded"] = cleanupErrors.isEmpty()
            if (cleanupErrors.isNotEmpty()) {
                f.facts["cleanupError"] = cleanupErrors.map(Throwable::toString)
                f.facts["status"] = "FAIL"
            }
            File(f.output, "result.json").writeText(Gson().toJson(f.facts))
            println("PLAYBACK_FEATURE_RESULT ${f.facts["status"]} ${f.output.absolutePath}/result.json")
            cleanupErrors.forEach { error -> failure?.addSuppressed(error) }
            if (failure == null && cleanupErrors.isNotEmpty()) throw cleanupErrors.first()
        }
    }

    private fun fixtureSource(f: Fixture, mediaUri: String): String = """
        // @key ${f.key}
        // @label 本地播放功能验收
        // @versionName 1.0-test
        // @versionCode 1
        // @libVersion 15
        function PageComponent_getMainTabs() { return new NonLabelMainTab(MainTab.MAIN_TAB_WITH_COVER); }
        function PageComponent_getSubTabs(mainTab) { return new ArrayList(); }
        function PageComponent_getContent(mainTab, subTab, pageKey) {
            var covers = new ArrayList();
            covers.add(makeCartoonCover({ id: ${Gson().toJson(f.id)}, url: "fixture://playback", title: ${Gson().toJson(f.title)}, intro: "本地合成视频验收", cover: "" }));
            return new Pair(null, covers);
        }
        function DetailedComponent_getDetailed(summary) {
            var episodes = new ArrayList();
            episodes.add(new Episode("ep1", "第1集", 0));
            episodes.add(new Episode("ep2", "第2集", 1));
            var lines = new ArrayList();
            if (${Gson().toJson(f.caseName)} == "media_error") {
                lines.add(new PlayLine("bad-media", "预期媒体文件错误线路", episodes));
            }
            lines.add(new PlayLine("bad", "预期解析失败线路", episodes));
            lines.add(new PlayLine("good", "可播放线路", episodes));
            var cartoon = makeCartoon({ id: summary.id, url: "fixture://playback", title: ${Gson().toJson(f.title)}, cover: "", intro: "本地合成视频验收", description: "不代表网络源验收", updateStrategy: 2, status: 2 });
            return new Pair(cartoon, lines);
        }
        function PlayComponent_getPlayInfo(summary, line, episode) {
            if (String(line.id) == "bad-media") {
                return new PlayerInfo(PlayerInfo.DECODE_TYPE_OTHER, ${Gson().toJson(Uri.fromFile(File(f.output, "intentionally-missing.mp4")).toString())});
            }
            if (String(line.id) == "bad") throw new Error("Expected fixture parser failure");
            return new PlayerInfo(PlayerInfo.DECODE_TYPE_OTHER, ${Gson().toJson(mediaUri)});
        }
    """.trimIndent()

    private fun waitFor(description: String, timeoutMs: Long = 25_000L, predicate: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            // VM/network/media work uses the Android clock, but page transitions/recomposition
            // require Compose frames even when this loop only reads VM state on the main thread.
            compose.mainClock.advanceTimeByFrame()
            if (predicate()) return
            SystemClock.sleep(50L)
        }
        throw AssertionError("Timed out: $description")
    }

    private fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        instrumentation.runOnMainSync { result = runCatching(block) }
        return requireNotNull(result).getOrThrow()
    }

    private fun <T> snapshot(preference: Preference<T>): () -> Unit {
        val wasSet = preference.isSet()
        val value = preference.get()
        return { if (wasSet) preference.set(value) else preference.delete() }
    }
}
