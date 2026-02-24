package com.javacore.server.util;

import java.util.Map;

/**
 * Maps file extensions to Content-Type header values.
 */
public final class ContentTypes {

    private static final Map<String, String> BY_EXTENSION = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"),
            Map.entry("htm", "text/html; charset=utf-8"),
            Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("js", "application/javascript; charset=utf-8"),
            Map.entry("json", "application/json; charset=utf-8"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("svg", "image/svg+xml")
    );

    private static final String DEFAULT = "application/octet-stream";

    private ContentTypes() {
    }

    public static String forExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return DEFAULT;
        }
        String ext = extension.toLowerCase();
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        return BY_EXTENSION.getOrDefault(ext, DEFAULT);
    }

    public static String forPath(String path) {
        if (path == null) {
            return DEFAULT;
        }
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0 || lastDot >= path.length() - 1) {
            return DEFAULT;
        }
        return forExtension(path.substring(lastDot + 1));
    }
}
