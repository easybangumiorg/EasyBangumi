package org.easybangumi.next.shared.plugin.api.component.filter

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
 *
 */
sealed class Filter {

    abstract val id: String

    data class SelectionFilter(
        override val id: String,
        val title: String,
        val options: List<String>,
        val selected: List<String>,
    ): Filter()

    data class RadioFilter(
        override val id: String,
        val title: String,
        val options: List<String>,
        val selected: String,
    ): Filter()

    data class SwitchFilter(
        override val id: String,
        val title: String,
        val selected: Boolean,
    ): Filter()

}