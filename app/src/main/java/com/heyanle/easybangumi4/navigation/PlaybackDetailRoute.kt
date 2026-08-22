package com.heyanle.easybangumi4.navigation

import java.net.URLEncoder

const val PLAYBACK_DETAIL_V2_ROUTE = "detailed"
const val PLAYBACK_DETAIL_LEGACY_ROUTE = "detailed_legacy"

/** 播放详情页实现。默认值集中在此文件，便于发布时验证和紧急回退。 */
enum class PlaybackDetailTarget(val route: String) {
    V2(PLAYBACK_DETAIL_V2_ROUTE),
    Legacy(PLAYBACK_DETAIL_LEGACY_ROUTE),
}

val DEFAULT_PLAYBACK_DETAIL_TARGET: PlaybackDetailTarget = PlaybackDetailTarget.V2

data class PlaybackDetailRouteArguments(
    val id: String,
    val source: String,
    val enterDataJson: String,
)

/**
 * 构建播放详情路由。普通入口不传 [target] 时进入 V2；恢复入口显式传入 Legacy。
 * `enterDataJson` 在此只视为不透明字符串，解析仍由播放页现有 JSON 适配器负责。
 */
fun buildPlaybackDetailRoute(
    id: String,
    source: String,
    enterDataJson: String = "{}",
    target: PlaybackDetailTarget = DEFAULT_PLAYBACK_DETAIL_TARGET,
): String {
    return "${target.route}?source=${encodeRouteArgument(source)}" +
        "&id=${encodeRouteArgument(id)}" +
        "&enter_data=${encodeRouteArgument(enterDataJson)}"
}

/**
 * 收口 Navigation 已解码的三个参数，V2 与 Legacy 目的地必须共用此入口。
 * Navigation 在匹配 URI 时会完成百分号解码，这里不能再次 URL decode，否则 `+` 会被误改为空格。
 */
fun decodePlaybackDetailRouteArguments(
    id: String,
    source: String,
    enterDataJson: String,
): PlaybackDetailRouteArguments {
    return PlaybackDetailRouteArguments(
        id = id,
        source = source,
        enterDataJson = enterDataJson,
    )
}

private fun encodeRouteArgument(value: String): String =
    URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
