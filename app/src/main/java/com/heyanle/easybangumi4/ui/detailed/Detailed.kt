package com.heyanle.easybangumi4.ui.detailed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.bangumi_source_api.api.Source
import com.heyanle.bangumi_source_api.api.entity.CartoonSummary
import com.heyanle.easybangumi4.ui.common.DetailedContainer

/**
 * Created by HeYanLe on 2023/3/4 16:34.
 * https://github.com/heyanLE
 */

@Composable
fun Detailed (
    id: String,
    source: String,
    url: String,
) {

    val summary = CartoonSummary(id, source, url)
    val owner = DetailedController.getViewModelStoreOwner(summary)

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner
    ) {
        DetailedContainer(sourceKey = source) { _, sou, det ->

            val vm = viewModel<DetailedViewModel>(factory = DetailedViewModelFactory(summary, det))
            Detailed(vm = vm, source = sou)
        }
    }

}

@Composable
fun Detailed (
    vm: DetailedViewModel,
    source: Source,
){

}



