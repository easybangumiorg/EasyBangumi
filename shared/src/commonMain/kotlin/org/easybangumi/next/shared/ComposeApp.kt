package org.easybangumi.next.shared

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.withContext
import okio.buffer
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UFD.Companion.TYPE_ASSETS
import org.easybangumi.next.lib.unifile.UniFileFactory
import org.easybangumi.next.lib.unifile.fromUFD
import org.easybangumi.next.lib.utils.coroutineProvider
import org.easybangumi.next.shared.foundation.LocalUIMode
import org.easybangumi.next.shared.foundation.image.LocalImageLoader
import org.easybangumi.next.shared.foundation.image.createImageLoader
import org.easybangumi.next.shared.scheme.LocalSizeScheme
import org.easybangumi.next.shared.scheme.SizeScheme
import org.easybangumi.next.shared.theme.EasyTheme
import org.easybangumi.next.shared.compose.UI
import org.easybangumi.next.shared.foundation.stringRes
import org.easybangumi.next.shared.resources.Res
import org.easybangumi.next.shared.foundation.snackbar.MoeSnackBar
import org.koin.compose.KoinContext

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
fun ComposeApp() {
    KoinContext() {

        val top = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(LocalDensity.current).toDp()
        }
        CompositionLocalProvider(
            LocalImageLoader provides createImageLoader(),
            LocalUIMode provides UI.getUiMode(),
            LocalSizeScheme provides SizeScheme(statusBarHeight = top)
        ) {
            EasyTheme {
                Router()
                MoeSnackBar()
                NeedKnowDialog()
            }
        }


    }
}

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