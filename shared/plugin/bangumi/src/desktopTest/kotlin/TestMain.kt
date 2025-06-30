import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.shared.ktor.KtorConfig
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.test.platform
import org.easybangumi.shared.plugin.bangumi.business.BangumiBusiness
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.Subject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.test.KoinTest
import kotlin.test.Test
import kotlin.test.assertTrue

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
class TestMain: KoinTest {

    init {
        startKoin {
            modules(platform)
        }
    }

    @Test
    fun getSubject() {
        val hookUrl404 = "https://404"
        val hookUrl200 = "https://200"
        val hookUrlTimeout = "https://timeout"

        runBlocking {
            val mockEngine = MockEngine { req ->
                println("Mock request: ${req.url}")
                when (req.url.toString()) {
                    hookUrl200 -> {
                        respond(
                            content = subjectTestRespBody,
                            status = io.ktor.http.HttpStatusCode.OK
                        )
                    }
                    hookUrl404 -> {
                        respond(
                            content = subjectTestRespBody404,
                            status = io.ktor.http.HttpStatusCode.NotFound
                        )
                    }
                    hookUrlTimeout -> {
                        // 模拟超时
                        throw HttpRequestTimeoutException(req)
                    }
                    else -> {
                        respond(
                            content = "Unknown request",
                            status = io.ktor.http.HttpStatusCode.BadRequest
                        )
                    }
                }
            }
            val ktorFactory = object: KtorFactory {
                override fun create(vararg config: KtorConfig): HttpClient {
                    return HttpClient(mockEngine) {
                        config.forEach {
                            it.apply(this)
                        }
                        install(Logging) {
                            logger = Logger.DEFAULT
                            level = LogLevel.ALL
                        }
                    }
                }
            }
            val bangumiBusiness = BangumiBusiness(
                ktorFactory,
            )
            bangumiBusiness.hookDebugUrl = hookUrl200
            val resp200 = bangumiBusiness.api.getSubject("526816").await()

            bangumiBusiness.hookDebugUrl = hookUrl404
            val resp404 = bangumiBusiness.api.getSubject("404").await()

            bangumiBusiness.hookDebugUrl = hookUrlTimeout
           val respTimeout = bangumiBusiness.api.getSubject("timeout").await()

            println(resp200)
            println(resp404)
            println(respTimeout)
            assertTrue {
                resp200 is BgmRsp.Success<Subject> && resp200.code == 200 && resp200.data.id == 526816
            }
            assertTrue {
                resp404 is BgmRsp.Error && resp404.code == 404
            }

            assertTrue {
                respTimeout is BgmRsp.Error && respTimeout.isTimeout()
            }


        }

    }

    @Test
    fun getTrends() {
        runBlocking {
            val ktorFactory = object: KtorFactory {
                override fun create(vararg config: KtorConfig): HttpClient {
                    return HttpClient(Java) {
                        config.forEach {
                            it.apply(this)
                        }
                        install(Logging) {
                            logger = Logger.DEFAULT
                            level = LogLevel.ALL
                        }
                    }
                }
            }
            val bangumiBusiness = BangumiBusiness(
                ktorFactory,
            )
            val resp = bangumiBusiness.api.getTrends(1).await()
            println(resp)
            resp.throwIfError()

            assertTrue {
                resp is BgmRsp.Success<List<Subject>> && resp.code == 200 && resp.data.isNotEmpty()
            }

        }
    }




}