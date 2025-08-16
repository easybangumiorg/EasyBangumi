package com.heyanle.easybangumi4.ui.extension_push

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.LauncherBus
import com.heyanle.easybangumi4.plugin.extension.ExtensionController
import com.heyanle.easybangumi4.plugin.extension.ExtensionInfo
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushController
import com.heyanle.easybangumi4.plugin.extension.push.ExtensionPushTask
import com.heyanle.easybangumi4.ui.common.moeDialogAlert
import com.heyanle.easybangumi4.ui.common.moeSnackBar
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.inject.core.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * Created by heyanle on 2025/8/16
 * https://github.com/heyanLE
 */
class ExtensionPushV2ViewModel : ViewModel() {



}