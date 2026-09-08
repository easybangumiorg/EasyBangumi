package com.heyanle.easybangumi4.ui.cartoon_play

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.easybangumi4.danmaku.DANDANPLAY_SOURCE_ID
import com.heyanle.easybangumi4.danmaku.DanmakuComment
import com.heyanle.easybangumi4.danmaku.applyDisplaySampling
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayMode
import com.heyanle.easybangumi4.danmaku.DanmakuRendererCommand
import com.heyanle.easybangumi4.danmaku.DanmakuRendererConfigEffect
import com.heyanle.easybangumi4.danmaku.DanmakuRendererSyncPolicy
import com.heyanle.easybangumi4.danmaku.PlaybackTimelineClock
import com.heyanle.easybangumi4.danmaku.classifyDanmakuConfigChange
import com.heyanle.easybangumi4.danmaku.resolveDanmakuCanvasHeightPx
import com.heyanle.easybangumi4.danmaku.normalizedPlaybackSpeed
import com.heyanle.easybangumi4.danmaku.toDfmStyle
import kotlinx.coroutines.delay
import loli.ball.easyplayer2.EasyPlayerController
import master.flame.danmaku.controller.DanmakuFilters
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import kotlin.math.roundToInt

/** Imperative adapter around DanmakuFlameMaster; the caller owns normalized comment state. */
class DfmDanmakuRenderer {
    private var view: DanmakuView? = null
    private var context: DanmakuContext? = null
    private var pendingComments: List<DanmakuComment> = emptyList()
    private var pendingBindingOffsetMillis: Long = 0L
    private var appliedConfig = DanmakuDisplayConfig.DEFAULT
    private var playbackSpeed = 1f
    private val playbackTimelineClock = PlaybackTimelineClock()
    private val renderedItems = mutableListOf<RenderedDanmaku>()
    private val syncPolicy = DanmakuRendererSyncPolicy()

    /**
     * Returns the renderer-owned native view, creating and preparing it only when necessary.
     *
     * Compose may recreate an [AndroidView] holder for a configuration change while keeping the
     * surrounding composition and renderer alive. Reusing the native view across that holder
     * replacement avoids a release/prepare gap in which the video keeps rendering but danmaku is
     * temporarily empty. A different Android [Context] still creates a fresh view so an Activity
     * is never retained across a real recreation.
     */
    fun getOrCreateView(
        androidContext: Context,
        positionMillis: Long,
        isPlaying: Boolean,
    ): DanmakuView {
        val currentView = view
        if (currentView != null && currentView.context === androidContext) {
            attach(currentView, positionMillis, isPlaying)
            return currentView
        }

        return PlaybackAwareDanmakuView(androidContext).apply {
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            setBackgroundColor(Color.TRANSPARENT)
            this@DfmDanmakuRenderer.attach(this, positionMillis, isPlaying)
        }
    }

    /**
     * DFM owns a separate clock from ExoPlayer. A true View/Context replacement must seed the
     * newly attached view from the player's *current* position.
     */
    internal fun attach(view: DanmakuView, positionMillis: Long, isPlaying: Boolean) {
        val sameView = this.view === view
        val commands = syncPolicy.onAttach(
            replacingView = !sameView && this.view != null,
            positionMillis = positionMillis,
            isPlaying = isPlaying,
        )
        if (sameView) {
            execute(commands)
            return
        }
        if (DanmakuRendererCommand.ReleaseAttachedView in commands) releaseViewResources()
        this.view = view
        val newContext = DanmakuContext.create()
            .setDanmakuStyle(1, 3f)
            .setDanmakuBold(false)
            .setDuplicateMergingEnabled(true)
        newContext.registerFilter(provenanceFilter)
        applyDisplayConfig(newContext, appliedConfig, view)
        context = newContext
        setVisible(appliedConfig.enabled)
        view.setCallback(object : DrawHandler.Callback {
            override fun prepared() {
                // DFM may invoke this callback from its DrawHandler thread. Serialize renderer
                // state changes on the View thread and validate the generation there: a released
                // view can finish preparing after another Activity/view has already been attached.
                view.post {
                    if (
                        this@DfmDanmakuRenderer.view === view &&
                        this@DfmDanmakuRenderer.context === newContext
                    ) {
                        execute(syncPolicy.onPrepared())
                    }
                }
            }

            override fun updateTimer(timer: DanmakuTimer) {
                timer.update(playbackTimelineClock.positionAt(SystemClock.elapsedRealtime()))
            }
            override fun danmakuShown(danmaku: BaseDanmaku) = Unit
            override fun drawingFinished() = Unit
        })
        view.prepare(EmptyDanmakuParser(), newContext)
        (view as? PlaybackAwareDanmakuView)?.enablePlayerClock()
    }

