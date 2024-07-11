package com.heyanle.easybangumi4.cartoon.local

import androidx.core.net.toUri
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.local.source.LocalSource
import com.heyanle.easybangumi4.source_api.entity.CartoonCover
import com.heyanle.easybangumi4.source_api.entity.CartoonCoverImpl
import com.heyanle.easybangumi4.utils.getMatchReg
import com.hippo.unifile.UniFile

/**
 * Created by heyanle on 2024/7/7.
 * https://github.com/heyanLE
 */
data class CartoonLocalMsg (
    // 目前 itemId 取 title，后续可能会改，先拆开
    val itemId: String,

    // form tvshow.nfo
    val title: String,
    val desc: String,
    val cover: String,
    val genres: List<String>,
) {
    companion object {
        const val TAG = "CartoonLocalMsg"

        fun formCartoonInfo(cartoonInfo: CartoonLocalItem): CartoonLocalMsg {
            return CartoonLocalMsg(
                itemId = cartoonInfo.itemId,
                title = cartoonInfo.title,
                desc = cartoonInfo.desc,
                cover = cartoonInfo.cover,
                genres = cartoonInfo.genre
            )
        }
    }
}

data class CartoonLocalItem (
    // folder uri
    val folderUri: String,
    // tvshow.nfo file uri
    val nfoUri: String,

    // 目前 itemId 取 title，后续可能会改，先拆开
    val itemId: String,

    // form tvshow.nfo
    val title: String,
    val desc: String,
    val cover: String,
    val genre: List<String>,

    val episodes: List<CartoonLocalEpisode>,
){

    companion object {
        const val TAG = "CartoonLocalItem"
        const val TV_SHOW_NFO_FILE_NAME = "tvshow.nfo"

    }

    val cartoonCover: CartoonCover by lazy {
        CartoonCoverImpl(
            id = itemId,
            title = title,
            source = LocalSource.LOCAL_SOURCE_KEY,
            url = "",
            intro = "",
            coverUrl = if (cover.startsWith("http")) cover else UniFile.fromUri(APP, folderUri.toUri())?.findFile(cover)?.uri?.toString(),
        )
    }

    fun matches(query: String): Boolean {
        var matched = false
        for (match in query.split(',')) {
            val regex = match.getMatchReg()
            if (title.matches(regex)) {
                matched = true
                break
            }
        }
        return matched
    }

}

data class CartoonLocalEpisode(
    val title: String,
    val episode: Int,
    val addTime: String,

    val mediaUri: String,
    val nfoUri: String,
)