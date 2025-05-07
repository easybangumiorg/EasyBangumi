package org.easybangumi.next.shared.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import org.easybangumi.next.lib.store.preference.Preference

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
fun <T> Preference<T>.collectAsState() = flow().collectAsState(get())