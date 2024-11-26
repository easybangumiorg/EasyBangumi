package com.heyanle.easybangumi4.utils

import androidx.compose.runtime.Composable
import androidx.lifecycle.MutableLiveData
import com.heyanle.easy_i18n.R

/**
 * 带状态的数据
 * Created by HeYanLe on 2023/1/8 22:49.
 * https://github.com/heyanLE
 */
sealed class StatusData<T> {
    class None<T> : StatusData<T>()
    data class Loading<T>(
        val loadingText: String = stringRes(R.string.loading)
    ) : StatusData<T>()

    data class Error<T>(
        val errorMsg: String = "",
        val throwable: Throwable? = null,
    ) : StatusData<T>()

    data class Data<T>(
        val data: T
    ) : StatusData<T>()

    fun isNone(): Boolean {
        return this is None
    }

    fun isLoading(): Boolean {
        return this is Loading
    }

    fun isError(): Boolean {
        return this is Error
    }

    fun isData(): Boolean {
        return this is Data
    }

    @Composable
    fun composeLoading(content: @Composable (Loading<T>) -> Unit): StatusData<T> {
        (this as? Loading)?.let {
            content(it)
        }
        return this
    }

    @Composable
    fun composeError(content: @Composable (Error<T>) -> Unit): StatusData<T> {
        (this as? Error)?.let {
            content(it)
        }
        return this
    }

    @Composable
    fun composeData(content: @Composable (Data<T>) -> Unit): StatusData<T> {
        (this as? Data)?.let {
            content(it)
        }
        return this
    }

    fun onLoading(block: (Loading<T>) -> Unit): StatusData<T> {
        (this as? Loading)?.let {
            block(it)
        }
        return this
    }

    fun onError(block: (Error<T>) -> Unit): StatusData<T> {
        (this as? Error)?.let {
            block(it)
        }
        return this
    }

    fun onData(block: (Data<T>) -> Unit): StatusData<T> {
        (this as? Data)?.let {
            block(it)
        }
        return this
    }

}

fun <T> MutableLiveData<StatusData<T>>.loading(loadingText: String = stringRes(R.string.loading)) {
    value = StatusData.Loading(loadingText)
}

fun <T> MutableLiveData<StatusData<T>>.error(
    errorMsg: String = "",
    throwable: Throwable? = null,
) {
    value = StatusData.Error(errorMsg, throwable)
}

fun <T> MutableLiveData<StatusData<T>>.data(data: T) {
    value = StatusData.Data(data)
}
