package com.heyanle.easybangumi4.ui.search_migrate.migrate

import androidx.compose.foundation.layout.Column
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easy_i18n.R
import com.heyanle.easybangumi4.source_api.entity.CartoonSummary

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

    Column {

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
        actions = {},
    )

}