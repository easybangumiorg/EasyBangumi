package com.heyanle.easybangumi4.plugin.api.utils.api;

import java.util.Map;

public class JsRenderedStrategy {

    public final String url;
    public final String callBackRegex;
    public final String encoding;
    public final String userAgentString;
    public final Map<String, String> header;
    public final String actionJs;
    public final boolean isBlockBlob;
    public final long timeOut;
    public final boolean isBlockResource;

    public JsRenderedStrategy(
            String url,
            String callBackRegex,
            String encoding,
            String userAgentString,
            Map<String, String> header,
            String actionJs,
            boolean isBlockBlob,
            long timeOut,
            boolean isBlockResource
    ) {
        this.url = url;
        this.callBackRegex = callBackRegex;
        this.encoding = encoding;
        this.userAgentString = userAgentString;
        this.header = header;
        this.actionJs = actionJs;
        this.isBlockBlob = isBlockBlob;
        this.timeOut = timeOut;
        this.isBlockResource = isBlockResource;
    }
}
