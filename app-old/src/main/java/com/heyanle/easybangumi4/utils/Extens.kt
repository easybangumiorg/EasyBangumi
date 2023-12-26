package com.heyanle.easybangumi4.utils

/**
 * Created by HeYanLe on 2023/8/13 13:53.
 * https://github.com/heyanLE
 */
val Int.kb get() = this * 1024
val Int.mb get() = this * 1024 * 1024
val Long.mb get() = this * 1024 * 1024
val Int.gb get() = this.toLong() * 1024 * 1024 * 1024
val Double.gb get() = (this * 1024 * 1024 * 1024).toLong()