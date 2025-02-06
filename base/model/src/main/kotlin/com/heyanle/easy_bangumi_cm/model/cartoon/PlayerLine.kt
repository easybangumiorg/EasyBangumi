package com.heyanle.easy_bangumi_cm.model.cartoon

/**
 * Created by heyanlin on 2024/12/5.
 */
class PlayerLine (
    val id: String,
    val label: String = LABEL_NONE,
    val order: Int,
    val episodeList : List<Episode>
): Extractor {
    override var ext: String = ""

    companion object {
        // 无
        const val LABEL_NONE = "##none##"
    }
}