package com.heyanle.easybangumi4.plugin.source.js.utils;


import com.heyanle.easybangumi4.plugin.api.utils.core.SourceUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

import kotlin.text.Regex;

/**
 * Created by HeYanLe on 2024/11/3 20:54.
 * https://github.com/heyanLE
 */

public class JSSourceUtils {


    public static String urlParser(String rootURL, String source) {
        System.currentTimeMillis();
        return SourceUtils.INSTANCE.urlParser(rootURL, source);
    }

}
