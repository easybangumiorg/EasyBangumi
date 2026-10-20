package com.heyanle.easybangumi4.v2.ui.sourcelab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.sourcelab.LabPhase
import com.heyanle.easybangumi4.sourcelab.LabRun
import com.heyanle.easybangumi4.sourcelab.LabSourceItem
import com.heyanle.easybangumi4.sourcelab.LabSourceResult
import com.heyanle.easybangumi4.sourcelab.LabStep
import com.heyanle.easybangumi4.sourcelab.LabStepResult
import com.heyanle.easybangumi4.sourcelab.LabVerdict
import com.heyanle.easybangumi4.sourcelab.LabVerificationRequest
import com.heyanle.easybangumi4.sourcelab.SourceLabPlayProbe
import com.heyanle.easybangumi4.sourcelab.SourceLabViewModel
import com.heyanle.easybangumi4.sourcelab.toMarkdown
import com.heyanle.easybangumi4.sourcelab.toEvidenceJson
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader
import kotlinx.coroutines.delay

private val ColorPass = Color(0xFF1D9E75)
private val ColorSuspect = Color(0xFFEF9F27)
private val ColorFail = Color(0xFFE24B4A)
private val ColorSkip = Color(0xFF888780)

@Composable
fun SourceLabScreen(onBack: () -> Unit) {
    val vm = viewModel<SourceLabViewModel>()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    BackHandler(enabled = state.phase == LabPhase.RUNNING) { vm.cancel() }
    DisposableEffect(vm) {
        onDispose {
            if (vm.state.value.phase == LabPhase.RUNNING) vm.cancel()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            vm.consumeMessage()
            it.moeSnackBar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V2Tokens.WarmBackground),
        ) {
            V2SecondaryHeader(
                title = "源体检",
                onBack = { if (state.phase == LabPhase.RUNNING) vm.cancel() else onBack() },
                actions = {
                    val report = state.run
                    when {
                        state.phase == LabPhase.RUNNING -> Text(
                            text = "${report?.items?.size ?: 0}/${report?.total ?: 0}",
                            color = V2Tokens.TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 6.dp),
                        )

                        state.phase == LabPhase.REPORT && report != null -> {
                            IconButton(onClick = { copyToClipboard(context, report.toEvidenceJson()) }) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "复制验收 JSON",
                                    tint = V2Tokens.TextPrimary,
                                )
                            }
                            IconButton(onClick = { shareText(context, report.toMarkdown()) }) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "分享报告",
                                    tint = V2Tokens.TextPrimary,
                                )
                            }
                        }
                    }
                },
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.phase) {
                    LabPhase.CONFIG -> ConfigContent(
                        vm = vm,
                        available = state.available,
                        keywords = state.config.keywords,
                        steps = state.config.steps,
                        selected = state.config.selected,
                    )

                    LabPhase.RUNNING -> RunningContent(
                        state.run, state.verificationRequest, state.verificationRequestedAt, vm::cancel,
                    )
                    LabPhase.REPORT -> ReportContent(vm, state.run)
                }
            }
        }

        // 播放段必须有真实 Surface，使用小尺寸 TextureView 渲染视频帧。
        PlaySurfaceHost(
            probe = vm.playProbe,
            active = state.phase == LabPhase.RUNNING && LabStep.PLAY in state.config.steps,
        )
    }
}

// ------------------------------------------------------------------ 配置

