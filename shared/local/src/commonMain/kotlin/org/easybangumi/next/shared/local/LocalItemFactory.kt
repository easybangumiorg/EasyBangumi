package org.easybangumi.next.shared.local

import okio.buffer
import okio.use
import org.easybangumi.next.lib.unifile.UFD
import org.easybangumi.next.lib.unifile.UniFile

/**
 * 本地番剧数据模型
 */
data class LocalCartoonItem(
    val itemId: String,
    val folderUFD: UFD,
    val title: String,
    val desc: String,
    val cover: String,
    val tags: List<String>,
    val episodes: List<LocalEpisode>,
)

data class LocalEpisode(
    val title: String,
    val episode: Int,
    val mediaUFD: UFD,
    val nfoUFD: UFD?,
)

/**
 * Kodi NFO 解析和创建
 */
object LocalItemFactory {

    /**
     * 从文件夹解析本地番剧
     */
    fun getItemFromFolder(uniFile: UniFile): LocalCartoonItem? {
        if (!uniFile.isDirectory()) return null

        val nfoFile = uniFile.child("tvshow.nfo") ?: return null
        if (!nfoFile.exists()) return null

        val nfoContent = try {
            nfoFile.openSource().use { source ->
                source.buffer().readUtf8()
            }
        } catch (e: Exception) {
            return null
        }

        val title = parseXmlTag(nfoContent, "title") ?: return null
        val desc = parseXmlTag(nfoContent, "plot") ?: ""
        val poster = parseXmlTag(nfoContent, "poster") ?: ""
        val tags = parseAllXmlTags(nfoContent, "tag")

        val episodes = uniFile.listFiles()?.mapNotNull { file ->
            getEpisodeFromFile(uniFile, file)
        } ?: emptyList()

        return LocalCartoonItem(
            itemId = uniFile.getName(),
            folderUFD = uniFile.getUFD(),
            title = title,
            desc = desc,
            cover = poster,
            tags = tags,
            episodes = episodes
        )
    }

    /**
     * 从文件解析剧集信息
     */
    fun getEpisodeFromFile(folder: UniFile, media: UniFile?): LocalEpisode? {
        media ?: return null
        val name = media.getName()
        if (!name.endsWith(".mp4") && !name.endsWith(".mkv")) return null

        val baseName = name.removeSuffix(".mp4").removeSuffix(".mkv")
        val nfoFile = folder.child("$baseName.nfo")

        var title = ""
        var episode = 0

        if (nfoFile != null && nfoFile.exists()) {
            try {
                val nfoContent = nfoFile.openSource().use { it.buffer().readUtf8() }
                title = parseXmlTag(nfoContent, "title") ?: ""
                episode = parseXmlTag(nfoContent, "episode")?.toIntOrNull() ?: 0
            } catch (e: Exception) {
                // 降级到文件名刮削
            }
        }

        if (episode == 0) {
            // 从文件名刮削：提取 E 后面的数字
            val regex = Regex("""E(\d+)$""")
            val match = regex.find(baseName)
            episode = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }

        if (title.isEmpty()) {
            // 从文件名提取标题
            title = baseName.replace(Regex("""\s*E\d+$"""), "").trim()
        }

        return LocalEpisode(
            title = title,
            episode = episode,
            mediaUFD = media.getUFD(),
            nfoUFD = nfoFile?.getUFD()
        )
    }

    /**
     * 创建本地番剧文件夹和 tvshow.nfo
     */
    fun createItemFolder(
        rootFolder: UniFile,
        itemId: String,
        title: String,
        desc: String,
        coverUrl: String,
        tags: List<String>,
    ): UniFile? {
        val targetFolder = rootFolder.createDirectory(itemId) ?: return null
        if (!targetFolder.canWrite()) return null

        // 生成 tvshow.nfo：child() 获取引用，openSink() 自动创建
        val nfoContent = buildTvShowNfo(title, desc, coverUrl, tags)
        val nfoFile = targetFolder.child("tvshow.nfo") ?: return null
        try {
            nfoFile.openSink(false).buffer().use { it.writeUtf8(nfoContent) }
        } catch (e: Exception) {
            return null
        }

        return targetFolder
    }

    /**
     * 生成剧集 NFO 内容
     */
    fun buildEpisodeNfo(title: String, season: Int, episode: Int): String {
        return buildString {
            appendLine("<episodedetails>")
            appendLine("  <title>${escapeXml(title)}</title>")
            appendLine("  <season>$season</season>")
            appendLine("  <episode>$episode</episode>")
            appendLine("</episodedetails>")
        }
    }

    /**
     * 生成 tvshow.nfo 内容
     */
    private fun buildTvShowNfo(title: String, desc: String, cover: String, tags: List<String>): String {
        return buildString {
            appendLine("<tvshow>")
            appendLine("  <title>${escapeXml(title)}</title>")
            appendLine("  <plot>${escapeXml(desc)}</plot>")
            appendLine("  <art>")
            appendLine("    <poster>${escapeXml(cover)}</poster>")
            appendLine("  </art>")
            for (tag in tags) {
                appendLine("  <tag>${escapeXml(tag)}</tag>")
            }
            appendLine("</tvshow>")
        }
    }

    /**
     * 简单解析 XML 标签内容
     */
    private fun parseXmlTag(xml: String, tag: String): String? {
        val regex = Regex("""<$tag>(.*?)</$tag>""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(xml)?.groupValues?.get(1)?.trim()
    }

    /**
     * 解析所有同名 XML 标签
     */
    private fun parseAllXmlTags(xml: String, tag: String): List<String> {
        val regex = Regex("""<$tag>(.*?)</$tag>""", RegexOption.DOT_MATCHES_ALL)
        return regex.findAll(xml).map { it.groupValues[1].trim() }.toList()
    }

    /**
     * XML 转义
     */
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
