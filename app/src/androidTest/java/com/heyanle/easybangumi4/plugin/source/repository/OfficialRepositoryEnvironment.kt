package com.heyanle.easybangumi4.plugin.source.repository

/** Internal acceptance environments; never packaged in the application APK. */
enum class OfficialRepositoryEnvironment(
    val label: String,
    val indexUrl: String,
    val description: String,
) {
    RELEASE(
        label = "正式源仓库 (release)",
        indexUrl = "https://easybangumi.org/repository/v2/index.jsonl",
        description = "面向日常使用的正式源；新增或更新需通过手机验收。",
    ),
    DEV(
        label = "开发源仓库 (dev)",
        indexUrl = "https://raw.githubusercontent.com/easybangumiorg/CommunityJsExtensionEasyBangumi/main/channel/dev/index.jsonl",
        description = "用于测试候选源，可能暂时不可用；切换环境不会删除或降级已安装的源。",
    );

    fun repositoryInfo() = RepositoryInfo(url = indexUrl, label = label, addedAt = 0)

    companion object {
        fun fromUrl(url: String): OfficialRepositoryEnvironment? =
            entries.firstOrNull { it.indexUrl == url.trim() }
    }
}
