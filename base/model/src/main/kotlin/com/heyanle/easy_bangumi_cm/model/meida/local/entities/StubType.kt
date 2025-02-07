package com.heyanle.easy_bangumi_cm.model.meida.local.entities

enum class StubType {
    UNKNOWN,
    DVD,
    HDDVD,
    BLURAY,
    VHS,
    TV,
    WEB,
}

// 存档类型规则，用来描述视频原始的存档类型，如DVD、BD、WEB等
data class StubTypeRule(
    val type: StubType,
    val token: String,
)
