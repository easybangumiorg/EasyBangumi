package com.heyanle.easybangumi.ui.anim

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.heyanle.easybangumi.ANIM
import com.heyanle.easybangumi.ANIM_PLAY
import com.heyanle.easybangumi.LocalNavController
import com.heyanle.easybangumi.R
import com.heyanle.easybangumi.SEARCH
import com.heyanle.easybangumi.ui.LoadingPage
import com.heyanle.easybangumi.ui.common.HomeTopAppBar
import com.heyanle.easybangumi.ui.setting.ThemeSettingCard
import kotlinx.coroutines.launch

/**
 * Created by HeYanLe on 2023/1/7 21:52.
 * https://github.com/heyanLE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimPage(
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            HomeTopAppBar(
                scrollBehavior = scrollBehavior,
                label = {
                    Text(text = stringResource(id = R.string.anim_title))
                },
                isShowSearch = true,
                onSearch = {
                    scope.launch {
                        nav.navigate("${SEARCH}/${ANIM}")
                    }

                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
            ) {

            }

        }
    )

}