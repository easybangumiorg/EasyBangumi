package com.heyanle.easybangumi4.ui.cartoon_play

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.heyanle.easybangumi4.danmaku.DANDANPLAY_SOURCE_ID
import com.heyanle.easybangumi4.danmaku.DANMAKU_AREA_RATIO_TIERS
import com.heyanle.easybangumi4.danmaku.DanmakuComment
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayConfig
import com.heyanle.easybangumi4.danmaku.DanmakuDisplayMode
import com.heyanle.easybangumi4.danmaku.DanmakuRendererCommand
import com.heyanle.easybangumi4.danmaku.DanmakuRendererConfigEffect
import com.heyanle.easybangumi4.danmaku.DanmakuRendererSyncPolicy
import com.heyanle.easybangumi4.danmaku.DfmDanmakuStyle
import com.heyanle.easybangumi4.danmaku.classifyDanmakuConfigChange
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

/** Imperative adapter around DanmakuFlameMaster; the caller owns normalized comment state. */
class DfmDanmakuRenderer {
    private var view: DanmakuView? = null
    private var context: DanmakuContext? = null
    private var pendingComments: List<DanmakuComment> = emptyList()
    private var pendingBindingOffsetMillis: Long = 0L
    private var appliedConfig = DanmakuDisplayConfig.DEFAULT
    private var lastScrollAreaHeight = -1
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
            // 全屏/小窗切换只改变视图高度，不经过任何配置变化路径：
            // 高度变化后按新高度重放显示区域限制，避免行数上限静默失效或过严。
            addOnLayoutChangeListener { changedView, _, _, _, _, _, _, _, _ ->
                val danmakuView = changedView as? DanmakuView ?: return@addOnLayoutChangeListener
                val currentContext = this@DfmDanmakuRenderer.context
                    ?: return@addOnLayoutChangeListener
                if (danmakuView.height > 0 && danmakuView.height != lastScrollAreaHeight) {
                    applyScrollArea(
                        context = currentContext,
                        config = appliedConfig,
                        style = appliedConfig.toDfmStyle(danmakuView.currentScaledDensity()),
                        view = danmakuView,
                    )
                }
            }
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

            override fun updateTimer(timer: DanmakuTimer) = Unit
            override fun danmakuShown(danmaku: BaseDanmaku) = Unit
            override fun drawingFinished() = Unit
        })
        view.prepare(EmptyDanmakuParser(), newContext)
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
                    DanmakuRendererConfigEffect.STYLE -> {
                        // STYLE wins classification when style and content change together.
                        applyDisplayConfig(currentContext, normalized, currentView)
                    }
                }
            }
        }
        appliedConfig = normalized
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

    /**
     * Draws the currently visible danmaku layer into a screenshot canvas.
     *
     * This must be called on the main thread, matching Android View's drawing contract. Controls
     * are not part of this renderer, so the resulting image contains video + danmaku only.
     */
    fun drawSnapshotOnto(canvas: Canvas, targetWidth: Int, targetHeight: Int): Boolean {
        val currentView = view ?: return false
        if (currentView.visibility != View.VISIBLE || currentView.width <= 0 || currentView.height <= 0) {
            return false
        }
        val checkpoint = canvas.save()
        canvas.scale(
            targetWidth.toFloat() / currentView.width,
            targetHeight.toFloat() / currentView.height,
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
        pendingComments.forEach { comment ->
            currentContext.mDanmakuFactory
                .createDanmaku(comment.toDfmType(), currentContext)
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
        val style = config.toDfmStyle(view.currentScaledDensity())
        context
            .setScaleTextSize(
                config.fontSizeSp / DanmakuDisplayConfig.DEFAULT_FONT_SIZE_SP,
            )
            .setDanmakuMargin(style.marginPx)
            .setScrollSpeedFactor(style.scrollDurationFactor)
        // DFM 的全局透明度直接写绘制 paint 的 alpha，立即生效。
        context.setDanmakuTransparency(config.opacity)
        applyScrollArea(context, config, style, view)
    }

    /**
     * 用 DFM 的逐类型最大行数近似 B 站的"显示区域"：把可见高度乘以占比后按轨道高度
     * 反算滚动弹幕的行数上限。顶部/底部固定弹幕不受行数限制，与 B 站行为一致。
     */
    private fun applyScrollArea(
        context: DanmakuContext,
        config: DanmakuDisplayConfig,
        style: DfmDanmakuStyle,
        view: DanmakuView,
    ) {
        if (view.height <= 0) return
        // DFM 的 javadoc 明确 null 才是"取消行数限制"，空 map 会让 filter 常驻。
        val maxLinesPair: Map<Int, Int>? = if (config.areaRatio >= DANMAKU_AREA_RATIO_TIERS.last()) {
            null
        } else {
            val trackHeightPx = (style.textSizePx + style.marginPx).coerceAtLeast(1f)
            val maxLines = ((view.height * config.areaRatio) / trackHeightPx)
                .toInt()
                .coerceAtLeast(1)
            mapOf(BaseDanmaku.TYPE_SCROLL_RL to maxLines)
        }
        context.setMaximumLines(maxLinesPair)
        context.mGlobalFlagValues.updateFilterFlag()
        lastScrollAreaHeight = view.height
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
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
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
        displayConfig.areaRatio,
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
                if (isPlaying) renderer.resume() else renderer.pause()
            }

            override fun onPositionDiscontinuity(positionMs: Long) {
                renderer.seekTo(positionMs)
            }
        }
        player.addListener(listener)
        if (player.isPlaying) renderer.resume() else renderer.pause()
        onDispose {
            player.removeListener(listener)
        }
    }
}

private const val STYLE_RECONFIGURE_DEBOUNCE_MILLIS = 80L