    /**
     * Applies a normalized snapshot without touching ExoPlayer.
     *
     * Visibility is a cheap View change. Content, timing, and style settings update DFM's live
     * filters/global flags and retain both the item set and the playing/paused clock state.
     */
    fun setDisplayConfig(
        config: DanmakuDisplayConfig,
        positionMillis: Long,
    ) {
        val normalized = config.normalized()
        val effect = classifyDanmakuConfigChange(appliedConfig, normalized)
        setVisible(normalized.enabled)
        // 先落快照再分派：显示区域这类纯布局字段变化会归为 NONE，
        // appliedConfig 仍必须前进到最新值，否则下一次 classify 会拿旧值误判。
        appliedConfig = normalized
        if (effect == DanmakuRendererConfigEffect.NONE) return

        context?.let { currentContext ->
            view?.let { currentView ->
                when (effect) {
                    DanmakuRendererConfigEffect.NONE,
                    DanmakuRendererConfigEffect.VISIBILITY_ONLY,
                    -> Unit
                    DanmakuRendererConfigEffect.CONTENT -> {
                        applyContentConfig(currentContext, normalized)
                    }
                    DanmakuRendererConfigEffect.STYLE,
                    DanmakuRendererConfigEffect.REPLACE_ITEMS,
                    -> {
                        // REPLACE_ITEMS 与 STYLE 同路径应用原生样式；
                        // 条目集重建由 onConfigurationChanged 返回的命令触发。
                        applyDisplayConfig(currentContext, normalized, currentView)
                    }
                }
            }
        }
        execute(syncPolicy.onConfigurationChanged(effect, positionMillis))
    }

    fun setComments(
        comments: List<DanmakuComment>,
        timeOffsetMillis: Long,
        positionMillis: Long,
    ) {
        val contentsChanged =
            pendingComments != comments || pendingBindingOffsetMillis != timeOffsetMillis
        pendingComments = comments
        pendingBindingOffsetMillis = timeOffsetMillis
        execute(syncPolicy.onCommentsChanged(contentsChanged, positionMillis))
    }

    fun seekTo(positionMillis: Long) {
        renderedItems.forEach { rendered ->
            (rendered.item as? FixedSpeedScrollDanmaku)?.requestTimelineReset()
        }
        execute(syncPolicy.onPositionDiscontinuity(positionMillis))
    }

    /** Resume the DFM clock in place; seeking here replays visible danmaku after a pause. */
    fun resume() {
        execute(syncPolicy.onPlaybackChanged(true))
    }

    fun pause() {
        execute(syncPolicy.onPlaybackChanged(false))
    }

