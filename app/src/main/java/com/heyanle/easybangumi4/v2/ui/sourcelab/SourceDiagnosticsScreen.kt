package com.heyanle.easybangumi4.v2.ui.sourcelab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.sourcelab.LabPhase
import com.heyanle.easybangumi4.sourcelab.LabSourceResult
import com.heyanle.easybangumi4.sourcelab.LabStep
import com.heyanle.easybangumi4.sourcelab.LabUiState
import com.heyanle.easybangumi4.sourcelab.LabVerdict
import com.heyanle.easybangumi4.sourcelab.SourceLabViewModel
import com.heyanle.easybangumi4.sourcelab.diagnosticLabel
import com.heyanle.easybangumi4.sourcelab.diagnosticStep
import com.heyanle.easybangumi4.sourcelab.toDiagnosticsReport
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.v2.theme.V2Theme
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.easybangumi4.v2.ui.component.V2SecondaryHeader

/** 公共入口仅主动检查用户选择的已启用源；内部验收保留在 SourceLabScreen。 */
@Composable
fun SourceDiagnosticsScreen(onBack: () -> Unit, onManageSources: () -> Unit) {
    val factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SourceLabViewModel(userDiagnostics = true) as T
        }
    }
    val vm: SourceLabViewModel = viewModel(key = "user-source-diagnostics", factory = factory)
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    BackHandler(enabled = state.phase == LabPhase.RUNNING) { vm.cancel() }
    DisposableEffect(vm, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && vm.state.value.phase == LabPhase.RUNNING) vm.cancel()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (vm.state.value.phase == LabPhase.RUNNING) vm.cancel()
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let { message ->
            vm.consumeMessage()
            message.moeSnackBar()
        }
    }

    Column(Modifier.fillMaxSize().background(V2Tokens.WarmBackground)) {
        V2SecondaryHeader(
            title = "源诊断",
            onBack = { if (state.phase == LabPhase.RUNNING) vm.cancel() else onBack() },
            actions = {
                state.run?.takeIf { state.phase == LabPhase.REPORT }?.let { run ->
                    IconButton(onClick = {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                            ?.setPrimaryClip(ClipData.newPlainText("源诊断报告", run.toDiagnosticsReport()))
                        "诊断报告已复制".moeSnackBar()
                    }) {
                        Icon(Icons.Filled.ContentCopy, "复制诊断报告", tint = V2Tokens.TextPrimary)
                    }
                    IconButton(onClick = {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, run.toDiagnosticsReport())
                        }, "分享诊断报告"))
                    }) {
                        Icon(Icons.Filled.Share, "分享诊断报告", tint = V2Tokens.TextPrimary)
                    }
                }
            },
        )
        when (state.phase) {
            LabPhase.CONFIG -> DiagnosticsConfig(vm, state, onManageSources)
            LabPhase.RUNNING -> DiagnosticsProgress(vm, state)
            LabPhase.REPORT -> DiagnosticsResults(vm, state)
        }
    }
}

@Composable
private fun DiagnosticsConfig(vm: SourceLabViewModel, state: LabUiState, onManageSources: () -> Unit) {
    var keywords by rememberSaveable { mutableStateOf(state.config.keywords.joinToString("\n")) }
    Column(Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                DiagnosticHint("选择需要检查的源，依次测试首页、搜索和播放。播放检查会静音加载一个作品的首集，消耗少量流量，不写入观看历史。")
                DiagnosticHint("遇到人机验证时，请手动完成验证。诊断仅在此页面打开时运行，可以随时停止。")
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it; vm.setKeywordText(it) },
                    label = { Text("搜索关键词（可换行填写）") },
                    supportingText = { Text("找到结果后检查第一部；无结果时尝试首页内容。") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("已启用的源 · 已选 ${state.config.selected.size}", color = V2Tokens.TextPrimary,
                        modifier = Modifier.weight(1f), fontSize = 14.sp)
                    TextButton(onClick = { vm.selectAll(state.config.selected.size != state.available.size) }) {
                        Text(if (state.config.selected.size == state.available.size && state.available.isNotEmpty()) "取消全选" else "全选")
                    }
                }
            }
            if (state.available.isEmpty()) {
                item {
                    DiagnosticHint(if (state.bundleReady) "还没有可诊断的源，请先添加并启用你自己的源。" else "正在加载已安装的源…")
                    OutlinedButton(onClick = onManageSources) { Text("打开源管理") }
                }
            }
            items(state.available, key = { it.key }) { source ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { vm.toggleSource(source.key) }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = source.key in state.config.selected, onCheckedChange = { vm.toggleSource(source.key) })
                    Column(Modifier.weight(1f)) {
                        Text(source.label, fontSize = 15.sp, color = V2Tokens.TextPrimary)
                        Text("版本 ${source.version}", fontSize = 12.sp, color = V2Tokens.TextSecondary)
                    }
                }
            }
        }
        Button(
            onClick = { vm.start() },
            enabled = state.bundleReady && state.config.selected.isNotEmpty() && state.config.keywords.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        ) { Text("开始诊断 · ${state.config.selected.size} 个源") }
    }
}

