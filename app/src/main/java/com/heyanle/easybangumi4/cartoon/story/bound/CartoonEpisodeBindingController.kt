package com.heyanle.easybangumi4.cartoon.story.bound

import com.heyanle.easybangumi4.base.json.JsonFileProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * 集级绑定注册表，持久化于 cartoon_episode_binding.json。
 * 同一 (source, cartoonId) 的同一集只有一条绑定；重复 upsert 即覆盖（换绑到最新）。
 */
class CartoonEpisodeBindingController(
    private val jsonFileProvider: JsonFileProvider,
) {

    private val helper = jsonFileProvider.cartoonEpisodeBinding

    private val scope = MainScope()

    val bindings: StateFlow<List<CartoonEpisodeBinding>> = helper.requestFlow
        .map { it }
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    fun upsert(binding: CartoonEpisodeBinding) {
        helper.update { list ->
            val mutable = list.toMutableList()
            mutable.removeAll { it.sameEpisode(binding) }
            mutable.add(binding)
            mutable
        }
    }

    fun remove(source: String, cartoonId: String, episodeId: String, episodeOrder: Int) {
        helper.update { list ->
            list.filterNot { it.isSameEpisode(source, cartoonId, episodeId, episodeOrder) }
        }
    }

    fun removeBindingsForFlatFile(fileName: String) {
        helper.update { list ->
            list.filterNot {
                it.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE && it.flatFileName == fileName
            }
        }
    }

    fun removeBindingsForLocalItem(itemId: String) {
        helper.update { list ->
            list.filterNot {
                it.targetType == CartoonEpisodeBinding.TARGET_LOCAL_STORY && it.localItemId == itemId
            }
        }
    }

    fun findBindingsForFlatFile(fileName: String): List<CartoonEpisodeBinding> {
        return bindings.value.filter {
            it.targetType == CartoonEpisodeBinding.TARGET_FLAT_FILE && it.flatFileName == fileName
        }
    }

    fun findBinding(
        source: String,
        cartoonId: String,
        lineId: String?,
        episodeId: String,
        episodeOrder: Int,
    ): CartoonEpisodeBinding? {
        val candidates = bindings.value.filter {
            it.source == source && it.cartoonId == cartoonId
        }
        if (candidates.isEmpty()) return null
        val line = lineId.orEmpty()
        // 匹配优先级：同线路 episodeId -> episodeId -> 同线路 order -> order
        return candidates.firstOrNull {
            it.lineId == line && it.episodeId.isNotEmpty() && it.episodeId == episodeId
        } ?: candidates.firstOrNull {
            it.episodeId.isNotEmpty() && it.episodeId == episodeId
        } ?: candidates.firstOrNull {
            it.lineId == line && it.episodeOrder == episodeOrder
        } ?: candidates.firstOrNull {
            it.episodeOrder == episodeOrder
        }
    }

    fun findBindingsForCartoon(source: String, cartoonId: String): List<CartoonEpisodeBinding> {
        return bindings.value.filter { it.source == source && it.cartoonId == cartoonId }
    }

}
