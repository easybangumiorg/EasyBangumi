package com.heyanle.easybangumi4.danmaku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanmakuRendererSyncPolicyTest {

    @Test
    fun pausedInitialAttachmentStartsAtCurrentPositionAndThenPauses() {
        val policy = DanmakuRendererSyncPolicy()

        assertEquals(
            listOf(DanmakuRendererCommand.PrepareAttachedView),
            policy.onAttach(replacingView = false, positionMillis = 12_345L, isPlaying = false),
        )
        assertEquals(
            listOf(
                DanmakuRendererCommand.ReplaceItems,
                DanmakuRendererCommand.StartPausedAt(12_345L),
            ),
            policy.onPrepared(),
        )
    }

    @Test
    fun pauseAndResumeNeverSeekOrRestartTheClock() {
        val policy = preparedPolicy(positionMillis = 2_000L, isPlaying = true)

        assertEquals(
            listOf(DanmakuRendererCommand.Pause),
            policy.onPlaybackChanged(false),
        )
        assertEquals(
            listOf(DanmakuRendererCommand.Resume),
            policy.onPlaybackChanged(true),
        )
        assertTrue(policy.onPlaybackChanged(true).isEmpty())
    }

    @Test
    fun seekUsesTheNewPositionAndKeepsAPausedClockPaused() {
        val playing = preparedPolicy(positionMillis = 1_000L, isPlaying = true)
        assertEquals(
            listOf(DanmakuRendererCommand.SeekTo(8_000L)),
            playing.onPositionDiscontinuity(8_000L),
        )

        val paused = preparedPolicy(positionMillis = 1_000L, isPlaying = false)
        assertEquals(
            listOf(DanmakuRendererCommand.SeekPausedTo(8_000L)),
            paused.onPositionDiscontinuity(8_000L),
        )
    }

    @Test
    fun recreatedViewReleasesOldViewAndStartsFromLatestPlayerPosition() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = true)

        assertEquals(
            listOf(
                DanmakuRendererCommand.ReleaseAttachedView,
                DanmakuRendererCommand.PrepareAttachedView,
            ),
            policy.onAttach(replacingView = true, positionMillis = 43_210L, isPlaying = false),
        )
        assertEquals(
            listOf(
                DanmakuRendererCommand.ReplaceItems,
                DanmakuRendererCommand.StartPausedAt(43_210L),
            ),
            policy.onPrepared(),
        )
    }

    @Test
    fun sameViewRecompositionDoesNotSeekOrRestart() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = true)

        assertTrue(
            policy.onAttach(
                replacingView = false,
                positionMillis = 9_000L,
                isPlaying = true,
            ).isEmpty(),
        )
        assertTrue(policy.onPrepared().isEmpty())
    }

    @Test
    fun repeatedSameViewHolderUpdatesNeverReleaseOrPrepareAgain() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = true)

        val commands = buildList {
            addAll(
                policy.onAttach(
                    replacingView = false,
                    positionMillis = 2_000L,
                    isPlaying = true,
                ),
            )
            addAll(
                policy.onAttach(
                    replacingView = false,
                    positionMillis = 3_000L,
                    isPlaying = false,
                ),
            )
            addAll(
                policy.onAttach(
                    replacingView = false,
                    positionMillis = 4_000L,
                    isPlaying = true,
                ),
            )
        }

        assertEquals(
            listOf(
                DanmakuRendererCommand.Pause,
                DanmakuRendererCommand.Resume,
            ),
            commands,
        )
        assertTrue(DanmakuRendererCommand.ReleaseAttachedView !in commands)
        assertTrue(DanmakuRendererCommand.PrepareAttachedView !in commands)
        assertTrue(policy.onPrepared().isEmpty())
    }

    @Test
    fun commentReplacementReseedsAtCurrentPositionWithoutUnpausing() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = false)

        assertEquals(
            listOf(
                DanmakuRendererCommand.ReplaceItems,
                DanmakuRendererCommand.SeekPausedTo(7_500L),
            ),
            policy.onCommentsChanged(contentsChanged = true, positionMillis = 7_500L),
        )
        assertTrue(policy.onCommentsChanged(contentsChanged = false, positionMillis = 8_000L).isEmpty())
    }

    @Test
    fun visibilityOnlyConfigurationDoesNotReplaceOrMoveItems() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = true)

        assertTrue(
            policy.onConfigurationChanged(
                DanmakuRendererConfigEffect.VISIBILITY_ONLY,
                positionMillis = 9_000L,
            ).isEmpty(),
        )
    }

    @Test
    fun styleConfigurationUpdatesNativeStateWithoutReplacingItemsOrTouchingPausedClock() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = false)

        assertTrue(
            policy.onConfigurationChanged(
                DanmakuRendererConfigEffect.STYLE,
                positionMillis = 9_500L,
            ).isEmpty(),
        )
    }

    @Test
    fun configurationAfterSeekDoesNotIssueASecondTimelineCommand() {
        val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = true)
        policy.onPositionDiscontinuity(20_000L)

        assertTrue(
            policy.onConfigurationChanged(
                DanmakuRendererConfigEffect.CONTENT,
                positionMillis = 20_120L,
            ).isEmpty(),
        )
    }

    @Test
    fun configurationNeverEmitsPlaybackCommandsRegardlessOfPlaybackState() {
        listOf(false, true).forEach { isPlaying ->
            val policy = preparedPolicy(positionMillis = 1_000L, isPlaying = isPlaying)

            val commands = policy.onConfigurationChanged(
                DanmakuRendererConfigEffect.CONTENT,
                positionMillis = 2_000L,
            )

            assertTrue(commands.isEmpty())
            assertTrue(commands.none { command ->
                    command is DanmakuRendererCommand.SeekTo ||
                    command is DanmakuRendererCommand.SeekPausedTo ||
                    command is DanmakuRendererCommand.StartAt ||
                    command is DanmakuRendererCommand.StartPausedAt ||
                    command == DanmakuRendererCommand.Pause ||
                    command == DanmakuRendererCommand.Resume
            })
        }
    }

    @Test
    fun configurationBeforePreparationIsAppliedWithoutStartingTheClock() {
        val policy = DanmakuRendererSyncPolicy()
        policy.onAttach(replacingView = false, positionMillis = 4_000L, isPlaying = false)

        assertTrue(
            policy.onConfigurationChanged(
                DanmakuRendererConfigEffect.STYLE,
                positionMillis = 4_200L,
            ).isEmpty(),
        )
        assertEquals(
            listOf(
                DanmakuRendererCommand.ReplaceItems,
                DanmakuRendererCommand.StartPausedAt(4_200L),
            ),
            policy.onPrepared(),
        )
    }

    @Test
    fun densityChangeRebuildsItemsAtTheDesiredPosition() {
        val policy = preparedPolicy(positionMillis = 12_000L, isPlaying = true)

        val commands = policy.onConfigurationChanged(
            DanmakuRendererConfigEffect.REPLACE_ITEMS,
            positionMillis = 15_000L,
        )

        assertEquals(
            listOf(
                DanmakuRendererCommand.ReplaceItems,
                DanmakuRendererCommand.SeekTo(15_000L),
            ),
            commands,
        )
    }

    @Test
    fun configChangeClassifierUsesTheMostExpensiveRequiredEffect() {
        val default = DanmakuDisplayConfig.DEFAULT

        assertEquals(
            DanmakuRendererConfigEffect.NONE,
            classifyDanmakuConfigChange(default, default.copy()),
        )
        assertEquals(
            DanmakuRendererConfigEffect.VISIBILITY_ONLY,
            classifyDanmakuConfigChange(default, default.copy(enabled = false)),
        )
        assertEquals(
            DanmakuRendererConfigEffect.CONTENT,
            classifyDanmakuConfigChange(default, default.copy(showTop = false)),
        )
        assertEquals(
            DanmakuRendererConfigEffect.STYLE,
            classifyDanmakuConfigChange(
                default,
                default.copy(showTop = false, fontSizeSp = 24f),
            ),
        )
        assertEquals(
            DanmakuRendererConfigEffect.STYLE,
            classifyDanmakuConfigChange(default, default.copy(opacity = 0.5f)),
        )
        // 显示区域由 Compose 布局承载，纯区域变化不需要原生渲染命令。
        assertEquals(
            DanmakuRendererConfigEffect.NONE,
            classifyDanmakuConfigChange(default, default.copy(areaRatio = 0.5f)),
        )
        assertEquals(
            DanmakuRendererConfigEffect.REPLACE_ITEMS,
            classifyDanmakuConfigChange(default, default.copy(densityRatio = 0.5f)),
        )
        assertEquals(
            DanmakuRendererConfigEffect.REPLACE_ITEMS,
            classifyDanmakuConfigChange(default, default.copy(mergeRepeatWindowMillis = 3000L)),
        )
        assertEquals(
            DanmakuRendererConfigEffect.REPLACE_ITEMS,
            classifyDanmakuConfigChange(
                default,
                default.copy(densityRatio = 0.5f, fontSizeSp = 24f),
            ),
        )
        assertEquals(
            DanmakuRendererConfigEffect.STYLE,
            classifyDanmakuConfigChange(
                default,
                default.copy(areaRatio = 0.5f, fontSizeSp = 24f),
            ),
        )
    }

    private fun preparedPolicy(
        positionMillis: Long,
        isPlaying: Boolean,
    ) = DanmakuRendererSyncPolicy().apply {
        onAttach(replacingView = false, positionMillis = positionMillis, isPlaying = isPlaying)
        onPrepared()
    }
}
