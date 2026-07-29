package com.heyanle.easybangumi4.danmaku

/** The only source registry. Its factory accepts only sources wired by the application module. */
class InnerDanmakuSourceRegistry private constructor(
    private val sourcesById: Map<String, DanmakuSource>,
) {
    val sources: List<DanmakuSource>
        get() = sourcesById.values.toList()

    fun source(id: String): DanmakuSource? = sourcesById[id]

    fun enabledAndAvailable(enabledIds: Set<String>): List<DanmakuSource> {
        return sources.filter { it.metadata.id in enabledIds && it.isAvailable() }
    }

    companion object {
        fun create(danDanPlaySource: DanDanPlaySource): InnerDanmakuSourceRegistry {
            return InnerDanmakuSourceRegistry(
                sourcesById = mapOf(danDanPlaySource.metadata.id to danDanPlaySource),
            )
        }
    }
}
