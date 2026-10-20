package com.heyanle.easybangumi4.sourcelab

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import coil.load
import coil.request.Disposable
import com.heyanle.easybangumi4.plugin.api.component.VerificationParam
import com.heyanle.easybangumi4.plugin.api.component.VerificationResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference
import java.lang.AutoCloseable
import kotlin.coroutines.coroutineContext

/**
 * 手机人工接力入口，也供 ADB 验收程序复用。使用独立 Dialog，不切换导航或移除播放 Surface。
 * WebView 始终沿用源创建的实例和 Cookie；完成只表示可以重试原请求，不表示验证码或播放通过。
 */
class SourceLabVerificationHost(
    private val activityProvider: () -> Activity?,
    private val manualTimeoutMs: Long = 600_000L,
) : LabVerificationHandler, AutoCloseable {
    init {
        require(manualTimeoutMs > 0L)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private val _pending = MutableStateFlow<LabVerificationRequest?>(null)
    val pending: StateFlow<LabVerificationRequest?> = _pending.asStateFlow()

    @Volatile private var closed = false
    private var active: Session? = null

    override suspend fun verify(request: LabVerificationRequest): LabVerificationResponse =
        withContext(Dispatchers.Main.immediate) {
            mutex.withLock {
                if (closed) {
                    closeUnclaimedWebView(request)
                    return@withLock LabVerificationResponse.Cancelled
                }
                val activity = activityProvider()?.takeUnless {
                    it.isFinishing || it.isDestroyed ||
                        (it is LifecycleOwner && !it.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
                }
                if (activity == null) {
                    closeUnclaimedWebView(request)
                    return@withLock LabVerificationResponse.Unavailable
                }

                val session = Session(activity, request)
                active = session
                _pending.value = request
                var response: LabVerificationResponse? = null
                try {
                    if (!session.show()) return@withLock LabVerificationResponse.Unavailable
                    val answer = withTimeoutOrNull(manualTimeoutMs) { session.answer.await() }
                        ?: LabVerificationResponse.Expired
                    coroutineContext.ensureActive()
                    response = answer
                    answer
                } finally {
                    // Caller cancellation must release the window even while its coroutine is cancelled.
                    withContext(NonCancellable) {
                        session.dispose(returnToSource = response is LabVerificationResponse.Completed)
                        if (active === session) {
                            active = null
                            _pending.value = null
                        }
                    }
                }
            }
        }

    /** Stop this wait; the runner turns Cancelled into batch cancellation and retains completed results. */
    fun cancel() {
        scope.launch { active?.answer?.complete(LabVerificationResponse.Cancelled) }
    }

    override fun close() {
        closed = true
        scope.launch {
            active?.answer?.complete(LabVerificationResponse.Cancelled)
            scope.cancel()
        }
    }

    private fun closeUnclaimedWebView(request: LabVerificationRequest) {
        (request.verificationParam as? VerificationParam.WebView)?.iWebProxy?.let { proxy ->
            runCatching {
                proxy.getWebView()?.let { view -> (view.parent as? ViewGroup)?.removeView(view) }
                proxy.close()
            }
        }
    }

    private inner class Session(
        private val activity: Activity,
        private val request: LabVerificationRequest,
    ) : Application.ActivityLifecycleCallbacks {
        val answer = CompletableDeferred<LabVerificationResponse>()
        private val dialog = Dialog(activity)
        private var webView: WebView? = null
        private var originalParent: WeakReference<ViewGroup>? = null
        private var originalIndex = -1
        private var originalParams: ViewGroup.LayoutParams? = null
        private var originalVisibility = View.VISIBLE
        private var input: EditText? = null
        private var image: ImageView? = null
        private var imageRequest: Disposable? = null
        private var timer: kotlinx.coroutines.Job? = null
        private var registered = false
        private var disposing = false

        fun show(): Boolean = runCatching {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val root = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.WHITE)
            }
            val header = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(4), dp(12), dp(4))
            }
            header.addView(text("${request.sourceLabel} · ${request.step.label} · 人工验证", 14f).apply {
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            })
            val location = buildString {
                request.keyword?.let { append("关键词：").append(it) }
                request.lineLabel?.let {
                    if (isNotEmpty()) append(" · ")
                    append("线路 ").append((request.lineIndex ?: 0) + 1).append("：").append(it)
                }
                request.episodeLabel?.let { append(" · ").append(it) }
            }
            if (location.isNotBlank()) header.addView(text(location, 12f).apply {
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            })
            val status = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val elapsed = text("人工等待不计测试超时", 11f)
            status.addView(elapsed, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            val detailsToggle = text("展开说明", 12f).apply {
                gravity = Gravity.CENTER
                minHeight = dp(32)
                setPadding(dp(8), 0, 0, 0)
            }
            status.addView(detailsToggle)
            header.addView(status)
            root.addView(header)

            // The original WebView fills this area. Expanded instructions overlay it instead of
            // reducing the site's viewport; the user can collapse them before solving a challenge.
            val content = FrameLayout(activity)
            root.addView(content, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            val beganAt = SystemClock.elapsedRealtime()
            timer = scope.launch {
                while (true) {
                    elapsed.text = "第 ${request.attempt} 次 · 等待 " +
                        "${(SystemClock.elapsedRealtime() - beganAt) / 1_000}/${manualTimeoutMs / 1_000}s · 不计测试超时"
                    delay(1_000L)
                }
            }

            when (val param = request.verificationParam) {
                is VerificationParam.WebView -> {
                    val view = param.iWebProxy.getWebView() ?: return@runCatching false
                    webView = view
                    val parent = view.parent as? ViewGroup
                    originalParent = parent?.let(::WeakReference)
                    originalIndex = parent?.indexOfChild(view) ?: -1
                    originalParams = view.layoutParams
                    originalVisibility = view.visibility
                    parent?.removeView(view)
                    content.addView(view, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                    ))
                    view.visibility = View.VISIBLE
                }

                is VerificationParam.ImageCaptcha -> {
                    val imageContent = LinearLayout(activity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(12), 0, dp(12), 0)
                    }
                    content.addView(imageContent, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                    ))
                    param.title?.takeIf(String::isNotBlank)?.let { imageContent.addView(text(it, 17f)) }
                    param.text?.takeIf(String::isNotBlank)?.let { imageContent.addView(text(it, 14f)) }
                    image = ImageView(activity).apply {
                        contentDescription = "待人工识别的验证码图片"
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        imageContent.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
                        imageRequest = load(param.image)
                    }
                    input = EditText(activity).apply {
                        hint = param.hint ?: "输入验证码"
                        contentDescription = "人工验证码答案"
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                        isSaveEnabled = false
                        setSingleLine()
                        root.addView(this, LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            leftMargin = dp(12)
                            rightMargin = dp(12)
                        })
                    }
                }
            }

            val details = ScrollView(activity).apply {
                visibility = View.GONE
                setBackgroundColor(Color.WHITE)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                addView(text(buildString {
                    append(request.sourceLabel).append(" · ").append(request.step.label)
                    if (location.isNotBlank()) append('\n').append(location)
                    append("\n第 ${request.attempt} 次验证；完成后重试同一请求，仍需实际播放通过。")
                    (request.verificationParam as? VerificationParam.WebView)?.tips
                        ?.takeIf(String::isNotBlank)?.let { append('\n').append(it) }
                }, 13f))
            }
            content.addView(details, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP,
            ))
            detailsToggle.setOnClickListener {
                val expanded = details.visibility != View.VISIBLE
                details.visibility = if (expanded) View.VISIBLE else View.GONE
                detailsToggle.text = if (expanded) "收起说明" else "展开说明"
            }

            val actions = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(4), 0, dp(4), 0)
            }
            actions.addView(button("验证完成，重试") {
                val result = when (val param = request.verificationParam) {
                    is VerificationParam.WebView -> VerificationResult.WebView(param.iWebProxy)
                    is VerificationParam.ImageCaptcha -> {
                        val value = input?.text?.toString().orEmpty()
                        if (value.isBlank()) {
                            input?.error = "请输入验证码"
                            return@button
                        }
                        VerificationResult.ImageCaptcha(value)
                    }
                }
                answer.complete(LabVerificationResponse.Completed(result))
            }, LinearLayout.LayoutParams(0, dp(56), 1.4f))
            actions.addView(button("跳过此源") { answer.complete(LabVerificationResponse.Skipped) },
                LinearLayout.LayoutParams(0, dp(56), 1f))
            actions.addView(button("停止测试") { answer.complete(LabVerificationResponse.Cancelled) },
                LinearLayout.LayoutParams(0, dp(56), 1f))
            root.addView(actions)

            dialog.setContentView(root)
            dialog.window?.apply {
                // A plain background removes the floating dialog drawable's default outer insets.
                setBackgroundDrawable(ColorDrawable(Color.WHITE))
                decorView.setPadding(0, 0, 0, 0)
                WindowCompat.setDecorFitsSystemWindows(this, false)
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
                // Keep the controls above both navigation gestures and the on-screen keyboard.
                view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
                insets
            }
            dialog.setOnCancelListener { answer.complete(LabVerificationResponse.Skipped) }
            dialog.setOnDismissListener {
                if (!disposing) answer.complete(LabVerificationResponse.Skipped)
            }
            dialog.setCanceledOnTouchOutside(true)
            activity.application.registerActivityLifecycleCallbacks(this)
            registered = true
            dialog.show()
            dialog.window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            ViewCompat.requestApplyInsets(root)
            true
        }.getOrDefault(false)

        fun dispose(returnToSource: Boolean) {
            if (disposing) return
            disposing = true
            timer?.cancel()
            if (registered) activity.application.unregisterActivityLifecycleCallbacks(this)
            imageRequest?.dispose()
            image?.setImageDrawable(null)
            input?.text?.clear()
            webView?.let { view ->
                (view.parent as? ViewGroup)?.removeView(view)
                val parent = originalParent?.get()
                if (returnToSource && parent != null && parent.isAttachedToWindow && !activity.isDestroyed) {
                    val index = originalIndex.coerceIn(0, parent.childCount)
                    runCatching {
                        parent.addView(view, index, originalParams)
                        view.visibility = originalVisibility
                    }
                }
            }
            dialog.setOnDismissListener(null)
            dialog.setOnCancelListener(null)
            runCatching { dialog.dismiss() }
            if (!returnToSource) closeUnclaimedWebView(request)
            webView = null
            originalParent = null
            originalParams = null
        }

        private fun text(value: String, size: Float) = TextView(activity).apply {
            text = value
            textSize = size
            setTextColor(Color.BLACK)
        }

        private fun button(label: String, onClick: () -> Unit) = Button(activity).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            minWidth = 0
            minHeight = dp(48)
            minimumHeight = dp(48)
            maxLines = 2
            gravity = Gravity.CENTER
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener { if (!answer.isCompleted) onClick() }
        }

        private fun dp(value: Int) = (value * activity.resources.displayMetrics.density).toInt()

        override fun onActivityStopped(activity: Activity) {
            if (activity === this.activity) answer.complete(LabVerificationResponse.Unavailable)
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity === this.activity) answer.complete(LabVerificationResponse.Unavailable)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    }
}