    fun setVisible(visible: Boolean) {
        view?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setPlaybackSpeed(speed: Float) {
        val normalized = speed.normalizedPlaybackSpeed()
        if (playbackSpeed == normalized) return
        playbackSpeed = normalized
        // The DFM clock advances in media time. Synced motion therefore speeds up automatically;
        // only the non-synced mode needs an inverse velocity update to remain constant on screen.
        if (appliedConfig.syncScrollSpeedWithPlayback) return
        val currentContext = context ?: return
        val currentView = view ?: return
        applyStyle(currentContext, appliedConfig, currentView)
    }

    fun synchronizePlaybackTimeline(
        positionMillis: Long,
        speed: Float,
        isPlaying: Boolean,
        isDiscontinuity: Boolean = false,
    ) {
        playbackTimelineClock.synchronize(
            positionMillis = positionMillis,
            playbackSpeed = speed,
            isPlaying = isPlaying,
            realtimeMillis = SystemClock.elapsedRealtime(),
            allowBackward = isDiscontinuity,
        )
    }

    /**
     * Draws the currently visible danmaku layer into a screenshot canvas.
     *
     * This must be called on the main thread, matching Android View's drawing contract. Controls
     * are not part of this renderer, so the resulting image contains video + danmaku only.
     *
     * The danmaku canvas is only areaRatio × video height and top-aligned in the video, so the
     * layer must be scaled uniformly by the WIDTH ratio and anchored top-left — scaling height
     * independently would stretch the danmaku over the full screenshot.
     */
    fun drawSnapshotOnto(canvas: Canvas, targetWidth: Int, targetHeight: Int): Boolean {
        val currentView = view ?: return false
        if (currentView.visibility != View.VISIBLE || currentView.width <= 0 || currentView.height <= 0) {
            return false
        }
        if (targetWidth <= 0 || targetHeight <= 0) return false
        val checkpoint = canvas.save()
        canvas.scale(
            targetWidth.toFloat() / currentView.width,
            targetWidth.toFloat() / currentView.width,
        )
        currentView.draw(canvas)
        canvas.restoreToCount(checkpoint)
        return true
    }

    fun clear() {
        pendingComments = emptyList()
        renderedItems.clear()
        execute(syncPolicy.onClear())
    }

    fun release() {
        releaseViewResources()
        syncPolicy.onRelease()
        pendingComments = emptyList()
        pendingBindingOffsetMillis = 0L
        renderedItems.clear()
    }

    private fun releaseViewResources() {
        view?.release()
        view = null
        context = null
        renderedItems.clear()
    }

    private fun execute(commands: List<DanmakuRendererCommand>) {
        commands.forEach { command ->
            when (command) {
                DanmakuRendererCommand.ReleaseAttachedView -> releaseViewResources()
                DanmakuRendererCommand.PrepareAttachedView -> Unit // attach configures and prepares it.
                DanmakuRendererCommand.ReplaceItems -> replaceItems()
                DanmakuRendererCommand.ClearItems -> view?.removeAllDanmakus(false)
                is DanmakuRendererCommand.StartAt -> view?.start(command.positionMillis)
                is DanmakuRendererCommand.StartPausedAt -> {
                    val currentView = view
                    if (currentView is PlaybackAwareDanmakuView) {
                        currentView.startPausedAt(command.positionMillis)
                    } else {
                        // Only renderer-owned views are used in production. Keep direct attach()
                        // test fixtures functional without weakening the production guarantee.
                        currentView?.start(command.positionMillis)
                        currentView?.pause()
                    }
                }
                is DanmakuRendererCommand.SeekTo -> view?.seekTo(command.positionMillis)
                is DanmakuRendererCommand.SeekPausedTo -> {
                    val currentView = view
                    if (currentView is PlaybackAwareDanmakuView) {
                        currentView.seekPausedTo(command.positionMillis)
                    } else {
                        currentView?.seekTo(command.positionMillis)
                        currentView?.pause()
                    }
                }
                DanmakuRendererCommand.Pause -> view?.pause()
                DanmakuRendererCommand.Resume -> view?.resume()
            }
        }
    }

    private fun replaceItems() {
        val currentView = view ?: return
        val currentContext = context ?: return
        val baseTextSizePx =
            DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP * currentView.currentScaledDensity()
        currentView.removeAllDanmakus(false)
        renderedItems.clear()
        // 数量抽样与复读合并是最终筛选后的展示变换，重建时间轴时统一应用。
        val displayComments = pendingComments.applyDisplaySampling(
            densityRatio = appliedConfig.densityRatio,
            mergeRepeatWindowMillis = appliedConfig.mergeRepeatWindowMillis,
            blockRulesEnabled = appliedConfig.blockRulesEnabled,
            blockedTextRules = appliedConfig.blockedTextRules,
            blockedRegexRules = appliedConfig.blockedRegexRules,
        )
        val scrollAdmission = if (appliedConfig.preventScrollOcclusion) ScrollDanmakuAdmission() else null
        val scrollSpeed = appliedConfig.toDfmStyle(
            scaledDensity = currentView.currentScaledDensity(),
            density = currentView.resources.displayMetrics.density,
            playbackSpeed = playbackSpeed,
        ).scrollPixelsPerMediaSecond
        displayComments.forEach { comment ->
            val item = if (comment.mode == DanmakuDisplayMode.SCROLL) {
                FixedSpeedScrollDanmaku(currentContext.mDanmakuFactory, scrollAdmission, scrollSpeed)
            } else {
                currentContext.mDanmakuFactory.createDanmaku(comment.toDfmType(), currentContext)
            }
            item
                ?.apply {
                    text = comment.text
                    textColor = comment.colorArgb.takeIf { it != 0 } ?: Color.WHITE
                    textShadowColor = Color.BLACK
                    // Font-size changes are applied through DFM's global text scale. Keeping a
                    // stable base size lets every existing item update without being recreated.
                    textSize = baseTextSizePx
                    padding = 4
                    obj = DanmakuMetadata(
                        provenance = comment.provenance ?: DANDANPLAY_SOURCE_ID,
                    )
                    setTime(comment.timeMillis.coerceAtLeast(0L))
                    renderedItems += RenderedDanmaku(
                        sourceTimeMillis = comment.timeMillis,
                        item = this,
                    )
                    currentView.addDanmaku(this)
                }
        }
        applyTimelineOffset(currentContext, appliedConfig)
    }

    private fun applyDisplayConfig(
        context: DanmakuContext,
        config: DanmakuDisplayConfig,
        view: DanmakuView,
    ) {
        applyStyle(context, config, view)
        applyContentConfig(context, config)
    }

    private fun applyStyle(
        context: DanmakuContext,
        config: DanmakuDisplayConfig,
        view: DanmakuView,
    ) {
        val style = config.toDfmStyle(
            scaledDensity = view.currentScaledDensity(),
            density = view.resources.displayMetrics.density,
            playbackSpeed = playbackSpeed,
        )
        context
            .setScaleTextSize(
                config.fontSizeSp / DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP,
            )
            .setDanmakuMargin(style.marginPx)
        // Anchor all visible comments at the same timestamp before changing their velocity.
        val now = playbackTimelineClock.positionAt(SystemClock.elapsedRealtime())
        renderedItems.forEach { rendered ->
            (rendered.item as? FixedSpeedScrollDanmaku)?.updateSpeed(
                timeMillis = now,
                speed = style.scrollPixelsPerMediaSecond,
            )
        }
        // DFM 的全局透明度直接写绘制 paint 的 alpha，立即生效。
        context.setDanmakuTransparency(config.opacity)
    }

    /**
     * DFM already has live filters and global layout invalidation. Apply configuration to those
     * primitives instead of clearing/re-adding the timeline.
     */
    private fun applyContentConfig(
        context: DanmakuContext,
        config: DanmakuDisplayConfig,
    ) {
        context
            .setR2LDanmakuVisibility(config.showScroll)
            // Defense-mode scrolling items identify as SPECIAL to bypass DFM's row overwriting.
            .setSpecialDanmakuVisibility(config.showScroll)
            .setFTDanmakuVisibility(config.showTop)
            .setFBDanmakuVisibility(config.showBottom)
        provenanceFilter.setData(config.enabledProvenance)
        context.mGlobalFlagValues.updateFilterFlag()
        applyTimelineOffset(context, config)
    }

    private fun applyTimelineOffset(
        context: DanmakuContext,
        config: DanmakuDisplayConfig,
    ) {
        context.mGlobalFlagValues.updateSyncOffsetTimeFlag()
        renderedItems.forEach { rendered ->
            val targetTimeMillis = (
                rendered.sourceTimeMillis +
                    pendingBindingOffsetMillis +
                    config.timeOffsetMillis
            ).coerceAtLeast(0L)
            rendered.item.setTimeOffset(targetTimeMillis - rendered.item.time)
        }
    }

    @Suppress("DEPRECATION")
    private fun DanmakuView.currentScaledDensity(): Float {
        return resources.displayMetrics.scaledDensity
    }

    private fun DanmakuComment.toDfmType(): Int = when (mode) {
        DanmakuDisplayMode.SCROLL -> BaseDanmaku.TYPE_SCROLL_RL
        DanmakuDisplayMode.TOP -> BaseDanmaku.TYPE_FIX_TOP
        DanmakuDisplayMode.BOTTOM -> BaseDanmaku.TYPE_FIX_BOTTOM
    }

    private data class RenderedDanmaku(
        val sourceTimeMillis: Long,
        val item: BaseDanmaku,
    )

    private data class DanmakuMetadata(
        val provenance: String,
    )

    private val provenanceFilter = object : DanmakuFilters.BaseDanmakuFilter<Set<String>>() {
        @Volatile
        private var enabledProvenance: Set<String> = appliedConfig.enabledProvenance

        override fun filter(
            danmaku: BaseDanmaku,
            index: Int,
            totalsize: Int,
            timer: DanmakuTimer?,
            fromCachingTask: Boolean,
            context: DanmakuContext?,
        ): Boolean {
            val provenance = (danmaku.obj as? DanmakuMetadata)?.provenance ?: return false
            return provenance !in enabledProvenance
        }

        override fun setData(data: Set<String>?) {
            enabledProvenance = data?.toSet().orEmpty()
        }

        override fun reset() = Unit
    }

    private class EmptyDanmakuParser : BaseDanmakuParser() {
        override fun parse(): IDanmakus = Danmakus()
    }

    /**
     * DFM 0.9.25's start/seek handlers always enter the running state and remove already queued
     * pause messages. Queueing pause from the caller immediately after start/seek therefore does
     * not preserve a paused player. Posting the pause *onto DFM's own handler* makes it run after
     * the timeline command has completed, without relying on frame delays or a second clock.
     */
    private class PlaybackAwareDanmakuView(
        androidContext: Context,
    ) : DanmakuView(androidContext) {

        fun enablePlayerClock() {
            // Non-blocking mode delegates every DFM timer update to DrawHandler.Callback, whose
            // value comes from the player-backed thread-safe media clock above.
            handler?.enableNonBlockMode(true)
        }

        fun startPausedAt(positionMillis: Long) {
            start(positionMillis)
            pauseAfterTimelineCommand()
        }

        fun seekPausedTo(positionMillis: Long) {
            seekTo(positionMillis)
            pauseAfterTimelineCommand()
        }

        private fun pauseAfterTimelineCommand() {
            handler?.post {
                // pause() resolves the volatile handler again: release() may have detached it
                // while this command was waiting behind start/seek.
                pause()
            }
        }
    }
}

/** Transparent, non-intercepting DFM layer. Compose controls are composed after this layer. */
@Composable
fun DfmDanmakuOverlay(
    renderer: DfmDanmakuRenderer,
    player: EasyPlayerController,
    comments: List<DanmakuComment>,
    bindingOffsetMillis: Long,
    displayConfig: DanmakuDisplayConfig,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val minimumLineHeightPx = with(density) {
        (
            displayConfig.fontSizeSp.sp.toPx() * displayConfig.lineHeightFactor +
                MINIMUM_DANMAKU_LINE_VERTICAL_PADDING.toPx()
        ).roundToInt().coerceAtLeast(1)
    }
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            // 显示区域即弹幕画布：画布顶部对齐视频顶部、高度 = areaRatio × 视频高度。
            // DFM 的滚动轨道与顶部/底部锚点都以画布高度分配，三种类型的弹幕
            // （滚动从顶部排、顶部从顶部堆、底部贴画布底边向上堆）天然被约束在
            // 画布内，与显示类型开关完全正交。竖屏下 10% 可能小于一条弹幕的
            // 实际行高，因此布局只在运行时兜底到一行，不改写用户保存的区域比例。
            .layout { measurable, constraints ->
                if (!constraints.hasBoundedHeight) {
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                } else {
                    val targetHeight = resolveDanmakuCanvasHeightPx(
                        containerHeightPx = constraints.maxHeight,
                        areaRatio = displayConfig.areaRatio,
                        minimumLineHeightPx = minimumLineHeightPx,
                    ).coerceAtLeast(constraints.minHeight)
                    val placeable = measurable.measure(
                        constraints.copy(minHeight = targetHeight, maxHeight = targetHeight),
                    )
                    layout(placeable.width, targetHeight) {
                        placeable.placeRelative(0, 0)
                    }
                }
            },
        factory = { context ->
            renderer.synchronizePlaybackTimeline(
                positionMillis = player.currentPosition,
                speed = player.speed,
                isPlaying = player.isPlaying,
            )
            renderer.getOrCreateView(
                androidContext = context,
                positionMillis = player.currentPosition,
                isPlaying = player.isPlaying,
            ).apply {
                // A configuration change can create the new holder before Compose has detached
                // the old one. The renderer intentionally keeps this native view alive, so move
                // it between holders instead of allocating and asynchronously preparing another.
                (parent as? ViewGroup)?.removeView(this)
            }
        },
        update = { view ->
            renderer.synchronizePlaybackTimeline(
                positionMillis = player.currentPosition,
                speed = player.speed,
                isPlaying = player.isPlaying,
            )
            renderer.attach(view, player.currentPosition, player.isPlaying)
        },
    )
    // Visibility, filtering and timing are discrete actions and apply immediately.
    LaunchedEffect(
        displayConfig.enabled,
        displayConfig.showScroll,
        displayConfig.showTop,
        displayConfig.showBottom,
        displayConfig.enabledProvenance,
        displayConfig.timeOffsetMillis,
        displayConfig.preventScrollOcclusion,
    ) {
        renderer.setDisplayConfig(displayConfig, player.currentPosition)
    }
    // Slider previews can update on every pointer move. Compose cancels the previous effect, so
    // only the latest style snapshot performs the expensive 5k-item rebuild.
    LaunchedEffect(
        displayConfig.fontSizeSp,
        displayConfig.lineHeightFactor,
        displayConfig.scrollSpeed,
        displayConfig.opacity,
        displayConfig.densityRatio,
        displayConfig.mergeRepeatWindowMillis,
        displayConfig.blockRulesEnabled,
        displayConfig.blockedTextRules,
        displayConfig.blockedRegexRules,
        displayConfig.syncScrollSpeedWithPlayback,
    ) {
        delay(STYLE_RECONFIGURE_DEBOUNCE_MILLIS)
        renderer.setDisplayConfig(displayConfig, player.currentPosition)
    }
    LaunchedEffect(comments, bindingOffsetMillis) {
        renderer.setComments(
            comments = comments,
            timeOffsetMillis = bindingOffsetMillis,
            positionMillis = player.currentPosition,
        )
    }
    DisposableEffect(player) {
        val listener = object : EasyPlayerController.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                renderer.synchronizePlaybackTimeline(
                    positionMillis = player.currentPosition,
                    speed = player.speed,
                    isPlaying = isPlaying,
                )
                if (isPlaying) renderer.resume() else renderer.pause()
            }