@Composable
private fun ConfigContent(
    vm: SourceLabViewModel,
    available: List<LabSourceItem>,
    keywords: List<String>,
    steps: Set<LabStep>,
    selected: Set<String>,
) {
    var keywordText by rememberSaveable { mutableStateOf(keywords.joinToString("\n")) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(6.dp))
            Text(
                "按顺序搜索别名，选择第一部；全部为空才尝试首页第一部。逐条线路测试首集，首帧后进度推进至少 3 秒。",
                fontSize = 12.sp,
                color = V2Tokens.TextSecondary,
            )
            Text(
                "遇到验证码会暂停并打开人工验证。完成后重试原请求；仍需实际播放通过。人工等待单独计时，可跳过此源或停止测试。",
                fontSize = 12.sp,
                color = V2Tokens.TextSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text("搜索别名（每行一个，首次有结果即停止）", fontSize = 13.sp, color = V2Tokens.TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = keywordText,
                onValueChange = {
                    keywordText = it
                    vm.setKeywordText(it)
                },
                modifier = Modifier.fillMaxWidth().height(96.dp),
                singleLine = false,
            )
            Spacer(Modifier.height(14.dp))
            Text("测试项", fontSize = 13.sp, color = V2Tokens.TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabStep.values().forEach { step ->
                    LabChip(label = step.label, selected = step in steps, onClick = { vm.toggleStep(step) })
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("测试源（${selected.size}/${available.size}）", fontSize = 13.sp, color = V2Tokens.TextSecondary)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { vm.selectAll(true) }) { Text("全选") }
                TextButton(onClick = { vm.selectAll(false) }) { Text("全不选") }
            }
            Spacer(Modifier.height(4.dp))
        }

        if (available.isEmpty()) {
            Text(
                text = "还没有已加载的源。先到「源管理」里添加/启用番源。",
                fontSize = 13.sp,
                color = V2Tokens.TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = available, key = { it.key }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.toggleSource(item.key) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = item.key in selected, onCheckedChange = { vm.toggleSource(item.key) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, fontSize = 15.sp, color = V2Tokens.TextPrimary)
                            Text(
                                "${item.key} · v${item.version}(${item.versionCode})",
                                fontSize = 11.sp,
                                color = V2Tokens.TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { vm.start() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        ) {
            Text("开始体检")
        }
    }
}

// ------------------------------------------------------------------ 执行

@Composable
private fun RunningContent(
    run: LabRun?,
    verification: LabVerificationRequest?,
    requestedAt: Long,
    onCancel: () -> Unit,
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(verification, requestedAt) {
        while (verification != null) {
            now = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        Spacer(Modifier.height(12.dp))
        if (verification == null) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { (run?.items?.size ?: 0).toFloat() / (run?.total ?: 1).coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = (run?.runningKey ?: "准备中") + " · " + (run?.runningStep?.label ?: "…"),
            fontSize = 14.sp,
            color = V2Tokens.TextPrimary,
        )
        if (verification != null) {
            Text(
                "等待手动验证 · ${verification.sourceLabel} · ${verification.step.label}" +
                    (verification.keyword?.let { " · $it" } ?: "") +
                    (verification.lineLabel?.let { " · $it" } ?: ""),
                fontSize = 14.sp,
                color = ColorSuspect,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "已等待 ${((now - requestedAt).coerceAtLeast(0L)) / 1_000}s，不计测试超时。请在验证窗口中操作，完成后将重试同一请求。",
                fontSize = 12.sp,
                color = V2Tokens.TextSecondary,
            )
        }
        Text(
            "已完成源的人工等待：${run?.items.orEmpty().sumOf { it.manualWaitMs } / 1_000}s",
            fontSize = 12.sp,
            color = V2Tokens.TextSecondary,
            modifier = Modifier.padding(top = 6.dp),
        )
        TextButton(onClick = onCancel) { Text("停止测试，保留报告") }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = V2Tokens.Divider)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = run?.items.orEmpty(), key = { it.key }) { item ->
                ResultRow(item)
            }
        }
    }
}

// ------------------------------------------------------------------ 报告

@Composable
private fun ReportContent(vm: SourceLabViewModel, run: LabRun?) {
    val items = run?.sortedItems().orEmpty()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCell("通过", run?.passed ?: 0, ColorPass, Modifier.weight(1f))
            SummaryCell("可疑", run?.suspect ?: 0, ColorSuspect, Modifier.weight(1f))
            SummaryCell("失败", run?.failed ?: 0, ColorFail, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.start(onlyFailed = true) }, modifier = Modifier.weight(1f)) {
                Text("重测未通过")
            }
            OutlinedButton(onClick = { vm.backToConfig() }, modifier = Modifier.weight(1f)) {
                Text("改配置")
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = V2Tokens.Divider)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items = items, key = { it.key }) { item ->
                ResultRow(item)
            }
        }
    }
}

