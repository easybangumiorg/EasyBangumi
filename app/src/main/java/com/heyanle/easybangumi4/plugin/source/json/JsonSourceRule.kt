package com.heyanle.easybangumi4.plugin.source.json

import com.heyanle.easybangumi4.plugin.api.entity.Cartoon
import com.heyanle.easybangumi4.plugin.source.PluginV3

data class JsonSourceRule(
    val type: String = TYPE,
    val key: String = "",
    val label: String = "",
    val versionName: String = "",
    val versionCode: Int = -1,
    val libVersion: Int = -1,
    val cover: String? = null,
    val describe: String? = null,
    val site: SiteRule = SiteRule(),
    val pages: List<PageRule> = emptyList(),
    val search: ListRule? = null,
    val detail: DetailRule? = null,
    val play: PlayRule? = null,
) {
    companion object {
        const val TYPE = "easybangumi-json-source"
    }
}

data class SiteRule(
    val baseUrl: String = "",
    val userAgent: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

data class PageRule(
    val label: String = "",
    val showCover: Boolean = true,
    val list: ListRule = ListRule(),
)

data class ListRule(
    val url: String = "",
    val firstPage: Int = 0,
    val nextPage: PageKeyRule? = null,
    val item: SelectorRule = SelectorRule(),
    val fields: CoverFieldRule = CoverFieldRule(),
)

data class PageKeyRule(
    val selector: SelectorRule? = null,
    val offset: Int? = 1,
)

data class CoverFieldRule(
    val id: SelectorRule = SelectorRule(),
    val title: SelectorRule = SelectorRule(),
    val url: SelectorRule = SelectorRule(),
    val cover: SelectorRule? = null,
    val intro: SelectorRule? = null,
)

data class DetailRule(
    val url: String = "{url}",
    val fields: CartoonFieldRule = CartoonFieldRule(),
    val playLines: PlayLineRule = PlayLineRule(),
)

data class CartoonFieldRule(
    val id: SelectorRule? = null,
    val title: SelectorRule? = null,
    val url: SelectorRule? = null,
    val cover: SelectorRule? = null,
    val intro: SelectorRule? = null,
    val description: SelectorRule? = null,
    val genre: SelectorRule? = null,
    val status: SelectorRule? = null,
    val updateStrategy: Int = Cartoon.UPDATE_STRATEGY_ALWAYS,
)

data class PlayLineRule(
    val line: SelectorRule = SelectorRule(),
    val lineId: SelectorRule? = null,
    val lineLabel: SelectorRule? = null,
    val episode: SelectorRule = SelectorRule(),
    val episodeId: SelectorRule? = null,
    val episodeLabel: SelectorRule? = null,
    val episodeUrl: SelectorRule? = null,
)

data class PlayRule(
    val url: String = "{url}",
    val direct: SelectorRule? = null,
    val renderVideo: Boolean = true,
    val useLegacyParser: Boolean = false,
    val actionJs: String? = null,
    val timeout: Long = 15000L,
)

data class SelectorRule(
    val query: String = "",
    val type: SelectorType = SelectorType.CSS,
    val attr: String? = null,
    val index: Int = 0,
    val regex: String? = null,
    val replacement: String = "$1",
    val default: String? = null,
) {
    fun isBlank(): Boolean = query.isBlank()
}

enum class SelectorType {
    CSS,
    XPATH,
}

fun JsonSourceRule.isSupported(): Boolean {
    return type == JsonSourceRule.TYPE && libVersion in PluginV3.SUPPORTED_LIB_VERSION_RANGE
}
