package com.heyanle.easybangumi4.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.heyanle.easybangumi4.utils.stringRes
import kotlinx.coroutines.launch

// 弹窗队列
val moeDialogQueue = mutableStateListOf<MoeDialogData>()
sealed class MoeDialogData(
    val onDismiss: ((MoeDialogData) -> Unit)? = null,
    val onShow: ((MoeDialogData) -> Unit)? = null,
) {

    class AlertDialog(
        val text: @Composable ((MoeDialogData) -> Unit)? = null,
        val title: @Composable ((MoeDialogData) -> Unit)? = null,
        val modifier: Modifier = Modifier,
        val onDismissBtn: ((MoeDialogData) -> Unit)? = null,
        val onConfirmBtn: ((MoeDialogData) -> Unit)? = null,
        val dismissLabel: String = stringRes(com.heyanle.easy_i18n.R.string.cancel),
        val confirmLabel: String = stringRes(com.heyanle.easy_i18n.R.string.confirm),
        val properties: DialogProperties = DialogProperties(),
        onDismiss: ((MoeDialogData) -> Unit)? = null,
        onShow: ((MoeDialogData) -> Unit)? = null,
    ): MoeDialogData(onDismiss, onShow)

    class BaseDialog(
        val modifier: Modifier = Modifier,
        val properties: DialogProperties = DialogProperties(),
        onDismiss: ((MoeDialogData) -> Unit)? = null,
        onShow: ((MoeDialogData) -> Unit)? = null,
        val content: @Composable ((MoeDialogData) -> Unit),
    ) : MoeDialogData(onDismiss, onShow)

    class Compose(
        onDismiss: ((MoeDialogData) -> Unit)? = null,
        onShow: ((MoeDialogData) -> Unit)? = null,
        val compose: @Composable ((MoeDialogData) -> Unit)
    ): MoeDialogData(onDismiss, onShow)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoeDialogHost() {
    moeDialogQueue.forEach {
        when (it) {
            is MoeDialogData.AlertDialog -> {
                AlertDialog(
                    modifier = it.modifier,
                    onDismissRequest = { it.dismiss() },
                    properties = it.properties,
                    title = {
                        it.title?.invoke(it)
                    },
                    text = {
                        it.text?.invoke(it)
                    },
                    confirmButton = {
                        if (it.onConfirmBtn != null) {
                            TextButton(onClick = {
                                it.onConfirmBtn.invoke(it)
                                it.dismiss()
                            }) {
                                Text(text = it.confirmLabel)
                            }
                        }
                    },
                    dismissButton = {
                        it.onDismissBtn?.run {
                            TextButton(onClick = {
                                it.onDismissBtn.invoke(it)
                                it.dismiss()
                            }) {
                                Text(text = it.dismissLabel)
                            }
                        }
                    }
                )
            }
            is MoeDialogData.BaseDialog -> {
                BasicAlertDialog(
                    onDismissRequest = { it.dismiss() },
                    properties = it.properties,
                    modifier = it.modifier,
                    content = { it.content.invoke(it) }
                )
            }
            is MoeDialogData.Compose -> {
                it.compose(it)
            }
            else -> {}
        }
    }
}

fun MoeDialogData.dismiss() = apply {
    mainMoeScope.launch {
        moeDialogQueue.remove(this@dismiss)
        this@dismiss.onDismiss?.invoke(this@dismiss)
    }
}

fun MoeDialogData.show() = apply {
    mainMoeScope.launch {
        moeDialogQueue.add(this@show)
        this@show.onShow?.invoke(this@show)
    }
}

fun String.moeDialogAlert(
    title: String? = null,
    onConfirm: ((MoeDialogData) -> Unit)? = null,
    onDismiss: ((MoeDialogData) -> Unit)? = null,
    confirmLabel: String = stringRes(com.heyanle.easy_i18n.R.string.confirm),
    dismissLabel: String = stringRes(com.heyanle.easy_i18n.R.string.cancel),
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
) = MoeDialogData.AlertDialog(
    { Text(text = this) },
    { title?.let { Text(text = it) } },
    modifier,
    onDismiss,
    onConfirm,
    dismissLabel,
    confirmLabel,
    properties,
).apply { show() }


fun Any?.dialog() = this.toString().moeDialogAlert()