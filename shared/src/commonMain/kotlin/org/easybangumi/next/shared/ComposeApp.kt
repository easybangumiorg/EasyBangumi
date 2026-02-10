package org.easybangumi.next.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.withContext
import okio.buffer
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UFD.Companion.TYPE_ASSETS
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */
@Composable
expect fun ComposeApp()

private val needKnowString = mutableStateOf("")

@Composable
fun NeedKnowDialog() {
    LaunchedEffect(Unit) {
        withContext(coroutineProvider.io()) {
            val ufd = UFD(TYPE_ASSETS, "NEED_KNOW.txt")
            val uniFile = UniFileFactory.fromUFD(ufd)
            needKnowString.value = uniFile?.openSource()?.buffer()?.readUtf8() ?: ""
        }

    }

    if (needKnowString.value.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                needKnowString.value = ""
            },
            text = {
                Text(needKnowString.value, modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        needKnowString.value = ""
                    }
                ) {
                    Text(text = stringRes(Res.strings.confirm))
                }
            }
        )
    }
}