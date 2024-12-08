package com.heyanle.easy_bangumi_cm.repository.play

import com.heyanle.easy_bangumi_cm.repository.Extractor

/**
 * Created by heyanlin on 2024/12/5.
 */
class Episode (
    val id: String,
    val label: String,
    val order: Int,
    val playInfo: PlayInfo,
): Extractor{

    @Transient
    override var ext: String = ""
}