package org.easybangumi.next.shared.ui.main.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.easybangumi.next.shared.foundation.plugin.SourceBundleContainer
import org.easybangumi.next.shared.foundation.view_model.vm

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
fun Home() {

    SourceBundleContainer(Modifier.fillMaxSize()) {
        val viewModel = vm(::HomeViewModel, it)

    }



    Column {

    }

}

@Composable
fun HomeTopAppBar() {}