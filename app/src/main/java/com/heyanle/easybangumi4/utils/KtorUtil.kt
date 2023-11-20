package com.heyanle.easybangumi4.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by heyanlin on 2023/11/17.
 */
object KtorUtil {

    val client = HttpClient()


}

suspend fun String.downloadTo(path: String, onProcess: (Float) -> Unit) {
    val targetFileTemp = File("${path}.temp")
    val targetFile = File(path)

    if(targetFileTemp.exists()){
        targetFileTemp.delete()
    }
    val statement = KtorUtil.client.prepareGet (this).execute { httpResponse ->
        val channel: ByteReadChannel = httpResponse.body()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            while (!packet.isEmpty) {
                val bytes = packet.readBytes()
                if(!targetFileTemp.exists()){
                    targetFileTemp.createNewFile()
                }
                targetFileTemp.appendBytes(bytes)
            }
        }
    }

}

