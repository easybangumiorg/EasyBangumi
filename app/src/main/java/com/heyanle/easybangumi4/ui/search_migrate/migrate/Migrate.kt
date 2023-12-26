package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.source.LocalSourceBundleController
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.common.OkImage

/**
 * Created by heyanle on 2023/12/23.
 * https://github.com/heyanLE
 */
@Composable
fun Migrate(
    summaries: List<CartoonSummary>,
    sources: List<String>,
) {

    val vm = viewModel<MigrateViewModel>(factory = MigrateViewModelFactory(summaries, sources))
    val state = vm.infoListFlow.collectAsState()
    val sta = state.value

    Column {
        if (sta.selection.isEmpty()) {

        } else {

        }
    }


}


@Composable
fun MigrateContent(

) {

}

@Composable
fun MigrateItem(
    cartoonInfo: CartoonInfo,
    isSelect: Boolean,
    itemVM: MigrateItemViewModel,
    onChangeCover: (CartoonInfo) -> Unit,
    onClick: (CartoonInfo) -> Unit,
    onLongPress: (CartoonInfo) -> Unit,
    onMigrateNow: (CartoonInfo) -> Unit,
    onDelete: (CartoonInfo) -> Unit,
) {
    val state = itemVM.flow.collectAsState()
    val sta = state

    val bundle = LocalSourceBundleController.current

    Row(
        modifier = Modifier
            .height(250.dp)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val process = if (cartoonInfo.lastHistoryTime != 0L) {
            cartoonInfo.matchHistoryEpisode?.let { last ->
                val index = last.first.sortedEpisodeList.indexOf(last.second)
                "${index + 1}/${last.first.sortedEpisodeList.size}"
            }
        } else ""
        MigrateItemCover(
            name = cartoonInfo.name,
            cover = cartoonInfo.coverUrl,
            source = bundle.source(cartoonInfo.source)?.label ?: cartoonInfo.sourceName,
            process = process ?: "",
            isSelect = isSelect
        )


        if(sta.value.isLoadingCover || sta.value.isLoadingPlay){

        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = stringResource(id = R.string.cartoon_migrate)
        )




    }

}

@Composable
fun MigrateItemCover(
    name: String,
    cover: String,
    source: String,
    process: String,
    isSelect: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(19 / 27F)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            OkImage(
                modifier = Modifier.fillMaxSize(),
                image = cover,
                contentDescription = name,
                errorRes = com.heyanle.easybangumi4.R.drawable.placeholder,
            )
            Text(
                fontSize = 13.sp,
                text = source,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(0.dp, 4.dp, 0.dp, 0.dp)
                    )
                    .padding(4.dp, 0.dp)
            )
            if (process.isNotEmpty()) {
                Text(
                    fontSize = 13.sp,
                    text = process,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(0.dp, 0.dp, 0.dp, 4.dp)
                        )
                        .padding(4.dp, 0.dp)
                )
            }
        }


        Spacer(modifier = Modifier.size(4.dp))
        Text(
            style = MaterialTheme.typography.bodySmall,
            text = name,
            maxLines = 2,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelect) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
        )
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    count: Int,
    onExit: () -> Unit,
    onHelp: () -> Unit,
) {

    TopAppBar(
        title = {
            Row {
                Text(text = stringResource(id = R.string.cartoon_migrate))
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(text = if (count <= 999) "$count" else "999+")
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onExit()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack, stringResource(id = R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                onHelp()
            }) {
                Icon(
                    imageVector = Icons.Filled.Help, stringResource(id = R.string.help)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )

}