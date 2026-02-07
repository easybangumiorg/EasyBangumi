package org.easybangumi.next.panic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import org.easybangumi.next.platformInformation
import org.easybangumi.next.shared.ComposeApp
import java.io.PrintWriter
import java.io.StringWriter


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
fun ApplicationScope.PanicApp(
    throwable: Throwable
) {
    val errorMsg = remember(throwable) {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        stringWriter.toString()
    }
    Window(
    onCloseRequest = ::exitApplication,
    title = "EasyBangumi.next.error",
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) {
                Text("哎呀，怎么全错了!")
                Text("操作系统：${platformInformation.platformName}")
                Text("软件版本：${platformInformation.versionName}")
                Text("软件版本：${platformInformation.versionCode}")
                Text("错误日志：（请截图发给作者）")
                Text(errorMsg)
            }
        }

    }

}