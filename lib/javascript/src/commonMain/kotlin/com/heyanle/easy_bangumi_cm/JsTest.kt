package com.heyanle.easy_bangumi_cm

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.define
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

/**
 * Created by heyanlin on 2025/1/7.
 */
fun main() {
    runBlocking {
        val quickJs = QuickJs.create(Dispatchers.IO)
        val quickJs2 = QuickJs.create(Dispatchers.IO)
        quickJs.define("console") {
            function("log") { args ->
                println(args.joinToString(" "))
            }
        }
        quickJs.define("utils") {
            asyncFunction("get") { args ->
                delay(5000)
                "1"
            }
        }
        val byte = quickJs.compile(testJs, "DebugSourceV1", asModule = true)
        quickJs.addModule(byte)
        quickJs.evaluate<String>(
            """
            import * as DebugSourceV1 from "DebugSourceV1";
            const player = new DebugSourceV1.PlayerComponent();
            console.log(DebugSourceV1.PlayerComponent === undefined);
            console.log(DebugSourceV1.HomeComponent === undefined);
            player.firstKey().then(console.log);
        """.trimIndent(),
            asModule = true
        )

        quickJs2.addModule(byte)

    }
}

val testJs = """
    export class PlayerComponent {
        async firstKey() {
            console.log("firstKey");
            return await utils.get();
        }
    }
""".trimIndent()