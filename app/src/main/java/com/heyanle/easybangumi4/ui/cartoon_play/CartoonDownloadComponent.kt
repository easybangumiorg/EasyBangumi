package com.heyanle.easybangumi4.ui.cartoon_play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.navigationDetailed
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.ui.cartoon_play.view_model.CartoonDownloadReqModel
import com.heyanle.easybangumi4.ui.common.CartoonCardWithCover
import com.heyanle.easybangumi4.ui.common.LoadingPage
import com.heyanle.easybangumi4.ui.common.page.list.SourceListGroupTab
import com.heyanle.easybangumi4.ui.common.page.list.listPageWithCover
import com.heyanle.easybangumi4.ui.common.pagingCommon
import com.heyanle.easybangumi4.ui.main.home.HomeBottomSheet

/**
 * Created by heyanle on 2024/7/8.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartoonDownloadDialog(
    cartoonInfo: CartoonInfo,
    playerLineWrapper: PlayLineWrapper,
    episodes: List<Episode>,
    onDismissRequest: () -> Unit,
) {


    val model = remember {
        CartoonDownloadReqModel(
            cartoonInfo = cartoonInfo,
            playerLineWrapper = playerLineWrapper,
            episodes = episodes
        )
    }

    val state = model.state.collectAsState()
    val sta = state.value
    val canReq = model.canReq.collectAsState()
    val cr = canReq.value

    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        IconButton(onClick = {
            onDismissRequest()
        }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "back")
        }


        if (sta.loading) {
            LoadingPage(
                modifier = Modifier.fillMaxSize()
            )
        } else if (sta.cartoonLocalItem == null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                OutlinedTextField(value = sta.keyword, onValueChange = {
                    model.changeKeyword(it)
                })
                LazyVerticalGrid(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    columns = GridCells.Adaptive(100.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(4.dp, 4.dp, 4.dp, 88.dp)
                ) {
                    items(sta.allLocalItem) { local ->
                        CartoonCardWithCover(cartoonCover = local.cartoonCover, onClick = {
                            model.setLocalItem(local)
                        })
                    }


                }

                TextButton(onClick = {
                    model.createNewLocal(cartoonInfo.name)
                }) {
                    Text(text = "dd")
                }
            }
        } else {

        }
    }
}