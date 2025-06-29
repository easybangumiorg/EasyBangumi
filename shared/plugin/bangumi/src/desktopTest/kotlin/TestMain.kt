import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.runBlocking
import org.easybangumi.next.DesktopHostArch
import org.easybangumi.next.DesktopHostOs
import org.easybangumi.next.Platform
import org.easybangumi.next.PlatformType
import org.easybangumi.next.shared.ktor.KtorConfig
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.shared.plugin.bangumi.business.BangumiBusiness
import org.easybangumi.shared.plugin.bangumi.model.BgmRsp
import org.easybangumi.shared.plugin.bangumi.model.Subject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
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
class TestMain {

    fun injectPlatform() {
        startKoin {
            modules(module {
                single {
                    object: Platform {
                        override val platformType: PlatformType
                            get() = PlatformType.Desktop
                        override val platformName: String
                            get() = PlatformType.Desktop.name
                        override val isDebug: Boolean = true
                        override val versionCode: Int = 1
                        override val versionName: String = "1.0.0"
                        override val hostOs: DesktopHostOs = DesktopHostOs.Windows
                        override val hostArch: DesktopHostArch = DesktopHostArch.X64
                    }
                }.binds(arrayOf(Platform::class))
            })
        }
    }

    @Test
    fun getSubject() {
        injectPlatform()
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




}