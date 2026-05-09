package org.easybangumi.next.source.inner.anich

import io.ktor.client.HttpClient
import org.easybangumi.next.lib.utils.ResourceOr
import org.easybangumi.next.shared.ktor.KtorFactory
import org.easybangumi.next.shared.ktor.ktorModule
import org.easybangumi.next.shared.source.api.component.Component
import org.easybangumi.next.shared.source.api.source.InnerSource
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

class AniChInnerSource : InnerSource() {

    companion object {
        const val SOURCE_KEY = "anich"
    }

    override val key: String = SOURCE_KEY
    override val label: ResourceOr = "AniCh"
    override val icon: ResourceOr = "https://anich.emmmm.eu.org/favicon.ico"
    override val version: Int = 1

    override val componentConstructor: Array<() -> Component> = arrayOf(
        ::AniChSearchComponent,
        ::AniChPlayComponent,
        ::AniChPrefComponent,
    )

    override val module: Module
        get() = module {
            includes(ktorModule)
            single {
                get<KtorFactory>().create()
            }.bind<HttpClient>()
        }
}
