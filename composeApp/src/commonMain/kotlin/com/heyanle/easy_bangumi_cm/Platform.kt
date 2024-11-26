package com.heyanle.easy_bangumi_cm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform