package com.heyanle.extension_load.model

import java.lang.Exception

/**
 * Created by HeYanLe on 2023/2/19 16:27.
 * https://github.com/heyanLE
 */
sealed class LoadResult<T: Extension> {
    class Success<T: Extension>(val extension: T) : LoadResult<T>()

    class Error<T: Extension>(val exception: Exception?, val errMsg: String) : LoadResult<T>()

}
