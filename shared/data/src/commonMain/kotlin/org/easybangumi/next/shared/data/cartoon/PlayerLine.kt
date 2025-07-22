package org.easybangumi.next.shared.data.cartoon

/**
 * Created by heyanle on 2024/12/5.
 */
class PlayerLine (
    val id: String,
    val label: String = LABEL_NONE,
    val order: Int = -1,
    val episodeList : List<Episode>
): Extractor {
    override var ext: String = ""

    companion object {
        // 无
        const val LABEL_NONE = "##none##"
    }
}