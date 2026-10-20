package com.heyanle.easybangumi4.sourcelab

import android.os.Build
import com.heyanle.easybangumi4.BuildConfig
import com.heyanle.easybangumi4.ActivityManager
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.case.SourceStateCase
import com.heyanle.easybangumi4.exo.CartoonMediaSourceFactory
import com.heyanle.easybangumi4.plugin.source.bundle.SourceBundle
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LabPhase { CONFIG, RUNNING, REPORT }

data class LabSourceItem(
    val key: String,
    val label: String,
    val version: String,
    val versionCode: Int,
)

data class LabUiState(
    val phase: LabPhase = LabPhase.CONFIG,
    val config: LabConfig = LabConfig(),
    val available: List<LabSourceItem> = emptyList(),
    val bundleReady: Boolean = false,
    val run: LabRun? = null,
    val message: String? = null,
    val verificationRequest: LabVerificationRequest? = null,
    val verificationRequestedAt: Long = 0L,
)

/**
 * 源体检的 ViewModel。
 *
 * 人工验证保持 RUNNING，避免移除真实播放 Surface；离开验收页时结束任务并保留报告。
 */
class SourceLabViewModel(private val userDiagnostics: Boolean = false) : ViewModel() {

    private val sourceStateCase: SourceStateCase by Inject.injectLazy()
    private val mediaSourceFactory: CartoonMediaSourceFactory by Inject.injectLazy()

    val playProbe = SourceLabPlayProbe(mediaSourceFactory)

    private val _state = MutableStateFlow(LabUiState(
        config = if (userDiagnostics) LabConfig(keywords = listOf(LabConfig.DEFAULT_KEYWORDS.first())) else LabConfig(),
    ))
    val state: StateFlow<LabUiState> = _state.asStateFlow()

    private var currentBundle: SourceBundle = SourceBundle.NONE
    private var runJob: Job? = null
    private var verificationHost: SourceLabVerificationHost? = null
    private var runGeneration = 0L
    private var stopGeneration = -1L
    private var initialSelectionPending = !userDiagnostics

    init {
        viewModelScope.launch {
            sourceStateCase.flowBundle().collect { bundle ->
                currentBundle = bundle
                val items = bundle.sources()
                    .filter { !userDiagnostics || it.key != com.heyanle.easybangumi4.cartoon.story.local.source.LocalSource.LOCAL_SOURCE_KEY }
                    .map { LabSourceItem(it.key, it.label, it.version, it.versionCode) }
                    .sortedBy { it.label }
                val initialSelection = initialSelectionPending && items.isNotEmpty()
                if (initialSelection) initialSelectionPending = false
                _state.update { state ->
                    val keys = items.map { it.key }.toSet()
                    // 用户诊断必须主动选源；刷新列表也不能把“全不选”恢复成全选。
                    val selected = if (initialSelection) keys
                        else state.config.selected.intersect(keys)
                    state.copy(available = items, bundleReady = true, config = state.config.copy(selected = selected))
                }
            }
        }
    }

    fun setKeywordText(text: String) {
        val keywords = text
            .split('\n', ',', '，', ';', '；')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        _state.update { it.copy(config = it.config.copy(keywords = keywords)) }
    }

    fun toggleStep(step: LabStep) {
        _state.update { state ->
            val steps = state.config.steps.toMutableSet()
            if (!steps.add(step)) {
                steps.remove(step)
            }
            if (steps.isEmpty()) {
                steps.add(step) // 至少留一段，避免空跑
            }
            state.copy(config = state.config.copy(steps = steps))
        }
    }

    fun toggleSource(key: String) {
        _state.update { state ->
            val selected = state.config.selected.toMutableSet()
            if (!selected.add(key)) {
                selected.remove(key)
            }
            state.copy(config = state.config.copy(selected = selected))
        }
    }

    fun selectAll(select: Boolean) {
        _state.update { state ->
            val keys = if (select) state.available.map { it.key }.toSet() else emptySet()
            state.copy(config = state.config.copy(selected = keys))
        }
    }