            override fun onPositionDiscontinuity(positionMs: Long) {
                renderer.synchronizePlaybackTimeline(
                    positionMillis = positionMs,
                    speed = player.speed,
                    isPlaying = player.isPlaying,
                    isDiscontinuity = true,
                )
                renderer.seekTo(positionMs)
            }

            override fun onPlaybackSpeedChanged(speed: Float) {
                renderer.synchronizePlaybackTimeline(
                    positionMillis = player.currentPosition,
                    speed = speed,
                    isPlaying = player.isPlaying,
                )
                renderer.setPlaybackSpeed(speed)
            }
        }
        player.addListener(listener)
        renderer.synchronizePlaybackTimeline(
            positionMillis = player.currentPosition,
            speed = player.speed,
            isPlaying = player.isPlaying,
        )
        renderer.setPlaybackSpeed(player.speed)
        if (player.isPlaying) renderer.resume() else renderer.pause()
        onDispose {
            player.removeListener(listener)
        }
    }
    // Both ExoPlayer and MPV callbacks can be sparse while playing. Periodic main-thread samples
    // correct decoder/buffering drift while the clock interpolates smoothly between samples.
    LaunchedEffect(player, renderer) {
        while (true) {
            renderer.synchronizePlaybackTimeline(
                positionMillis = player.currentPosition,
                speed = player.speed,
                isPlaying = player.isPlaying,
            )
            delay(PLAYBACK_CLOCK_SNAPSHOT_INTERVAL_MILLIS)
        }
    }
}

private const val STYLE_RECONFIGURE_DEBOUNCE_MILLIS = 80L
private const val PLAYBACK_CLOCK_SNAPSHOT_INTERVAL_MILLIS = 250L
private val MINIMUM_DANMAKU_LINE_VERTICAL_PADDING = 8.dp
