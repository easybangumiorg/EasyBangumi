package com.heyanle.easybangumi.utils.oksp

import java.lang.ClassCastException
import java.lang.Exception

/**
 * Created by HeYanLe on 2021/10/10 15:27.
 * https://github.com/heyanLE
 */
class OKSPException(msg: String): Exception(msg) {
    companion object{
        fun decoderNotFound() = OKSPException("Decoder Not Found")
        fun encoderNotFound() = OKSPException("Encoder Not Found")
        fun cantIterator() = OKSPException("Can't use OKSPSet.iterator, try OKSPSet#getSnapShootValue")
        fun castException(msg: String) = OKSPException(msg)
        fun uninitialized() = OKSPException("Please initialized first")
    }
}