    fun start(onlyFailed: Boolean = false) {
        val config = _state.value.config
        val keys = (if (onlyFailed) {
            _state.value.run?.retrySourceKeys().orEmpty().toList()
        } else {
            config.selected.toList()
        }).filter { currentBundle.source(it) != null }
        when {
            keys.isEmpty() -> {
                _state.update { it.copy(message = if (onlyFailed) "上次没有失败项" else "没有选中任何源") }
                return
            }

            currentBundle.empty() -> {
                _state.update { it.copy(message = "源还没加载完，稍后再试") }
                return
            }
        }

        val generation = ++runGeneration
        runJob?.cancel()
        verificationHost?.close()
        val host = SourceLabVerificationHost(
            activityProvider = ActivityManager::getTopActivity,
            manualTimeoutMs = config.manualTimeoutMs,
        )
        verificationHost = host
        val runConfig = config.copy(selected = keys.toSet())
        _state.update {
            it.copy(
                phase = LabPhase.RUNNING,
                message = null,
                verificationRequest = null,
                verificationRequestedAt = 0L,
                run = LabRun(
                    config = runConfig, total = keys.size,
                    evidenceContext = LabEvidenceContext(
                        device = LabDeviceEvidence(
                            manufacturer = Build.MANUFACTURER, model = Build.MODEL,
                            androidVersion = Build.VERSION.RELEASE, apiLevel = Build.VERSION.SDK_INT,
                        ),
                        app = LabAppEvidence(
                            packageName = BuildConfig.APPLICATION_ID, versionName = BuildConfig.VERSION_NAME,
                            versionCode = BuildConfig.VERSION_CODE, buildType = BuildConfig.BUILD_TYPE,
                        ),
                    ),
                ),
            )
        }

        val runBundle = currentBundle
        runJob = viewModelScope.launch {
            val runner = SourceLabRunner(runBundle, playProbe, verificationHandler = host)
            val results = ArrayList<LabSourceResult>(keys.size)
            val pendingJob = launch {
                host.pending.collect { request ->
                    if (generation == runGeneration) _state.update {
                        if (request != null && it.phase != LabPhase.RUNNING) return@update it
                        it.copy(
                            verificationRequest = request,
                            verificationRequestedAt = if (request == null) 0L else System.currentTimeMillis(),
                        )
                    }
                }
            }
            try {
                for (key in keys) {
                    if (stopGeneration == generation) break
                    if (generation == runGeneration) _state.update {
                        it.copy(run = it.run?.copy(
                            runningKey = key,
                            runningStep = LabStep.entries.firstOrNull { step -> step in runConfig.steps },
                        ))
                    }
                    val result = runCatching {
                        runner.runSource(key, runConfig) { step ->
                            if (generation == runGeneration) _state.update { current ->
                                current.copy(run = current.run?.copy(
                                    runningStep = LabStep.entries.firstOrNull { next ->
                                        next.ordinal > step.step.ordinal && next in runConfig.steps
                                    },
                                ))
                            }
                        }
                    }.getOrElse { t ->
                        if (t is CancellationException) throw t
                        LabSourceResult(
                            key = key,
                            label = runBundle.source(key)?.label ?: key,
                            steps = listOf(
                                LabStepResult(LabStep.HOME, LabVerdict.FAIL, 0L, "执行器异常：${t.message}")
                            ),
                        )
                    }
                    results.add(result)
                    if (generation == runGeneration) _state.update {
                        it.copy(run = it.run?.copy(items = results.toList(), runningKey = null, runningStep = null))
                    }
                }
            } catch (cancelled: CancellationException) {
                if (cancelled is LabVerificationCancelledException) {
                    results.add(cancelled.partialResult)
                }
                throw cancelled
            } finally {
                pendingJob.cancel()
                host.close()
                if (generation == runGeneration) _state.update {
                    it.copy(
                        phase = if (it.phase == LabPhase.CONFIG) LabPhase.CONFIG else LabPhase.REPORT,
                        verificationRequest = null,
                        verificationRequestedAt = 0L,
                        run = it.run?.copy(
                            items = results.toList(),
                            runningKey = null,
                            runningStep = null,
                            finishedAt = System.currentTimeMillis(),
                        ),
                    )
                }
            }
        }
    }

    /** 中止：保留已完成的结果，直接进报告。 */
    fun cancel() {
        stopGeneration = runGeneration
        if (verificationHost?.pending?.value != null) {
            // Let the runner finish its manual-cancellation evidence before its job ends.
            verificationHost?.cancel()
        } else {
            runJob?.cancel()
        }
        runJob = null
        _state.update {
            it.copy(
                phase = LabPhase.REPORT,
                verificationRequest = null,
                verificationRequestedAt = 0L,
                run = it.run?.copy(
                    runningKey = null,
                    runningStep = null,
                    finishedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun backToConfig() {
        _state.update { it.copy(phase = LabPhase.CONFIG) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    override fun onCleared() {
        verificationHost?.close()
        runJob?.cancel()
        playProbe.release()
        super.onCleared()
    }
}
