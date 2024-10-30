package com.heyanle.easybangumi4.ui.extension_push.from_url

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heyanle.easybangumi4.ui.extension_push.ExtensionPushType
import com.heyanle.easybangumi4.ui.extension_push.ExtensionPushViewModel

/**
 * Created by heyanlin on 2024/10/29.
 */
object ExtensionPushFromUrl : ExtensionPushType {

    @Composable
    fun vm(): ExtensionFromUrlViewModel {
        return viewModel<ExtensionFromUrlViewModel>()
    }

    @Composable
    override fun label(): String {
        return stringResource(com.heyanle.easy_i18n.R.string.js_file_url)
    }

    override fun LazyListScope.content() {
        item {

        }
    }

    @Composable
    override fun OutContent() {

    }

    override fun onPush(viewModel: ExtensionPushViewModel) {

    }
}
