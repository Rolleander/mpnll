package com.broll.mpnll.gwt;

import com.broll.mpnll.ConnectionDefaults;

public final class WebsocketUrl {

    public static String normalizeUrl(String value, boolean securePage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WebSocket host must not be empty");
        }

        int defaultPort = ConnectionDefaults.DEFAULT_WS_PORT;
        String url = value.trim();
        String lowerUrl = url.toLowerCase();

        String scheme;

        if (lowerUrl.startsWith("ws://")) {
            scheme = "ws://";
            url = url.substring("ws://".length());
        } else if (lowerUrl.startsWith("wss://")) {
            scheme = "wss://";
            url = url.substring("wss://".length());
        } else if (lowerUrl.startsWith("http://")) {
            scheme = "ws://";
            url = url.substring("http://".length());
        } else if (lowerUrl.startsWith("https://")) {
            scheme = "wss://";
            url = url.substring("https://".length());
        } else if (url.startsWith("//")) {
            scheme = securePage ? "wss://" : "ws://";
            url = url.substring(2);
        } else if (url.contains("://")) {
            throw new IllegalArgumentException(
                "Unsupported WebSocket URL scheme: " + value);
        } else {
            scheme = securePage ? "wss://" : "ws://";
        }

        // Find the end of the authority/host portion.
        int authorityEnd = url.length();

        int slash = url.indexOf('/');
        if (slash >= 0) {
            authorityEnd = Math.min(authorityEnd, slash);
        }

        int query = url.indexOf('?');
        if (query >= 0) {
            authorityEnd = Math.min(authorityEnd, query);
        }

        int fragment = url.indexOf('#');
        if (fragment >= 0) {
            authorityEnd = Math.min(authorityEnd, fragment);
        }

        String authority = url.substring(0, authorityEnd);

        // IPv6 addresses contain colons, so handle them separately.
        boolean hasPort;
        if (authority.startsWith("[")) {
            int closingBracket = authority.indexOf(']');
            hasPort = closingBracket >= 0
                && closingBracket + 1 < authority.length()
                && authority.charAt(closingBracket + 1) == ':';
        } else {
            hasPort = authority.indexOf(':') >= 0;
        }

        if (!hasPort) {
            url = authority + ":" + defaultPort + url.substring(authorityEnd);
        }

        return scheme + url;
    }

}
