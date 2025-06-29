package org.easybangumi.shared.plugin.bangumi.business

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.Deferred
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.CalendarItem
import org.easybangumi.shared.plugin.bangumi.model.Subject

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

class BangumiApiImpl (
   private val caller: BangumiCaller,
   private val bangumiBaseUrl: String = "https://api.bgm.tv/",
): BangumiApi {

   interface BangumiCaller {
      var hookDebugUrl: String?
      fun <T> request(block: suspend HttpClient.() -> BgmRsp<T>): Deferred<BgmRsp<T>>
   }


   private fun HttpRequestBuilder.bgmUrl(path:String) {
      url(caller.hookDebugUrl ?: (bangumiBaseUrl + path))
   }

   // get subjects/{subjectId}
   override fun getSubject(subjectId: String): Deferred<BgmRsp<Subject>> {
      return caller.request {
         get {
            bgmUrl("v0/subjects/$subjectId")
         }.body()
      }
   }

   override fun calendar(): Deferred<BgmRsp<List<CalendarItem>>> {
      return caller.request {
         get {
            bgmUrl("calendar")
         }.body()
      }
   }

}