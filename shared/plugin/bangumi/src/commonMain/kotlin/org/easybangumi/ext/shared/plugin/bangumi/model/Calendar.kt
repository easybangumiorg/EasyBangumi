package org.easybangumi.ext.shared.plugin.bangumi.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *    https://github.com/easybangumiorg/EasyBangumi
 *
 *    Copyright 2025 easybangumi.org and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 */

@Serializable
data class CalendarItem(
    @SerialName("weekday") var weekday: Weekday? = Weekday(),
    @SerialName("items") var items: ArrayList<CalendarSubject> = arrayListOf()
)

@Serializable
data class CalendarSubject(
    @SerialName("id") var id: Int? = null,
    @SerialName("url") var url: String? = null,
    @SerialName("type") var type: Int? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("name_cn") var nameCn: String? = null,
    @SerialName("summary") var summary: String? = null,
    @SerialName("air_date") var airDate: String? = null,
    @SerialName("air_weekday") var airWeekday: Int? = null,
    @SerialName("rating") var rating: Rating? = Rating(),
    @SerialName("rank") var rank: Int? = null,
    @SerialName("images") var images: Images? = Images(),
    @SerialName("collection") var collection: Collection? = Collection()
)

@Serializable
data class Weekday(
    @SerialName("en") var en: String? = null,
    @SerialName("cn") var cn: String? = null,
    @SerialName("ja") var ja: String? = null,
    @SerialName("id") var id: Int? = null
)