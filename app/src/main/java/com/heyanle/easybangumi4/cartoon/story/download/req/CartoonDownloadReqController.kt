package com.heyanle.easybangumi4.cartoon.story.download.req

import com.heyanle.easybangumi4.base.json.JsonFileProvider
import com.heyanle.easybangumi4.cartoon.entity.CartoonDownloadReq
import kotlinx.coroutines.MainScope

/**
 * 下载任务管理器，持久化保存
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
class CartoonDownloadReqController(
    private val jsonFileProvider: JsonFileProvider
) {

    private val scope = MainScope()


    private val helper = jsonFileProvider.cartoonDownload
    val downloadItem = helper.flow


    fun newDownloadItem(item: Collection<CartoonDownloadReq>) {
        helper.update {
            val list = it.toMutableList()
            list.addAll(item)
            list
        }
    }

    fun newDownloadItem(item: CartoonDownloadReq) {
        helper.update {
            val list = it.toMutableList()
            list.add(item)
            list
        }
    }


    fun removeDownloadItem(uuid: String) {
        helper.update {
            it.map {
                if(it.uuid == uuid){
                    null
                }else{
                    it
                }
            }.filterNotNull()

        }
    }

    fun removeDownloadItem(uuid: List<String>) {
        val set = uuid.toSet()
        helper.update {
            it.map {
                if(set.contains(it.uuid)){
                    null
                }else{
                    it
                }
            }.filterNotNull()

        }
    }

    fun removeDownloadItemWithItemId(itemId: Collection<String>) {
        helper.update {
            it.map {
                if(itemId.contains(it.toLocalItemId)){
                    null
                }else{
                    it
                }
            }.filterNotNull()
        }
    }

    fun replaceDownloadItem(item: CartoonDownloadReq) {
        helper.update { current ->
            var replaced = false
            val updated = current.map {
                if (it.uuid == item.uuid) {
                    replaced = true
                    item
                } else {
                    it
                }
            }
            if (replaced) updated else updated + item
        }
    }

    fun findDownloadItem(uuid: String): CartoonDownloadReq? {
        return helper.getOrNull()?.firstOrNull { it.uuid == uuid }
    }

    fun findDownloadItemsByLocalItemIds(itemIds: Collection<String>): List<CartoonDownloadReq> {
        val ids = itemIds.toSet()
        return helper.getOrNull()?.filter { it.toLocalItemId in ids }.orEmpty()
    }

}
