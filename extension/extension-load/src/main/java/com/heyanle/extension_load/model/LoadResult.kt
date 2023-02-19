package com.heyanle.extension_load.model

/**
 * Created by HeYanLe on 2023/2/19 16:27.
 * https://github.com/heyanLE
 */
sealed class LoadResult {
    class Success(val extension: Extension) : LoadResult()
    object Error : LoadResult()

}
