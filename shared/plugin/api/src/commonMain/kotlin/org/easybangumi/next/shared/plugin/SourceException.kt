package org.easybangumi.next.shared.plugin


/**
 * 番源异常，可预见的，业务中会展示 msg
 * Created by HeYanLe on 2024/12/8 21:36.
 * https://github.com/heyanLE
 */

class SourceException(
    val msg: String,
    val error: Throwable? = null,
): Exception(msg, error) {
    constructor(msg: String): this(msg, null)
}