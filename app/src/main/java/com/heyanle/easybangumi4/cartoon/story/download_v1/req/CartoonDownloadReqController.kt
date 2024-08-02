package com.heyanle.easybangumi4.cartoon.story.download_v1.req

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
            }.filterNotNull() ?: emptyList()

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
            }.filterNotNull() ?: emptyList()
        }
    }


}