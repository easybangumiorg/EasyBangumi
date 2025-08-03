package com.heyanle.easybangumi4.plugin.js.utils;


import com.heyanle.easybangumi4.source_api.utils.core.SourceUtils;

import org.jsoup.Jsoup;

import kotlin.text.Regex;

/**
 * Created by HeYanLe on 2024/11/3 20:54.
 * https://github.com/heyanLE
 */

public class JSSourceUtils {


    public static String urlParser(String rootURL, String source) {
        return SourceUtils.INSTANCE.urlParser(rootURL, source);
    }
}
