package com.heyanle.easy_bangumi_cm.common.compose.plugin

import androidx.compose.runtime.Composable
import com.heyanle.easy_bangumi_cm.plugin.api.component.media.home.HomePage

/**
 * Created by heyanlin on 2025/3/3.
 */
@Composable
fun HomeComponentPage(
    page: List<HomePage>
) {



}

@Composable
fun HomeComponentGroupPage(
    group: HomePage.Group,
) {

}

@Composable
fun HomeComponentSinglePage(
    page: HomePage.SingleCartoonPage
) {}