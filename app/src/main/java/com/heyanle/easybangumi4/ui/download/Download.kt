package com.heyanle.easybangumi4.ui.download

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.LocalNavController
import com.heyanle.easybangumi4.download.entity.DownloadItem
import com.heyanle.easybangumi4.ui.common.OkImage

/**
 * Created by heyanlin on 2023/8/9.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Download() {
    val nav = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val vm = viewModel<DownloadingViewModel>()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column {
            TopAppBar(
                title = { Text(stringResource(R.string.download_history)) },
                navigationIcon = {
                    IconButton(onClick = {
                        nav.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
            val list by vm.flow.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                items(list) {
                    DownloadItem(it, vm) {
                        vm.click(it)
                    }
                }
            }
        }
    }

}

@Composable
fun DownloadItem(
    downloadItem: DownloadItem,
    downloadingViewModel: DownloadingViewModel,
    onClick: (DownloadItem) -> Unit,
) {
    val info = downloadingViewModel.info(downloadItem)
    Row(
        modifier = Modifier
            .padding(8.dp, 4.dp)
            .height(IntrinsicSize.Min)
            .clickable {
                onClick(downloadItem)
            }
    ) {
        OkImage(
            modifier = Modifier
                .width(95.dp)
                .aspectRatio(19 / 13.5F)
                .clip(RoundedCornerShape(4.dp)),
            image = downloadItem.cartoonCover,
            contentDescription = downloadItem.cartoonTitle
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier,
                text = (downloadItem.cartoonTitle),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier,
                text = "${downloadItem.episodeLabel}-${downloadItem.playLine.label}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(info.status.value)
                Text(info.subStatus.value)
            }
            if (info.process.value == -1f) {
                LinearProgressIndicator()
            } else {
                LinearProgressIndicator(info.process.value)
            }

        }
    }
}