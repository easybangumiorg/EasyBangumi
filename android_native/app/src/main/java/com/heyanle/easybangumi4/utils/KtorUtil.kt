package com.heyanle.easybangumi4.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File

/**
 * Created by heyanlin on 2023/11/17.
 */
object KtorUtil {

    val client = HttpClient()


}

suspend fun String.downloadTo(path: String) {
    val targetFileTemp = File("${path}.temp")
    val targetFile = File(path)
    targetFile.parentFile?.mkdirs()

    if(targetFileTemp.exists()){
        targetFileTemp.delete()
    }
    KtorUtil.client.prepareGet (this).execute { httpResponse ->
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
    if (targetFileTemp.exists() && targetFileTemp.length() > 0){
        targetFile.delete()
        targetFileTemp.renameTo(targetFile)
    }

}

