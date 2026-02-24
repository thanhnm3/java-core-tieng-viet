package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * I/O & NIO topic - loads content from multiple HTML fragments.
 */
public final class IoNioTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/io-nio/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-stream.html",
            "03-file.html",
            "04-nio-buffer.html",
            "05-nio-channel.html",
            "06-path-files.html",
            "07-selector.html"
    );

    @Override
    public String getSlug() {
        return "io-nio";
    }

    @Override
    public String getTitle() {
        return "I/O & NIO - Stream, Channel, Buffer";
    }

    @Override
    public String getDescription() {
        return "Stream, Channel, Buffer, Path, Files, Selector";
    }

    @Override
    public String getContentHtml() {
        StringBuilder sb = new StringBuilder();
        ClassLoader loader = getClass().getClassLoader();

        for (String fragment : FRAGMENTS) {
            String path = FRAGMENT_BASE + fragment;
            String content = loadFragment(loader, path);
            sb.append(content);
        }

        return sb.toString();
    }

    private String loadFragment(ClassLoader loader, String path) {
        try (InputStream in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return "<p>Fragment not found: " + path + "</p>";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<p>Error loading: " + path + "</p>";
        }
    }
}
