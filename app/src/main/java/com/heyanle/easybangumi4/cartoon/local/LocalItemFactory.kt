package com.heyanle.easybangumi4.cartoon.local

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.heyanle.easybangumi4.APP
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalEpisode
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalItem
import com.heyanle.easybangumi4.cartoon.entity.CartoonLocalMsg
import com.heyanle.easybangumi4.utils.deleteRecursively
import com.hippo.unifile.UniFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by heyanle on 2024/7/9.
 * https://github.com/heyanLE
 */
object LocalItemFactory {


    @WorkerThread
    suspend fun newItemFolder(cartoonInfo: CartoonLocalMsg, rootFolder: UniFile): UniFile? {
        var targetFolder = rootFolder.findFile(cartoonInfo.itemId)
        if (targetFolder != null && targetFolder.exists()){
            targetFolder.deleteRecursively()
        }
        targetFolder = rootFolder.createDirectory(cartoonInfo.itemId) ?: return null
        if (!targetFolder.canWrite()) {
            return null
        }
        // bmp
        var hasPng = false
        val result = Coil.imageLoader(APP).execute(
            ImageRequest.Builder(APP)
                .data(cartoonInfo.cover)
                .build()
        )
        val bmp = result.drawable?.toBitmap()
        if (bmp != null){
            val pngFolder = targetFolder.createFile("cover.png")
            if (pngFolder != null && pngFolder.canWrite()) {
                pngFolder.openOutputStream().use {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                hasPng = true
            }
        }
        val nfoFile = targetFolder.createFile("tvshow.nfo") ?: return null
        val tvShow =  Element("tvshow")
        tvShow.appendElement("title").text(cartoonInfo.title)
        tvShow.appendElement("plot").text(cartoonInfo.desc)
        tvShow.appendElement("art").appendElement("poster").text(if (hasPng)"cover.png" else cartoonInfo.cover)
        cartoonInfo.genres.forEach {
            tvShow.appendElement("tag").text(it)
        }
        nfoFile.openOutputStream().use {
            it.write(tvShow.toString().toByteArray())
        }
        return targetFolder
    }

    @WorkerThread
    fun getItemFromFolder(uniFile: UniFile): CartoonLocalItem? {
        if (!uniFile.isDirectory){
            return null
        }
        val nfoFile = uniFile.findFile(CartoonLocalItem.TV_SHOW_NFO_FILE_NAME) ?: return null

        val jsoup = Jsoup.parse(nfoFile.openInputStream().reader().readText())
        val tvShow = jsoup.getElementsByTag("tvshow").first() ?: return null
        val title = tvShow.getElementsByTag("title").first()?.text() ?: return null
        val desc = tvShow.getElementsByTag("plot").first()?.text() ?: ""
        val cover = tvShow.getElementsByTag("art").first()?.getElementsByTag("poster")?.text() ?: ""
        val genre = tvShow.getElementsByTag("tag").map { it.text() }

        val episodes = uniFile.listFiles()?.mapNotNull { getEpisodeItemFromFile(uniFile, it) } ?: emptyList()
        return CartoonLocalItem(
            folderUri = uniFile.uri.toString(),
            nfoUri = nfoFile.uri.toString(),
            title = title,
            desc = desc,
            cover = cover,
            genre = genre,
            episodes = episodes,
            itemId = uniFile.name ?: return null
        )

    }

    @WorkerThread
    fun getEpisodeItemFromFile(folder: UniFile, media: UniFile): CartoonLocalEpisode? {
        val mediaName = media.name ?: return null
        if (!mediaName.endsWith(".mp4") && !mediaName.endsWith(".mkv")){
            // 暂时只解析 MP4 和 MKV 结尾的文件
            return null
        }
        val n = mediaName.replace(".mp4", "").replace(".mkv", "")
        val nfo = folder.findFile("${n}.nfo")
        var title: String = ""
        var addTime: String = ""
        var episode: Int = 0
        var completely = false
        if (nfo?.exists() == true){
            // 获取 nfo 文件
            val jsoup = Jsoup.parse(nfo.openInputStream().reader().readText())
            val episodedetails = jsoup.getElementsByTag("episodedetails").first()
            if (episodedetails != null){
                title = episodedetails.getElementsByTag("title").first()?.text() ?: ""
                addTime = episodedetails.getElementsByTag("dateadded").first()?.text() ?: ""
                episode = episodedetails.getElementsByTag("episode").first()?.text()?.toIntOrNull() ?: 0
                completely = true
            }

        }
        if (!completely){
            // 根据名称刮削
            title = n.replace(folder.name?:"", "").trim()
            // 正则提取 n 中最后一个 E 字符后面的数字
            val e = n.substringAfterLast("E").trimEnd().toIntOrNull()
            if (e != null){
                episode = e
            }
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date: String = format.format(media.lastModified())
            addTime = date
        }


        return CartoonLocalEpisode(
            title = title,
            episode = episode,
            addTime = addTime,
            mediaUri = media.uri.toString(),
            nfoUri = nfo?.uri?.toString() ?: ""
        )

    }
}