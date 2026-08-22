package com.heyanle.easybangumi4.cartoon.story.download.engine

class QuickDownloadEngineRegistry private constructor(
    engines: List<QuickDownloadEngine>,
) {
    private val enginesById: Map<String, QuickDownloadEngine>

    init {
        val duplicateIds = engines.groupingBy { it.descriptor.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicateIds.isEmpty()) {
            "Duplicate quick download engine ids: ${duplicateIds.joinToString()}"
        }
        enginesById = engines.associateBy { it.descriptor.id }
    }

    val descriptors: List<QuickDownloadEngineDescriptor>
        get() = enginesById.values.map { it.descriptor }

    fun find(id: String): QuickDownloadEngine? = enginesById[id]

    companion object {
        fun create(vararg engines: QuickDownloadEngine): QuickDownloadEngineRegistry {
            return QuickDownloadEngineRegistry(engines.toList())
        }
    }
}
