package com.heyanle.easybangumi4.source_api.utils.api

/**
 * Created by LoliBall on 2023/12/17 0:33.
 * https://github.com/WhichWho
 */
interface CaptchaHelper {

    /**
     * 打开一个对话框请求用户输入验证码
     * @param image 验证码图片，可以是以下类型
     * - [String] (mapped to a [Uri])
     * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
     * - [HttpUrl]
     * - [File]
     * - [DrawableRes]
     * - [Drawable]
     * - [Bitmap]
     * - [ByteArray]
     * - [ByteBuffer]
     * @param text 对话框提示文字
     * @param title 对话框标题
     * @param hint 用户输入框提示
     * @param onFinish 用户输入完毕确认后回调
     */
    fun start(
        image: Any,
        text: String? = null,
        title: String? = null,
        hint: String? = null,
        onFinish: (String) -> Unit
    )

    suspend fun start(
        image: Any,
        text: String? = null,
        title: String? = null,
        hint: String? = null
    ): String

}