@Composable
private fun DiagnosticsProgress(vm: SourceLabViewModel, state: LabUiState) {
    val run = state.run
    val runningName = state.available.firstOrNull { it.key == run?.runningKey }?.label ?: "准备中"
    Column(Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text("$runningName · ${run?.runningStep?.label ?: "正在整理结果"}",
            modifier = Modifier.padding(top = 12.dp), color = V2Tokens.TextPrimary, fontWeight = FontWeight.SemiBold)
        DiagnosticHint("已完成 ${run?.items?.size ?: 0}/${run?.total ?: 0} 个源")
        if (state.verificationRequest != null) {
            DiagnosticHint("等待手动验证，请在验证窗口中操作；完成后会重试当前请求。")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlaySurfaceHost(vm.playProbe, active = true, modifier = Modifier.clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.width(12.dp))
            DiagnosticHint("播放检查预览\n静音播放，不保存历史", Modifier.weight(1f))
        }
        TextButton(onClick = vm::cancel) { Text("停止诊断，保留已完成结果") }
        HorizontalDivider(color = V2Tokens.Divider)
        LazyColumn(Modifier.weight(1f)) {
            items(run?.items.orEmpty(), key = { it.key }) { DiagnosticResultCard(it) }
        }
    }
}

@Composable
private fun DiagnosticsResults(vm: SourceLabViewModel, state: LabUiState) {
    val run = state.run
    val needsRetry = run?.retrySourceKeys()?.isNotEmpty() == true
    Column(Modifier.fillMaxSize().padding(horizontal = V2Tokens.ScreenHorizontalPadding)) {
        DiagnosticHint("已完成 ${run?.items?.size ?: 0}/${run?.total ?: 0} 个源 · ${run?.passed ?: 0} 个正常")
        DiagnosticHint("结果只代表本次检查的作品和网络状态。没有搜索结果时，可以更换关键词。")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.start(onlyFailed = true) }, enabled = needsRetry, modifier = Modifier.weight(1f)) {
                Text("重试未通过项")
            }
            OutlinedButton(onClick = vm::backToConfig, modifier = Modifier.weight(1f)) { Text("重新选择") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(run?.sortedItems().orEmpty(), key = { it.key }) { DiagnosticResultCard(it) }
        }
    }
}

@Composable
private fun DiagnosticResultCard(result: LabSourceResult) {
    Surface(shape = RoundedCornerShape(12.dp), color = V2Tokens.Surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(result.label, fontWeight = FontWeight.SemiBold, color = V2Tokens.TextPrimary, modifier = Modifier.weight(1f))
                Text(result.verdict.diagnosticLabel(), color = V2Theme.colors.accent, fontSize = 12.sp)
            }
            LabStep.entries.forEach { step ->
                val message = result.diagnosticStep(step)
                Text("${step.label} · ${message.title}", color = V2Tokens.TextPrimary,
                    fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                DiagnosticHint(message.advice)
            }
        }
    }
}

@Composable
private fun DiagnosticHint(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 12.sp, lineHeight = 18.sp, color = V2Tokens.TextSecondary,
        modifier = modifier.padding(vertical = 4.dp))
}
