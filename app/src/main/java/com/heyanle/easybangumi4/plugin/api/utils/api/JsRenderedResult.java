package com.heyanle.easybangumi4.plugin.api.utils.api;

public class JsRenderedResult {

    public final JsRenderedStrategy strategy;
    public final String url;
    public final boolean isTimeout;
    public final String content;
    public final String interceptResource;

    public JsRenderedResult(
            JsRenderedStrategy strategy,
            String url,
            boolean isTimeout,
            String content,
            String interceptResource
    ) {
        this.strategy = strategy;
        this.url = url;
        this.isTimeout = isTimeout;
        this.content = content;
        this.interceptResource = interceptResource;
    }
}
