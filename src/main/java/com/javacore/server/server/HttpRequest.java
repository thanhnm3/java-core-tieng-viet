package com.javacore.server.server;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable HTTP request model.
 * Path is normalized (query string removed) for routing.
 */
public record HttpRequest(
        String method,
        String path,
        Map<String, String> headers,
        byte[] body
) {
    private static final int QUERY_INDEX = -1;

    /**
     * Returns path without query string for routing.
     */
    public String getPathForRouting() {
        int queryStart = path.indexOf('?');
        if (queryStart == QUERY_INDEX) {
            return path;
        }
        return path.substring(0, queryStart);
    }

    public static HttpRequest of(String method, String path, Map<String, String> headers, byte[] body) {
        return new HttpRequest(
                method != null ? method : "GET",
                path != null ? path : "/",
                headers != null ? Map.copyOf(headers) : Collections.emptyMap(),
                body != null ? body : new byte[0]
        );
    }
}
