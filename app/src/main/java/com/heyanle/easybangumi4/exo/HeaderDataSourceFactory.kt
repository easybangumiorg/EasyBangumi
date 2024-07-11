package com.heyanle.easybangumi4.exo

import android.app.Application
import android.net.Uri
import android.view.PixelCopy
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Created by HeYanLe on 2023/8/13 23:15.
 * https://github.com/heyanLE
 */
@UnstableApi
class HeaderDataSourceFactory(
    private val application: Application
) : DataSource.Factory {

    val http by lazy { DefaultHttpDataSource.Factory() }

    private val headers = HashMap<String, Map<String, String>>()
    private val readWriteLock = ReentrantReadWriteLock()

    override fun createDataSource(): DataSource {
        return readWriteLock.read {
            val map = hashMapOf<String, Map<String, String>>()
            map.putAll(headers)
            CustomHttpDataSource(
                map,
                http.createDataSource()
            )
        }
    }

    fun put(url: String, headers: Map<String, String>) {
        readWriteLock.write {
            val u = Uri.parse(url)?.toString() ?: url
            this.headers[u] = headers
        }
    }

    fun remove(url: String) {
        readWriteLock.write {
            val u = Uri.parse(url)?.toString() ?: url
            this.headers.remove(u)
        }
    }
}

@UnstableApi
class CustomHttpDataSource(
    val headerMap: Map<String, Map<String, String>>,
    val source: DefaultHttpDataSource
) : DataSource by source {

    override fun open(dataSpec: DataSpec): Long {
        // ensure newest token
        val header = headerMap[dataSpec.uri.toString()] ?: emptyMap()
        for (entry in header.iterator()) {
            source.setRequestProperty(entry.key, entry.value)
        }
        return source.open(dataSpec)
    }

}
