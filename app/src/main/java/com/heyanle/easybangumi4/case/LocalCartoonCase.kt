package com.heyanle.easybangumi4.case

import com.heyanle.easybangumi4.cartoon_download.LocalCartoonController
import com.heyanle.easybangumi4.cartoon_download.entity.LocalCartoon
import com.heyanle.easybangumi4.cartoon_download.entity.LocalEpisode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by heyanlin on 2023/10/2.
 */
class LocalCartoonCase(
    private val localCartoonController: LocalCartoonController
) {

    suspend fun awaitLocalCartoon(): List<LocalCartoon> {
        return flowLocalCartoon()
            .first()
    }

    fun flowLocalCartoon(): Flow<List<LocalCartoon>> {
        return localCartoonController.localCartoon.filter { it != null }
            .filterIsInstance<List<LocalCartoon>>()
    }


    fun flowLocalCartoon(uuid: String): Flow<LocalCartoon?> {
        return localCartoonController.localCartoon.filter { it != null }
            .filterIsInstance<List<LocalCartoon>>()
            .map {
                it.find { it.uuid == uuid }
            }
    }

    suspend fun findWithSummary(id: String, source: String, url: String): LocalCartoon? {
        return awaitLocalCartoon().find { it.cartoonId == id && it.cartoonSource == source && it.cartoonUrl == url }
    }

    suspend fun findWithUUID(uuid: String): LocalCartoon? {
        return awaitLocalCartoon().find { it.uuid == uuid }
    }

    suspend fun remove(list: List<LocalEpisode>) {
        localCartoonController.removeEpisode(list)
    }
}