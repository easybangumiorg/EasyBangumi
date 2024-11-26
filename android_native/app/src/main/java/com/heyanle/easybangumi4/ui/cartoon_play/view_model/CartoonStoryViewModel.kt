package com.heyanle.easybangumi4.ui.cartoon_play.view_model

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.base.DataResult
import com.heyanle.easybangumi4.cartoon.entity.CartoonInfo
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalEpisode
import com.heyanle.easybangumi4.cartoon.entity.CartoonStoryItem
import com.heyanle.easybangumi4.cartoon.entity.PlayLineWrapper
import com.heyanle.easybangumi4.cartoon.story.CartoonStoryController
import com.heyanle.easybangumi4.source_api.entity.Episode
import com.heyanle.easybangumi4.utils.MediaAndroidUtils
import com.heyanle.inject.core.Inject
import com.hippo.unifile.UniFile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File


/**
 * Created by heyanle on 2024/7/15.
 * https://github.com/heyanLE
 */
class CartoonStoryViewModel: ViewModel() {

    private val cartoonStoryController: CartoonStoryController by Inject.injectLazy()





    fun delete(
        triple: Triple<CartoonInfo, PlayLineWrapper, List<Episode>>, callback: ()-> Unit
    ){
        viewModelScope.launch {
            val l = cartoonStoryController.storyItemList.filterIsInstance<DataResult.Ok<List<CartoonStoryItem>>>().first()
            val list = l.okOrNull()?: emptyList()
            val item = list.firstOrNull {
                it.cartoonLocalItem.itemId == triple.first.id
            } ?: return@launch
            val episode = triple.third.map {  ep ->
                item.cartoonLocalItem.episodes.firstOrNull { it.episode == ep.order }
            }.filterIsInstance<CartoonLocalEpisode>()
            cartoonStoryController.removeEpisodeItem(episode)
            cartoonStoryController.storyItemList.filterIsInstance<DataResult.Ok<List<CartoonStoryItem>>>().filter { it !=  l}.first()
            callback()
        }

    }


    fun save(
        triple: Triple<CartoonInfo, PlayLineWrapper, List<Episode>>, callback: ()-> Unit
    ){
        viewModelScope.launch {
            val l = cartoonStoryController.storyItemList.filterIsInstance<DataResult.Ok<List<CartoonStoryItem>>>().first()
            val list = l.okOrNull()?: emptyList()
            val item = list.firstOrNull {
                it.cartoonLocalItem.itemId == triple.first.id
            } ?: return@launch
            val episode = triple.third.map {  ep ->
                item.cartoonLocalItem.episodes.firstOrNull { it.episode == ep.order }
            }.filterIsInstance<CartoonLocalEpisode>()
            episode.map { it.mediaUri }.let {
                MediaAndroidUtils.mediaScan(APP, it.map {
                    UniFile.fromUri(APP, it.toUri())?.filePath?.let {
                        FileProvider.getUriForFile(APP, "${APP.packageName}.provider", File(it))
                        File(it).toUri()
                    }?.toString() ?: it
                }, listOf("video/*"))
            }
            callback()
        }

    }

}