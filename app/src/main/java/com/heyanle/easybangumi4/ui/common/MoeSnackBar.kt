package com.heyanle.easybangumi4.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heyanle.easybangumi4.setting.SettingPreferences
import com.heyanle.easybangumi4.theme.EasyThemeController
import com.heyanle.easybangumi4.utils.stringRes
import com.heyanle.injekt.core.Injekt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val mainMoeScope = MainScope()

val moeSnackBarQueue = mutableStateListOf<MoeSnackBarData>()

object MoeSnackBar {
    const val LONG = 4000L
    const val SHORT = 2000L
    const val INFINITY = -1L
}

data class MoeSnackBarData constructor(
    val message: MutableState<String>,
    val modifier: Modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp),
    val duration: Long = MoeSnackBar.SHORT,
    val onCancel: ((MoeSnackBarData) -> Unit)? = null,
    val onConfirm: ((MoeSnackBarData) -> Unit)? = null,
    val cancelLabel: String = stringRes(com.heyanle.easy_i18n.R.string.cancel),
    val confirmLabel: String = stringRes(com.heyanle.easy_i18n.R.string.confirm),
    val onDismiss: ((MoeSnackBarData) -> Unit)? = null,
    val animationDuration: Int = 250,
    val show: MutableState<Boolean> = mutableStateOf(false),
    val shouldClear: MutableState<Boolean> = mutableStateOf(false),
    var isCancelPressed: Boolean = false,
    var isConfirmPressed: Boolean = false,
    val content: @Composable ((MoeSnackBarData) -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoeSnackBar(modifier: Modifier = Modifier) {
    val themeController: EasyThemeController by Injekt.injectLazy()
    Column(
        Modifier
            .fillMaxSize()
            .then(modifier)
            .wrapContentHeight(Alignment.Top)
            .verticalScroll(rememberScrollState())
    ) {
        val state = themeController.themeFlow.collectAsState().value
        moeSnackBarQueue.forEach {
            LaunchedEffect(it) {
                it.show.value = true
                if (it.duration > -1L) {
                    delay(it.duration + it.animationDuration.toLong())
                    if (it.show.value && !it.shouldClear.value) it.dismiss()
                }
            }
            AnimatedVisibility(
                it.show.value,
                enter = expandVertically(tween(it.animationDuration)) + fadeIn(tween(it.animationDuration)),
                exit = shrinkVertically(tween(it.animationDuration)) + fadeOut(tween(it.animationDuration))
            ) {
                val dismissState = rememberSwipeToDismissBoxState()
                SwipeToDismissBox(state = dismissState, backgroundContent = {}) {
                    if (it.content != null) it.content.run { this(it) } else {
                        Snackbar(
                            modifier = it.modifier,
                            action = {
                                Row {
                                    it.onConfirm?.run {
                                        TextButton({
                                            it.isConfirmPressed = true
                                            this(it)
                                        }) {
                                            Text(
                                                it.confirmLabel,
                                            )
                                        }
                                    }
                                    it.onCancel?.run {
                                        TextButton({
                                            it.isCancelPressed = true
                                            this(it)
                                        }) { Text(it.cancelLabel) }
                                    }
                                }
                            },
                            actionOnNewLine = it.onConfirm != null
                        ) { Text(it.message.value) }
                    }
                }
            }
        }
        if (moeSnackBarQueue.size > 0)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SnackbarDefaults.color,
                    contentColor = SnackbarDefaults.contentColor,
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    stringRes(com.heyanle.easy_i18n.R.string.info_moesnackbar_swipe),
                    Modifier.padding(8.dp, 0.dp),
                    fontSize = 12.sp
                )
            }
    }
}

fun MoeSnackBarData.dismiss() = apply {
    mainMoeScope.launch {
        onDismiss?.run { this(this@dismiss) }
        show.value = false
        delay(animationDuration.toLong())
        shouldClear.value = true
        clearIfAllClosed()
    }
}

private fun clearIfAllClosed() {
    val shouldClear1 = moeSnackBarQueue.all { it.shouldClear.value }
    if (shouldClear1) moeSnackBarQueue.clear()
}

fun MoeSnackBarData.show() = apply {
    mainMoeScope.launch {
        moeSnackBarQueue += this@show
    }
}

fun Any.moeSnackBar(
    duration: Long = MoeSnackBar.SHORT,
    onCancel: ((MoeSnackBarData) -> Unit)? = null,
    onConfirm: ((MoeSnackBarData) -> Unit)? = null,
    cancelLabel: String = stringRes(com.heyanle.easy_i18n.R.string.cancel),
    confirmLabel: String = stringRes(com.heyanle.easy_i18n.R.string.confirm),
    onDismiss: ((MoeSnackBarData) -> Unit)? = null,
    animationDuration: Int = 250,
    modifier: Modifier = Modifier.padding(8.dp, 8.dp, 8.dp, 0.dp),
    content: @Composable ((MoeSnackBarData) -> Unit)? = null,
) = MoeSnackBarData(
    mutableStateOf(toString()),
    modifier,
    duration,
    onCancel,
    onConfirm,
    cancelLabel,
    confirmLabel,
    onDismiss,
    animationDuration,
    content = content
).apply { show() }