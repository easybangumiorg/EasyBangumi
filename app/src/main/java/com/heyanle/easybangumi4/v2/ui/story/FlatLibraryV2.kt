package com.heyanle.easybangumi4.v2.ui.story

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.cartoon.repository.db.dao.CartoonInfoDao
import com.heyanle.easybangumi4.cartoon.story.bound.CartoonEpisodeBinding
import com.heyanle.easybangumi4.cartoon.story.bound.CartoonEpisodeBindingController
import com.heyanle.easybangumi4.cartoon.story.bound.FlatDownloadController
import com.heyanle.easybangumi4.cartoon.story.bound.FlatVideoItem
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonPlayViewModel
import com.heyanle.easybangumi4.ui.common.OkImage
import com.heyanle.easybangumi4.v2.theme.V2Tokens
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

/**
 * 扁平下载目录 tab：罗列 flat_download 内的视频文件。
 * 点击文件展示绑定到该文件的番剧实体并直接跳转本地播放；未绑定文件提供说明与外部打开。
 */
class FlatLibraryViewModel : ViewModel() {

    private val flatDownloadController: FlatDownloadController by Inject.injectLazy()
    private val bindingController: CartoonEpisodeBindingController by Inject.injectLazy()
    private val cartoonInfoDao: CartoonInfoDao by Inject.injectLazy()

    val flatVideos: StateFlow<List<FlatVideoItem>> = flatDownloadController.flatVideos

    /** 文件名 → 绑定该文件的番剧实体列表 */
    val bindingsByFile: StateFlow<Map<String, List<CartoonEpisodeBinding>>> =
        bindingController.bindings.map { list ->
            list.filter { it.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE }
                .groupBy { it.flatFileName }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun refresh() {
        flatDownloadController.refresh()
    }

    fun deleteFile(item: FlatVideoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                UniFile.fromUri(APP, item.uri.toUri())?.delete()
            }
            bindingController.removeBindingsForFlatFile(item.fileName)
            flatDownloadController.refresh()
        }
    }

    /** 若该番历史进度停留在绑定集上，则跳转时从历史进度续播 */
    suspend fun historyProgressFor(binding: CartoonEpisodeBinding): Long {
        return runCatching {
            val info = cartoonInfoDao.getByCartoonSummary(binding.cartoonId, binding.source)
            if (info != null &&
                binding.episodeId.isNotEmpty() &&
                info.lastEpisodeId == binding.episodeId &&
                info.lastProcessTime > 0
            ) {
                info.lastProcessTime
            } else {
                -1L
            }
        }.getOrDefault(-1L)
    }

}

fun openFlatFileExternally(uriString: String) {
    var uri = uriString.toUri()
    if (uri.scheme == "file") {
        val file = File(uri.path ?: "")
        if (file.exists()) {
            uri = FileProvider.getUriForFile(
                APP,
                APP.packageName + ".provider",
                file,
            )
        }
    }
    runCatching {
        APP.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FlatLibraryV2(
    flatViewModel: FlatLibraryViewModel = viewModel(),
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val flatVideos by flatViewModel.flatVideos.collectAsState()
    val bindingsByFile by flatViewModel.bindingsByFile.collectAsState()

    var pendingDelete by remember { mutableStateOf<FlatVideoItem?>(null) }
    var unboundFile by remember { mutableStateOf<FlatVideoItem?>(null) }
    var bindingSheetFile by remember { mutableStateOf<FlatVideoItem?>(null) }

    fun jumpToBinding(binding: CartoonEpisodeBinding) {
        scope.launch {
            val progress = flatViewModel.historyProgressFor(binding)
            navController.navigationDetailed(
                binding.cartoonId,
                binding.source,
                CartoonPlayViewModel.EnterData(
                    playLineId = binding.lineId,
                    playLineLabel = "",
                    playLineIndex = -1,
                    episodeId = binding.episodeId,
                    episodeLabel = binding.episodeLabel,
                    episodeOrder = binding.episodeOrder,
                    episodeIndex = -1,
                    adviceProgress = progress,
                    preferLocal = true,
                ),
            )
        }
    }

    if (flatVideos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "本地缓存暂无视频",
                style = MaterialTheme.typography.titleMedium,
                color = V2Tokens.TextSecondary,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "下载时选择「本地缓存」目的地，视频会出现在这里并自动绑定到对应番剧",
                style = MaterialTheme.typography.bodySmall,
                color = V2Tokens.TextSecondary,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(flatVideos, key = { it.uri }) { item ->
                val boundCount = bindingsByFile[item.fileName]?.size ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val bindings = bindingsByFile[item.fileName].orEmpty()
                            when {
                                bindings.isEmpty() -> unboundFile = item
                                bindings.size == 1 -> jumpToBinding(bindings.first())
                                else -> bindingSheetFile = item
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = V2Tokens.SurfaceMuted,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                            tint = V2Tokens.TextSecondary,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = formatFlatSize(item.size) + " · " +
                                stringResource(R.string.flat_file_bind_count, boundCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = V2Tokens.TextSecondary,
                        )
                    }
                    IconButton(onClick = { pendingDelete = item }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = V2Tokens.TextSecondary,
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(R.string.delete)) },
            text = { Text(text = "将删除文件「${item.fileName}」及其绑定关系，此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    flatViewModel.deleteFile(item)
                    pendingDelete = null
                }) {
                    Text(text = stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    unboundFile?.let { item ->
        AlertDialog(
            onDismissRequest = { unboundFile = null },
            title = { Text(text = stringResource(R.string.flat_unbound_title)) },
            text = { Text(text = stringResource(R.string.flat_unbound_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    openFlatFileExternally(item.uri)
                    unboundFile = null
                }) {
                    Text(text = stringResource(R.string.open_externally))
                }
            },
            dismissButton = {
                TextButton(onClick = { unboundFile = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    bindingSheetFile?.let { item ->
        val bindings = bindingsByFile[item.fileName].orEmpty()
        ModalBottomSheet(
            onDismissRequest = { bindingSheetFile = null },
        ) {
            Text(
                text = stringResource(R.string.bound_cartoon_list),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(horizontal = 12.dp),
            ) {
                items(bindings, key = { "${it.source}_${it.cartoonId}_${it.episodeOrder}" }) { binding ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                bindingSheetFile = null
                                jumpToBinding(binding)
                            }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OkImage(
                            modifier = Modifier
                                .size(width = 42.dp, height = 56.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            image = binding.cartoonCover,
                            contentDescription = binding.cartoonTitle,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = binding.cartoonTitle,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "第${binding.episodeOrder}集 ${binding.episodeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = V2Tokens.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = binding.source.substringAfterLast('.'),
                            style = MaterialTheme.typography.bodySmall,
                            color = V2Tokens.TextSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.size(16.dp))
        }
    }

}

private fun formatFlatSize(size: Long): String {
    return when {
        size >= 1L shl 30 -> String.format("%.2f GB", size.toDouble() / (1L shl 30))
        size >= 1L shl 20 -> String.format("%.1f MB", size.toDouble() / (1L shl 20))
        size >= 1L shl 10 -> String.format("%.1f KB", size.toDouble() / (1L shl 10))
        else -> "$size B"
    }
}
