package org.easybangumi.next.shared.data.store

import org.easybangumi.next.lib.store.file_helper.json.JsonFileHelper
import org.easybangumi.next.lib.store.file_helper.json.JsonlFileHelper
import org.easybangumi.next.lib.utils.pathProvider
import org.easybangumi.next.shared.data.cartoon.CartoonTag
import kotlin.reflect.typeOf

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
object StoreProvider {

    data class SearchHistory(
        val key: String,
        val time: Long,
    )
    val searchHistory: JsonlFileHelper<SearchHistory> by lazy {
        JsonlFileHelper(
            folder = pathProvider.getFilePath("store"),
            name = "search_history",
            clazz = SearchHistory::class,
        )
    }

    val cartoonTag: JsonlFileHelper<CartoonTag> by lazy {
        JsonlFileHelper(
            folder = pathProvider.getFilePath("store"),
            name = "collection_tag.jsonl",
            clazz = CartoonTag::class,
        )
    }

}