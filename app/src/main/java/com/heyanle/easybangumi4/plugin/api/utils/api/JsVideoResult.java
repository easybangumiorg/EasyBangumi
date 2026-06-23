package com.heyanle.easybangumi4.plugin.api.utils.api;

public class JsVideoResult {

    public final JsVideoStrategy strategy;
    public final String url;
    public final boolean isTimeout;
    public final boolean isM3u8;

    public JsVideoResult(
            JsVideoStrategy strategy,
            String url,
            boolean isTimeout,
            boolean isM3u8
    ) {
        this.strategy = strategy;
        this.url = url;
        this.isTimeout = isTimeout;
        this.isM3u8 = isM3u8;
    }
}
