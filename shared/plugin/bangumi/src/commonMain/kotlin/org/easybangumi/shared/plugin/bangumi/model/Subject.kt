package org.easybangumi.shared.plugin.bangumi.model

/**
 * Created by heyanle on 2025/6/27.
 */
data class ImageInfo (
    val large: String,
    val common : String,
    val medium: String,
    val small: String,
    val grid: String,
)

data class SubjectSmall (
    val id: Long,
    val name: String,
    val nameCN: String,
    val summary: String,
    val airData: String,
    val airWeekDay: Int,

    val imageInfo: ImageInfo,
    val eps: Int,
    val epsCount: Int,


)