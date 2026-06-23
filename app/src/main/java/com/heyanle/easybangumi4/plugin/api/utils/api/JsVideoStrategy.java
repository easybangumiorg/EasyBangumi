package com.heyanle.easybangumi4.plugin.api.utils.api;

import java.util.Map;

public class JsVideoStrategy {

    public final String url;
    public final String userAgentString;
    public final Map<String, String> header;
    public final String actionJs;
    public final long timeOut;
    public final boolean useLegacyParser;

    public JsVideoStrategy(
            String url,
            String userAgentString,
            Map<String, String> header,
            String actionJs,
            long timeOut,
            boolean useLegacyParser
    ) {
        this.url = url;
        this.userAgentString = userAgentString;
        this.header = header;
        this.actionJs = actionJs;
        this.timeOut = timeOut;
        this.useLegacyParser = useLegacyParser;
    }
}