@Composable
private fun SummaryCell(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$count", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = color)
            Text(label, fontSize = 12.sp, color = V2Tokens.TextSecondary)
        }
    }
}

@Composable
private fun ResultRow(item: LabSourceResult) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerdictBadge(item.verdict)
            Spacer(Modifier.width(8.dp))
            Text(item.label, fontSize = 15.sp, color = V2Tokens.TextPrimary, modifier = Modifier.weight(1f))
            Text("${item.elapsedMs}ms", fontSize = 11.sp, color = V2Tokens.TextSecondary)
        }
        Text(item.key, fontSize = 11.sp, color = V2Tokens.TextSecondary, modifier = Modifier.padding(top = 2.dp))
        if (item.verificationEvents.isNotEmpty()) {
            Text(
                "人工验证 ${item.verificationEvents.size} 次 · 等待 ${item.manualWaitMs / 1_000}s（不计机器耗时）",
                fontSize = 11.sp,
                color = V2Tokens.TextSecondary,
            )
        }
        Text(
            text = when (item.acceptance) {
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.SEARCH_PLAYABLE -> "搜索样本可播，等待准入审核"
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.HOME_PLAYABLE_SEARCH_EMPTY -> "首页样本可播，目标番剧未收录，等待准入审核"
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.HOME_PLAYABLE_SEARCH_UNAVAILABLE -> "首页可播但搜索不可用，需修复或复核"
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.PLAYBACK_FAILED -> "未完成可播放验收"
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.MANUAL_VERIFICATION_REQUIRED -> "需要人工验证后重测"
                com.heyanle.easybangumi4.sourcelab.LabAcceptance.INCOMPLETE -> "测试未完整执行，不能准入"
            },
            fontSize = 11.sp, color = V2Tokens.TextSecondary,
        )
        item.steps.forEach { step -> StepLine(step) }
    }
}

@Composable
private fun StepLine(step: LabStepResult) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(
            text = step.step.label,
            fontSize = 12.sp,
            color = colorOf(step.verdict),
            modifier = Modifier.width(36.dp),
        )
        Text(
            text = step.detail,
            fontSize = 12.sp,
            color = V2Tokens.TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text("${step.elapsedMs}ms", fontSize = 11.sp, color = V2Tokens.TextSecondary)
    }
}

@Composable
private fun VerdictBadge(verdict: LabVerdict) {
    Surface(color = colorOf(verdict).copy(alpha = 0.14f), shape = RoundedCornerShape(6.dp)) {
        Text(
            text = verdict.label,
            fontSize = 11.sp,
            color = colorOf(verdict),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun LabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val accent = V2Theme.colors.accent
    Surface(
        color = if (selected) accent.copy(alpha = 0.16f) else Color.Transparent,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) accent else V2Tokens.Divider),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (selected) accent else V2Tokens.TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

@Composable
internal fun PlaySurfaceHost(probe: SourceLabPlayProbe, active: Boolean, modifier: Modifier = Modifier) {
    if (!active) {
        return
    }
    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                var renderSurface: Surface? = null
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                        renderSurface?.release()
                        renderSurface = Surface(st)
                        probe.attachSurface(renderSurface)
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) = Unit

                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        probe.attachSurface(null)
                        renderSurface?.release()
                        renderSurface = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                }
            }
        },
        modifier = modifier
            .size(160.dp, 90.dp)
            .background(Color.Transparent),
    )
}

private fun colorOf(verdict: LabVerdict): Color = when (verdict) {
    LabVerdict.PASS -> ColorPass
    LabVerdict.SUSPECT -> ColorSuspect
    LabVerdict.FAIL -> ColorFail
    LabVerdict.SKIP -> ColorSkip
}

private fun copyToClipboard(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    manager.setPrimaryClip(ClipData.newPlainText("源体检报告", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "分享源体检报告"))
}
